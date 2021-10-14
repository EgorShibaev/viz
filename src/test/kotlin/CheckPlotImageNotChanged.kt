import java.io.File
import kotlin.random.Random
import kotlin.test.*

/**
 * In this test checked that content of png file didn't change.
 * There is expectedContent with values of random bytes in file.
 * If there is some changes in interface programmer can run
 * printFileContent function and take new content from info
 * channel in logger.
 */
class CheckPlotImageNotChanged {

	private var expectedContent = listOf<Byte>(
		-35, -61, 112, 68, 90, 56, 8, -32, 117, 112,
		36, -94, 0, -81, 7, 40, -2, 70, -7, 25,
		0, -100, 42, -49, 14, -128, 0, 59, -118, 59,
		-10, 67, -71, 0, 0, -36, 106, 7, -24, -98,
		0, 0, 0, -59, -41, -36, -106, 123, -20, -48,
		-88, -80, 53, 123, 106, -83, 127, 76, 21, -17,
		62, 62, 98, 0, 67, 50, -116, -81, -96, 26,
		-87, 9, 24, 86, 68, -75, -8, 17, -10, 116,
		14, 109, -125, 112, -91, 0, 88, 44, 0, -65,
		-29, 52, -13, -101, -55, 29, -110, -21, 76, 0,
		107
	)

	private val name = "tests/Plot/output.png"

	private fun readFile(): List<Byte> {
		val content = File(name).readBytes()
		return (0..100).map {
			content[Random(it).nextInt(from = 0, until = content.size)]
		}
	}

	@Test
	fun printFileContent() {
		val result = readFile().joinToString(separator = " ").split(' ')
		logger.info { result.chunked(10).joinToString(separator = ",\n") { it.joinToString(separator = ", ") } }
	}

	@Test
	fun testFileContent() {
		assertContentEquals(expectedContent, readFile())
	}

}