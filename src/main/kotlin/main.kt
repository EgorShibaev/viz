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
		ChartCell(100f, "text1"),
		ChartCell(64f, "text2"),
		ChartCell(127f, "text3"),
		ChartCell(1000f, "text4")
	)
	val intContent = listOf(
		PlotCell(1f, 1f, "text1"),
		PlotCell(2f, 10f, "text32"),
		PlotCell(3f, 5f, "text32"),
		PlotCell(5f, 15f, "text32"),
		PlotCell(7f, -1f, "text32"),
		PlotCell(8f, -2f, "text32"),
		PlotCell(-1f, 3f, "text32"),
		PlotCell(15f, 25f, "text32")
	)
	createWindow("plot", Diagram.PLOT, intContent)
	createWindow("bar chart", Diagram.BAR_CHART, content)
	createWindow("circle", Diagram.CIRCLE, content)
}

open class Cell

class PlotCell(val x: Float, val y: Float, val text: String) : Cell(), Comparable<PlotCell> {
	override operator fun compareTo(other: PlotCell): Int = when {
		x > other.x -> 1
		x == other.x -> y.compareTo(other.y)
		else -> -1
	}
}

class ChartCell(val value: Float, val text: String) : Cell()

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
		when (type) {
			Diagram.BAR_CHART -> barChart(
				canvas, Rect(w / 100F, h / 100F, w.toFloat() - 10F, h.toFloat() - 10F),
				content.map { it as? ChartCell ?: throw IllegalArgumentException("Wrong type for content") }
			)
			Diagram.CIRCLE -> separatedCircle(
				canvas, w / 2F, h / 2F, min(w / 2F, h / 2F) - 30F,
				content.map { it as? ChartCell ?: throw IllegalArgumentException("Wrong type for content") }
			)

			Diagram.PLOT -> {
				plot(
					canvas, Rect(w / 100f, h / 100f, w.toFloat() - 10F, h.toFloat() - 10F),
					content.map { it as? PlotCell ?: throw IllegalArgumentException("Wrong type for content") },
					PlotMode.WITH_SEGMENTS
				)
			}
		}
		layer.needRedraw()
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
	var lastDraggedX : Float? = null
	var lastDraggedY : Float? = null
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
			}
			else {
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