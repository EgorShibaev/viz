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
class CheckCircleImageNotChanged {

	private var expectedContent = listOf<Byte>(
		-2, 8, 53, 108, -11, -68, 120, -43, -28, 34,
		95, 74, -115, 7, 126, 123, 4, 49, -22, -87,
		100, -27, 9, 125, 111, 14, -97, 27, 86, 35,
		-104, 0, 8, 46, -39, 105, 2, -45, -87, 126,
		-87, -63, -30, -19, -100, -40, -99, 58, 25, -100,
		-67, -70, 61, -81, -89, -68, -109, 17, 67, 119,
		41, -26, -115, 79, 73, 0, -1, -115, -126, -116,
		0, 121, 14, -103, 9, -100, 113, -31, 15, 123,
		-17, 16, -11, 28, -60, -111, 59, -90, -37, 13,
		113, -50, -44, -31, -107, 33, -46, -86, 109, 37,
		106
	)

	private val name = "tests/Circle/output.png"

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