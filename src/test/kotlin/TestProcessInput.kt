import kotlin.test.*

class TestProcessInput{

	@Test
	fun testParseFileLine() {
		assertEquals(listOf("1", "2", "3", "4"), parseFileLine("1,2,3,4"))
		assertEquals(listOf("123"), parseFileLine("123"))
		assertEquals(listOf("1", "2", "3", "\"1,2,3\"", "4", "5"), parseFileLine("1,2,3,\"1,2,3\",4,5"))
	}

}