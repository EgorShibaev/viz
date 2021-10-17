import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.swing.Swing
import org.jetbrains.skija.*
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkiaRenderer
import org.jetbrains.skiko.SkiaWindow
import java.awt.Dimension
import java.awt.event.*
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import javax.swing.WindowConstants
import kotlin.io.path.Path
import kotlin.math.abs
import kotlin.math.pow
import mu.KotlinLogging

val logger = KotlinLogging.logger {}

/*
* arguments example (you can find it in test/Automation but
* these tests are only for check png files):
*
* plot tests/Plot/input.txt tests/Plot/output.png
* barChart tests/BarChart/input.txt tests/BarChart/output.png
* Circle tests/Circle/input.txt tests/Circle/output.png
* Polar tests/Polar/input.txt tests/Polar/output.png
* tree tests/Tree/input.txt tests/Tree/output.png
* */

fun main(args: Array<String>) {
	val commandLine = processCommandLine(args) ?: return
	val (type, content) = getContentFromFile(commandLine) ?: return
	writeToFile(commandLine.outputFile, type, content)
	createWindow(type.toString(), type, content)
}


/**
 * These classes contain info of each cell: name and detailedInfo for each
 * cell, coordinates for PlotCell and value for ChartCell.
 * PlotCell is used for plot. And ChartCell for others.
 * */
open class Cell(open val name: String, open val detailedInfo: String)

data class PlotCell(val x: Float, val y: Float, override val name: String, override val detailedInfo: String) :
	Cell(name, detailedInfo), Comparable<PlotCell> {
	override operator fun compareTo(other: PlotCell): Int = when {
		x > other.x -> 1
		x == other.x -> y.compareTo(other.y)
		else -> -1
	}
}

data class ChartCell(val value: Float, override val name: String, override val detailedInfo: String) :
	Cell(name, detailedInfo)

data class TreeCell(val children: List<TreeCell>, override val name: String, override val detailedInfo: String) :
	Cell(name, detailedInfo)

enum class Diagram {
	CIRCLE, BAR_CHART, PLOT, POLAR_CHART, TREE
}

fun writeToFile(outputFile: String, type: Diagram, content: List<Cell>) {
	val w = 1500
	val h = 1000
	val surface = Surface.makeRasterN32Premul(w, h)
	val canvas = surface.canvas
	canvas.drawRect(Rect(0f, 0f, w.toFloat(), h.toFloat()), Paint().apply { color = 0xffffffff.toInt() })
	drawDiagram(canvas, type, content, w.toFloat(), h.toFloat())
	val image: Image = surface.makeImageSnapshot()
	val pngData = image.encodeToData(EncodedImageFormat.PNG)!!
	val pngBytes = pngData.toByteBuffer()
	val path = Path(outputFile)
	val channel = Files.newByteChannel(
		path,
		StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE
	)
	channel.write(pngBytes)
	channel.close()
}

fun getNearestRoundNumber(value: Float): Float {
	val roundNumbers = (-10..10).map {
		// add round numbers with 1, 2, 5 as first digits
		// 1, 2, 5 was chosen because these numbers
		// look good when divide by 10.
		listOf(10.0.pow(it), 10.0.pow(it) * 2, 10.0.pow(it) * 5)
	}.flatten().map { it.toFloat() }
	return roundNumbers.minByOrNull { abs(it - value) } ?: 1f
}

fun getRoundNumberMore(value: Float): Float {
	// roundNumbers contain numbers like d * (10 ^ p)
	val roundNumbers = (-10..10).map {
		val round = 10.0.pow(it)
		(1..9).map { firstDigit -> round * firstDigit }
	}.flatten().map { it.toFloat() }
	return roundNumbers.firstOrNull { it >= value } ?: value
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
	var mouseX = 0f
	var mouseY = 0f
	var e: MouseWheelEvent? = null
	var pressedKeyCode: Int? = null
	// variables below are used for moving plot (see MyMouseMotionAdapter)
	var vectorToMoveX = 0f
	var vectorToMoveY = 0f
	var lastDraggedX: Float? = null
	var lastDraggedY: Float? = null
}