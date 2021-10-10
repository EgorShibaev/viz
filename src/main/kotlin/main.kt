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

fun main(args: Array<String>) {
	val commandLine = processCommandLine(args) ?: return
	val (type, content) = getContentFromFile(commandLine) ?: return
	writeToFile(commandLine.outputFile, type, content)
	createWindow(type.toString(), type, content)
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

		drawDiagram(canvas, type, content, w.toFloat(), h.toFloat())

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