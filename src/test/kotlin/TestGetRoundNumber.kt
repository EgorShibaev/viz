import kotlin.test.*

class TestNearestRoundNumber {

	@Test
	fun testUsualNumbers() {
		assertEquals(1f, getNearestRoundNumber(1f))
		assertEquals(1f, getNearestRoundNumber(1.5f))
		assertEquals(2f, getNearestRoundNumber(2f))
		assertEquals(2f, getNearestRoundNumber(3f))
		assertEquals(5f, getNearestRoundNumber(4f))
		assertEquals(5f, getNearestRoundNumber(5f))
		assertEquals(5f, getNearestRoundNumber(7f))
		assertEquals(10f, getNearestRoundNumber(8f))
	}

	@Test
	fun testSmallNumbers() {
		assertEquals(0.001f, getNearestRoundNumber(0.001f))
		assertEquals(0.001f, getNearestRoundNumber(0.0015f))
		assertEquals(0.002f, getNearestRoundNumber(0.002f))
		assertEquals(0.002f, getNearestRoundNumber(0.003f))
		assertEquals(0.005f, getNearestRoundNumber(0.004f))
		assertEquals(0.005f, getNearestRoundNumber(0.005f))
		assertEquals(0.005f, getNearestRoundNumber(0.007f))
		assertEquals(0.01f, getNearestRoundNumber(0.008f))
	}

	@Test
	fun testBigNumbers() {
		assertEquals(10000f, getNearestRoundNumber(10000f))
		assertEquals(10000f, getNearestRoundNumber(15000f))
		assertEquals(20000f, getNearestRoundNumber(20000f))
		assertEquals(20000f, getNearestRoundNumber(30000f))
		assertEquals(50000f, getNearestRoundNumber(40000f))
		assertEquals(50000f, getNearestRoundNumber(50000f))
		assertEquals(50000f, getNearestRoundNumber(70000f))
		assertEquals(100000f, getNearestRoundNumber(80000f))
	}
}

class TestGetRoundNumberMore {

	@Test
	fun testUsualNumbers() {
		assertEquals(1f, getRoundNumberMore(1f))
		assertEquals(2f, getRoundNumberMore(1.5f))
		assertEquals(2f, getRoundNumberMore(2f))
		assertEquals(3f, getRoundNumberMore(3f))
		assertEquals(4f, getRoundNumberMore(3.4f))
		assertEquals(20f, getRoundNumberMore(10.3f))
		assertEquals(200f, getRoundNumberMore(123f))
		assertEquals(50f, getRoundNumberMore(42f))
	}

	@Test
	fun testSmallNumbers() {
		assertEquals(0.0001f, getRoundNumberMore(0.0001f))
		assertEquals(0.0002f, getRoundNumberMore(0.00015f))
		assertEquals(0.0002f, getRoundNumberMore(0.0002f))
		assertEquals(0.0004f, getRoundNumberMore(0.0003001f))
		assertEquals(0.0004f, getRoundNumberMore(0.00034f))
		assertEquals(0.002f, getRoundNumberMore(0.00103f))
		assertEquals(0.02f, getRoundNumberMore(0.0123f))
		assertEquals(0.005f, getRoundNumberMore(0.0042f))
	}

	@Test
	fun testBigNumbers() {
		assertEquals(10000f, getRoundNumberMore(10000f))
		assertEquals(20000f, getRoundNumberMore(15000f))
		assertEquals(20000f, getRoundNumberMore(20000f))
		assertEquals(30000f, getRoundNumberMore(30000f))
		assertEquals(40000f, getRoundNumberMore(34000f))
		assertEquals(200000f, getRoundNumberMore(103000f))
		assertEquals(2000000f, getRoundNumberMore(1230000f))
		assertEquals(500000f, getRoundNumberMore(420000f))
	}
}