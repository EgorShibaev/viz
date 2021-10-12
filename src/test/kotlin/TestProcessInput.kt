import kotlin.test.*

class TestProcessInput {

	@Test
	fun testParseFileLine() {
		assertEquals(listOf("1", "2", "3", "4"), parseFileLine("1,2,3,4"))
		assertEquals(listOf("123"), parseFileLine("123"))
		assertEquals(listOf("1", "2", "3", "\"1,2,3\"", "4", "5"), parseFileLine("1,2,3,\"1,2,3\",4,5"))
		assertEquals(listOf("1", "2", "\"1, 2\"", "3", "4", "\"5, 6\""), parseFileLine("1,2,\"1, 2\",3,4,\"5, 6\""))
	}

	@Test
	fun testProcessCommandLine() {
		assertEquals(null, processCommandLine(arrayOf("one")))
		assertEquals(null, processCommandLine(arrayOf("one", "two", "tree", "four")))
		assertEquals(null, processCommandLine(arrayOf("unknown type", "file1", "file2")))
		assertEquals(
			CommandLine("input", "output", Diagram.PLOT),
			processCommandLine(arrayOf("plot", "input", "output"))
		)
		assertEquals(
			CommandLine("input", "output", Diagram.CIRCLE),
			processCommandLine(arrayOf("CIRCLE", "input", "output"))
		)
		assertEquals(
			CommandLine("input", "output", Diagram.CIRCLE),
			processCommandLine(arrayOf("CircleDiagram", "input", "output"))
		)
		assertEquals(
			CommandLine("input", "output", Diagram.POLAR_CHART),
			processCommandLine(arrayOf("PolAR", "input", "output"))
		)
		assertEquals(
			CommandLine("input", "output", Diagram.BAR_CHART),
			processCommandLine(arrayOf("BarChart", "input", "output"))
		)
	}

	@Test
	fun testGetChartCell() {
		assertEquals(null, getCell(Diagram.BAR_CHART, listOf("one")))
		assertEquals(null, getCell(Diagram.CIRCLE, listOf("one", "two")))
		assertEquals(null, getCell(Diagram.BAR_CHART, listOf("one", "two", "three", "four")))
		assertEquals(null, getCell(Diagram.POLAR_CHART, listOf("name", "1.2a", "info")))
		assertEquals(
			ChartCell(10.5f, "name", "info"),
			getCell(Diagram.CIRCLE, listOf("name", "10.5", "info"))
		)
		assertEquals(
			ChartCell(5f, "name", "info"),
			getCell(Diagram.POLAR_CHART, listOf("name", "5", "info"))
		)
	}

	@Test
	fun testGetPlotCell() {
		assertEquals(null, getPlotCell(listOf("one", "two")))
		assertEquals(null, getPlotCell(listOf("one", "two", "tree")))
		assertEquals(null, getPlotCell(listOf("one", "two", "tree", "four", "five")))
		assertEquals(null, getPlotCell(listOf("name", "not float", "not float", "info")))
		assertEquals(null, getPlotCell(listOf("name", "1", "not float", "info")))
		assertEquals(null, getPlotCell(listOf("name", "not float", "1", "info")))
		assertEquals(
			PlotCell(1.5f, 1.5f, "name", "info"),
			getPlotCell(listOf("1.5", "1.5", "name", "info"))
		)
		assertEquals(
			PlotCell(100f, -10f, "name", "info"),
			getPlotCell(listOf("100", "-10", "name", "info"))
		)
	}
}