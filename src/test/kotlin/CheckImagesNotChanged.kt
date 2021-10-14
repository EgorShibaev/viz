import org.jetbrains.skija.Image
import org.jetbrains.skija.Paint
import org.jetbrains.skija.Rect
import org.jetbrains.skija.Surface
import kotlin.test.*

/**
 * In these tests checked that image didn't change.
 * There is function getImageHash which draw diagram
 * and calculate hash of image (hash is sum actually, but for our
 * goals it is ok).
 * If programmer want to change interface, he should
 * change interface, run printCurrentHash function and change
 * expectedHash variable
 */
fun getImageHash(type: Diagram, content: List<Cell>): Int {
	val w = 1500
	val h = 1000
	val surface = Surface.makeRasterN32Premul(w, h)
	val canvas = surface.canvas
	canvas.drawRect(Rect(0f, 0f, w.toFloat(), h.toFloat()), Paint().apply { color = 0xFFFFFFFF.toInt() })
	drawDiagram(canvas, type, content, w.toFloat(), h.toFloat())
	val image: Image = surface.makeImageSnapshot()
	val buffer = image.peekPixels()
	if (buffer == null) {
		assert(false)
		return 0
	}
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

class CheckBarChartImageNotChanged {

	private val expectedHash = 20286
	private val type = Diagram.BAR_CHART

	@Test
	fun printCurrentHash() {
		logger.info { "Current hash of image of $type diagram - ${getImageHash(type, otherContent)}" }
	}

	@Test
	fun testHashNotChange() {
		assertEquals(expectedHash, getImageHash(type, otherContent))
	}
}

class CheckPolarImageNotChanged {

	private val expectedHash = -57857
	private val type = Diagram.POLAR_CHART

	@Test
	fun printCurrentHash() {
		logger.info { "Current hash of image of $type diagram - ${getImageHash(type, otherContent)}" }
	}

	@Test
	fun testHashNotChange() {
		assertEquals(expectedHash, getImageHash(type, otherContent))
	}
}

class CheckCircleImageNotChanged {

	private val expectedHash = -33841
	private val type = Diagram.CIRCLE

	@Test
	fun printCurrentHash() {
		logger.info { "Current hash of image of $type diagram - ${getImageHash(type, otherContent)}" }
	}

	@Test
	fun testHashNotChange() {
		assertEquals(expectedHash, getImageHash(type, otherContent))
	}
}