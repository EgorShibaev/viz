import org.jetbrains.skija.Image
import org.jetbrains.skija.Surface
import kotlin.test.*

/**
 * In this test checked that image didn't change.
 * There is function getCurrentHash which draw diagram
 * and calculate hash of image.
 * If programmer want to change interface, he should
 * change interface, run printCurrentHash function and change
 * expectedHash variable
 */
class CheckPlotImageNotChanged {

	private val expectedHash = -1814274412

	private val type = Diagram.PLOT

	private val fileName = "tests/Plot/input.txt"

	private fun getCurrentHash(): Int {
		val w = 1500
		val h = 1000
		val content = getContentFromFile(CommandLine(fileName, "", type))?.second
		if (content == null) {
			assert(false)
			return 0
		}
		val surface = Surface.makeRasterN32Premul(w, h)
		val canvas = surface.canvas
		drawDiagram(canvas, type, content, w.toFloat(), h.toFloat())
		val image: Image = surface.makeImageSnapshot()
		return image.peekPixels().hashCode()
	}

	@Test
	fun printCurrentHash() {
		logger.info { "Current hash of image of $type diagram - ${getCurrentHash()}" }
	}

	@Test
	fun testHashNotChange() {
		assertEquals(expectedHash, getCurrentHash())
	}

}