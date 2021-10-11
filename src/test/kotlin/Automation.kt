import kotlin.test.*

internal class Automation {

    @Test
    fun testPlot() {
        main(arrayOf("plot", "tests/Plot/input.txt", "tests/Plot/output.png"))
    }

    @Test
    fun testBarChart() {
        main(arrayOf("barChart", "tests/BarChart/input.txt", "tests/BarChart/output.png"))
    }

    @Test
    fun testCircle() {
        main(arrayOf("circle", "tests/Circle/input.txt", "tests/Circle/output.png"))
    }

    @Test
    fun testPolar() {
        main(arrayOf("polar", "tests/polar/input.txt", "tests/polar/output.png"))
    }
}
