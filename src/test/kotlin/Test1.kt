import kotlin.test.*

internal class Test1 {

    @Test
    fun testPlot() {
        main(arrayOf("diagram.plot.plot", "tests/Plot/input.txt", "tests/Plot/output.png"))
    }

    @Test
    fun testBarChart() {
        main(arrayOf("barChart", "tests/BarChart/input.txt", "tests/BarChart/output.png"))
    }

    @Test
    fun testCircle() {
        main(arrayOf("circle", "tests/Circle/input.txt", "tests/Circle/output.png"))
    }
}
