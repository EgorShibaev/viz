import diagram.PlotState
import org.jetbrains.skija.*
import kotlin.test.*

/**
 * In these tests checked that image didn't change.
 * There is function getImageHash which draw diagram
 * and calculate hash of image (hash is sum actually, but for our
 * goals it is ok).
 * If programmer want to change interface, he should
 * change interface, run printCurrentHash function and change
 * expectedHashes variable.
 * Function printCurrentHash is framed as Test because it
 * will be easier for programmer who want to change interface
 * run this function.
 *
 * For different operating system there are different hashes.
 * Now hashes for Windows and Linux are added.
 * First hash is for Windows second for linux
 */
fun getImageHash(type: Diagram, content: List<Cell>): Int {
	val w = 1500
	val h = 1000
	val surface = Surface.makeRasterN32Premul(w, h)
	val canvas = surface.canvas
	canvas.drawRect(Rect(0f, 0f, w.toFloat(), h.toFloat()), Paint().apply { color = 0xFFFFFFFF.toInt() })
	if (type == Diagram.PLOT)
		PlotState.apply {
			x0 = 0f
			y0 = 0f
			x1 = 0f
			y1 = 0f
			lastHeight = 0f
			lastWidth = 0f
		}
	drawDiagram(canvas, type, content, w.toFloat(), h.toFloat())
	val image: Image = surface.makeImageSnapshot()
	val a = image.encodeToData()?.bytes
	return a?.sum() ?: 0
}

val otherContent = listOf(
	ChartCell(1f, "name1", "info1"),
	ChartCell(123f, "name2", "info2"),
	ChartCell(312f, "name3", "info3"),
	ChartCell(21f, "name4", "info4"),
	ChartCell(3f, "name5", "info5"),
)

val plotContent = listOf(
	PlotCell(1f, 1f, "name1", "info"),
	PlotCell(1f, 12f, "name1", "info"),
	PlotCell(13f, 1f, "name1", "info"),
	PlotCell(-1f, 1f, "name1", "info"),
	PlotCell(1f, -1f, "name1", "info"),
)

val treeRoot = TreeCell(listOf(TreeCell(emptyList(), "1"), TreeCell(emptyList(), "2")), "3")

class CheckBarChartImageNotChanged {

	private val expectedHashes = mapOf(20286 to "windows", -21864 to "linux")
	private val type = Diagram.BAR_CHART

	@Test
	fun printCurrentHash() {
		logger.info { "Current hash of image of $type diagram - ${getImageHash(type, otherContent)}" }
	}

	@Test
	fun testHashNotChange() {
		assertContains(expectedHashes, getImageHash(type, otherContent))
	}
}

class CheckPolarImageNotChanged {

	private val expectedHashes = mapOf(-57857 to "windows", -52739 to "linux")
	private val type = Diagram.POLAR_CHART

	@Test
	fun printCurrentHash() {
		logger.info { "Current hash of image of $type diagram - ${getImageHash(type, otherContent)}" }
	}

	@Test
	fun testHashNotChange() {
		assertContains(expectedHashes, getImageHash(type, otherContent))
	}
}

class CheckCircleImageNotChanged {

	private val expectedHashes = mapOf(-33841 to "windows", -16485 to "linux")
	private val type = Diagram.CIRCLE

	@Test
	fun printCurrentHash() {
		logger.info { "Current hash of image of $type diagram - ${getImageHash(type, otherContent)}" }
	}

	@Test
	fun testHashNotChange() {
		assertContains(expectedHashes, getImageHash(type, otherContent))
	}
}

class CheckPlotImageNotChanged {

	private val expectedHashes = mapOf(-17283 to "windows", -12826 to "linux")
	private val type = Diagram.PLOT

	@Test
	fun printCurrentHash() {
		logger.info { "Current hash of image of $type diagram - ${getImageHash(type, plotContent)}" }
	}

	@Test
	fun testHashNotChange() {
		assertContains(expectedHashes, getImageHash(type, plotContent))
	}
}

class CheckTreeImageNotChanged {

	// not set for Linux
	private val expectedHashes = mapOf(-29902 to "windows", -1 to "linux")
	private val type = Diagram.TREE

	@Test
	fun printCurrentHash() {
		logger.info { "Current hash of image of $type diagram - ${getImageHash(type, listOf(treeRoot))}" }
	}

	@Test
	fun testHashNotChange() {
		assertContains(expectedHashes, getImageHash(type, listOf(treeRoot)))
	}
}