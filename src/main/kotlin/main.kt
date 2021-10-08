import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.swing.Swing
import org.jetbrains.skija.*
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkiaRenderer
import org.jetbrains.skiko.SkiaWindow
import java.awt.Dimension
import java.awt.event.*
import javax.swing.WindowConstants
import kotlin.math.*
import kotlin.random.Random

fun main() {
	val content = listOf(
		ChartCell(100f, "text1", "additional information number one"),
		ChartCell(66f, "text2", "additional information number two"),
		ChartCell(127f, "text3", "additional information number three"),
		ChartCell(1000f, "text4", "additional information number four"),
	)
	val intContent = listOf(
		PlotCell(1f, 1f, "text1", "additional information number one"),
		PlotCell(2f, 10f, "text32", "additional information number two"),
		PlotCell(3f, 5f, "text32", "additional information number thee"),
		PlotCell(5f, 15f, "text32", "additional information number four"),
		PlotCell(7f, -1f, "text32", "additional information number five"),
		PlotCell(8f, -2f, "text32", "additional information number six"),
		PlotCell(-1f, 3f, "text32", "additional information number seven"),
		PlotCell(15f, 25f, "text32", "additional information number eight"),
	)
	createWindow("plot", Diagram.PLOT, intContent)
	createWindow("bar chart", Diagram.BAR_CHART, content)
	createWindow("circle", Diagram.CIRCLE, content)
}

open class Cell

class PlotCell(val x: Float, val y: Float, val name: String, val detailedInfo: String) : Cell(), Comparable<PlotCell> {
	override operator fun compareTo(other: PlotCell): Int = when {
		x > other.x -> 1
		x == other.x -> y.compareTo(other.y)
		else -> -1
	}
}

class ChartCell(val value: Float, val name: String, val detailedInfo: String) : Cell()

enum class Diagram {
	CIRCLE, BAR_CHART, PLOT
}

fun randomColor(seed: Float) = Paint().apply {
	color = 0xFF000000.toInt() + Random((ln(seed + 1) * 1e7).toInt()).nextInt() % 0x1000000
}

private val typeface = Typeface.makeFromFile("fonts/JetBrainsMono-Regular.ttf")
val font = Font(typeface, 15f)
val thinFont = Font(typeface, 12f)
val paint = Paint().apply {
	color = 0xff000000.toInt()
	mode = PaintMode.FILL
	strokeWidth = 1f
}

val stroke = Paint().apply {
	color = 0xFF000000.toInt()
	mode = PaintMode.STROKE
	strokeWidth = 2f
}

val thinStroke = Paint().apply {
	color = 0x5F000000
	mode = PaintMode.STROKE
	strokeWidth = 0.5f
}

fun getTextWidth(text: String, font: Font) = text.length * font.size * 0.6f

fun drawWithByRight(canvas: Canvas, text: String, right: Float, y: Float, font: Font, paint: Paint) {
	canvas.drawString(text, right - getTextWidth(text, font), y, font, paint)
}

fun drawStringInRect(canvas: Canvas, text: String, rect: Rect, font: Font) {
	var currX = rect.left
	var currY = rect.top + font.size
	val words = text.split(' ')
	words.forEach {
		if (currX + font.size * 0.6f * it.length > rect.right && currX != rect.left){
			currX = rect.left
			currY += font.size + 2f
		}
		canvas.drawString(it, currX, currY, font, paint)
		currX += font.size * 0.6f * (it.length + 1)
	}
}

fun distance(x0: Float, y0: Float, x1: Float, y1: Float) = sqrt((x0 - x1).pow(2) + (y0 - y1).pow(2))

fun createWindow(title: String, type: Diagram, content: List<Cell>) = runBlocking(Dispatchers.Swing) {
	val window = SkiaWindow()
	window.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
	window.title = title

	window.layer.renderer = Renderer(window.layer, type, content)
	window.layer.addMouseMotionListener(MyMouseMotionAdapter)
	window.layer.addMouseWheelListener(MyMouseWheelListener)
	window.layer.addKeyListener(MyKeyAdapter)

	window.preferredSize = Dimension(800, 600)
	window.minimumSize = Dimension(100, 100)
	window.pack()
	window.layer.awaitRedraw()
	window.isVisible = true
}

class Renderer(
	private val layer: SkiaLayer,
	private val type: Diagram,
	private val content: List<Cell>
) : SkiaRenderer {
	override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
		val contentScale = layer.contentScale
		canvas.scale(contentScale, contentScale)
		val w = (width / contentScale).toInt()
		val h = (height / contentScale).toInt()

		assert(content.isNotEmpty())
		checkIsContentCorrect()

		when (type) {
			Diagram.BAR_CHART -> barChart(
				canvas, Rect(w / 100F, h / 100F, w.toFloat() - 10F, h.toFloat() - 10F),
				content.map { it as ChartCell }
			)
			Diagram.CIRCLE -> separatedCircle(
				canvas, w / 2F, h / 2F, min(w / 2F, h / 2F) - 30F,
				content.map { it as ChartCell }
			)
			Diagram.PLOT -> plot(
				canvas, Rect(w / 100f, h / 100f, w.toFloat() - 10F, h.toFloat() - 10F),
				content.map { it as PlotCell }, PlotMode.WITH_SEGMENTS
			)
		}
		layer.needRedraw()
	}

	private fun checkIsContentCorrect() {
		when (type) {
			Diagram.PLOT -> assert(content.all { it is PlotCell })
			Diagram.CIRCLE, Diagram.BAR_CHART -> assert(content.all { it is ChartCell })
		}
	}
}

object State {
	var lastHeight = 0f
	var lastWidth = 0f
	var x0 = -10f
	var y0 = -10f
	var x1 = 10f
	var y1 = 10f
	var mouseX = 0f
	var mouseY = 0f
	var e: MouseWheelEvent? = null
	var pressedKeyCode: Int? = null
	var vectorToMoveX = 0f
	var vectorToMoveY = 0f
	var lastDraggedX: Float? = null
	var lastDraggedY: Float? = null
}

object MyMouseMotionAdapter : MouseMotionAdapter() {
	override fun mouseMoved(event: MouseEvent) {
		State.mouseX = event.x.toFloat()
		State.mouseY = event.y.toFloat()
	}

	override fun mouseDragged(e: MouseEvent?) {
		e?.apply {
			val lastX = State.lastDraggedX
			val lastY = State.lastDraggedY
			if (lastX == null || lastY == null || abs(lastX - x) > 20f || abs(lastY - y) > 20f) {
				State.lastDraggedX = x.toFloat()
				State.lastDraggedY = y.toFloat()
			} else {
				State.vectorToMoveX += x - lastX
				State.vectorToMoveY += y - lastY
				State.lastDraggedX = x.toFloat()
				State.lastDraggedY = y.toFloat()
			}
		}
	}
}

object MyMouseWheelListener : MouseWheelListener {
	override fun mouseWheelMoved(e: MouseWheelEvent?) {
		State.e = e
	}
}

object MyKeyAdapter : KeyAdapter() {
	override fun keyPressed(e: KeyEvent?) {
		State.pressedKeyCode = e?.keyCode
	}
}