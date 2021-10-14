import kotlin.test.*

internal class Automation {

    private fun write(args: Array<String>) {
        val commandLine = processCommandLine(args)
        if (commandLine == null) {
            assert(false)
            return
        }
        val cont = getContentFromFile(commandLine)
        if (cont == null) {
            assert(false)
            return
        }
        val (type, content) = cont
        writeToFile(commandLine.outputFile, type, content)
    }

    @Test
    fun testPlot() {
        write(arrayOf("plot", "tests/Plot/input.txt", "tests/Plot/output.png"))
    }

    @Test
    fun testBarChart() {
        write(arrayOf("barChart", "tests/BarChart/input.txt", "tests/BarChart/output.png"))
    }

    @Test
    fun testCircle() {
        write(arrayOf("circle", "tests/Circle/input.txt", "tests/Circle/output.png"))
    }

    @Test
    fun testPolar() {
        write(arrayOf("polar", "tests/Polar/input.txt", "tests/Polar/output.png"))
    }
}
