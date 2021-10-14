import diagram.DiagramSegment
import diagram.getDiagramSegments
import kotlin.math.abs
import kotlin.test.*

class TestGetCircleDiagramSegments {

	private val elements = listOf(
		ChartCell(1f, "name", "info"),
		ChartCell(2f, "name", "info"),
		ChartCell(3f, "name", "info"),
		ChartCell(4f, "name", "info")
	)

	private val centerX = 10f
	private val centerY = 10f
	private val radius = 10f

	private fun assertSegmentsEquals(expected: DiagramSegment, actual: DiagramSegment) {
		fun assertFloatsEquals(value1: Float, value2: Float) {
			val maxDiff = 1e-4
			assert(abs(value1 - value2) < maxDiff)
		}

		assertFloatsEquals(expected.beginAngle, actual.beginAngle)
		assertFloatsEquals(expected.endAngle, actual.endAngle)
	}

	private fun getSegment(beginAngle: Float, endAngle: Float, value: Float, content: List<ChartCell>): DiagramSegment {
		val proportion = (value / content.sumOf { it.value.toDouble() }.toFloat() * 100).toInt()
		return DiagramSegment(
			beginAngle, endAngle, "name - $value ($proportion%)", centerX, centerY, radius, "info"
		)
	}

	@Test
	fun testOneElement() {
		val content = listOf(elements[1])
		val expected = getSegment(0f, Math.PI.toFloat() * 2, elements[1].value, content)
		val given = getDiagramSegments(centerX, centerY, radius, content)
		assert(given.size == 1)
		assertSegmentsEquals(expected, given[0])
	}

	@Test
	fun testFewElement() {
		val content = listOf(elements[0], elements[1], elements[2], elements[3])
		val angles = listOf(
			0f,
			Math.PI.toFloat() * 2 * 1 / 10,
			Math.PI.toFloat() * 2 * 3 / 10,
			Math.PI.toFloat() * 2 * 6 / 10,
			Math.PI.toFloat() * 2 * 10 / 10,
		)
		val expectedSegments = listOf(
			getSegment(angles[0], angles[1], elements[0].value, content),
			getSegment(angles[1], angles[2], elements[1].value, content),
			getSegment(angles[2], angles[3], elements[2].value, content),
			getSegment(angles[3], angles[4], elements[3].value, content)
		)
		val givenSegments = getDiagramSegments(centerX, centerY, radius, content)
		for (index in 0..3)
			assertSegmentsEquals(expectedSegments[index], givenSegments[index])
	}

}