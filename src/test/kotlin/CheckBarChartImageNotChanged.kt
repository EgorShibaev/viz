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
class CheckBarChartImageNotChanged {

	private var expectedContent = listOf<Byte>(
		0, 0, 99, -1, 67, -126, 84, -72, 125, 104,
		110, -89, -54, -128, 0, 63, -40, -126, -2, 91,
		107, 63, -32, -73, -12, -45, -128, -64, 0, -3,
		-98, 90, 0, -126, 7, -83, 32, -123, -33, 112,
		0, -21, -100, 54, -75, 126, 0, 38, -24, -39,
		-41, 0, -126, 1, -94, 126, 7, -15, -127, 0,
		0, 0, 8, -10, 22, 64, -4, 9, 77, 27,
		27, 0, -38, 0, 111, 32, 106, 81, 0, -123,
		-32, -66, 123, 116, -86, 11, 51, -114, 18, -3,
		103, -23, 0, 98, -100, -8, 0, -77, -23, 0,
		-18
	)

	private val name = "tests/BarChart/output.png"

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