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
class CheckPolarImageNotChanged {

	private var expectedContent = listOf<Byte>(
		-77, -67, 40, -57, -33, 0, -125, 69, -18, -107,
		83, -97, -74, 105, 32, -25, -11, 126, 25, -50,
		118, 110, 34, 65, -32, 34, -57, 33, -91, 99,
		-19, -114, 3, -77, 111, 69, -74, 100, 55, -123,
		-70, -62, -21, -29, -62, 10, 37, 96, -75, -31,
		67, 8, -38, 16, 105, -113, -83, -28, -118, 85,
		91, 13, -92, -76, -11, -120, -39, -50, -13, 116,
		-22, -20, 121, 107, -92, -103, 69, 38, 43, 81,
		-3, 95, 0, -51, 78, 17, 99, -49, 4, -67,
		-61, 36, -24, 37, -25, -99, 27, 60, -36, -93,
		126
	)

	private val name = "tests/Polar/output.png"

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