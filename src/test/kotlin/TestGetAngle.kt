import diagram.*
import kotlin.math.abs
import kotlin.test.*

class TestGetAngle {

	private fun assertEqualsForFloats(value1: Float, value2: Float) {
		val maxDifference = 1e-4
		assert(abs(value1 - value2) < maxDifference)
	}

	private val segment = DiagramSegment(
		0f, Math.PI.toFloat() / 2, "", 100f, 100f, 100f, ""
	)

	@Test
	fun testGetAngleInFirstQuarter() {
		assertEqualsForFloats(
			Math.PI.toFloat() / 4,
			segment.getAngle(150f, 50f)
		)
		assertEqualsForFloats(
			0f,
			segment.getAngle(100f, 50f)
		)
	}

	@Test
	fun testGetAngleInSecondQuarter() {
		assertEqualsForFloats(
			3 * Math.PI.toFloat() / 4,
			segment.getAngle(150f, 150f)
		)

		assertEqualsForFloats(
			Math.PI.toFloat() / 2,
			segment.getAngle(200f, 100f)
		)
	}

	@Test
	fun testGetAngleInThirdQuarter() {
		assertEqualsForFloats(
			5 * Math.PI.toFloat() / 4,
			segment.getAngle(50f, 150f)

		)
		assertEqualsForFloats(
			Math.PI.toFloat(),
			segment.getAngle(100f, 200f)
		)
	}

	@Test
	fun testGetAngleInFourthQuarter() {
		assertEqualsForFloats(
			7 * Math.PI.toFloat() / 4,
			segment.getAngle(50f, 50f)

		)
		assertEqualsForFloats(
			3 * Math.PI.toFloat() / 2,
			segment.getAngle(50f, 100f)
		)
	}
}