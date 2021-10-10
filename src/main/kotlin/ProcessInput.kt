import java.io.File

fun getContentFromFile(commandLine: CommandLine): Pair<Diagram, List<Cell>>? {
	val content = readInputFile(commandLine.inputFile) ?: return null
	return Pair(
		commandLine.type,
		content.map {
			getCell(commandLine.type, it) ?: return null
		}
	)
}

fun getChartCell(line: List<String>): ChartCell? = when {
	line.size != 3 -> {
		println("3 arguments expected")
		null
	}
	line[1].toFloatOrNull() == null -> {
		println("Float expected")
		null
	}
	else -> ChartCell(line[1].toFloat(), line[0], line[2])
}

fun getPlotCell(line: List<String>): PlotCell? = when {
	line.size != 4 -> {
		println("4 arguments expected")
		null
	}
	line[0].toFloatOrNull() == null || line[1].toFloatOrNull() == null -> {
		println("Float expected")
		null
	}
	else -> PlotCell(line[0].toFloat(), line[1].toFloat(), line[2], line[3])
}

fun getCell(type: Diagram, line: List<String>) = when (type) {
	Diagram.CIRCLE, Diagram.BAR_CHART -> getChartCell(line)
	Diagram.PLOT -> getPlotCell(line)
}

data class CommandLine(val inputFile: String, val outputFile: String, val type: Diagram)

fun processCommandLine(args: Array<String>): CommandLine? {
	if (args.size !in 2..3) {
		println("Three or two arguments expected")
		return null
	}
	val type = when (args[0].lowercase()) {
		"circle", "circlediagram" -> Diagram.CIRCLE
		"barchart", "bar_chart" -> Diagram.BAR_CHART
		"plot" -> Diagram.PLOT
		else -> {
			println("Unknown diagram type")
			return null
		}
	}
	return when (args.size) {
		2 -> CommandLine(args[1], "Diagram.png", type)
		else -> CommandLine(args[1], args[2], type)
	}
}

fun readInputFile(inputFile: String): List<List<String>>? {
	if (!File(inputFile).exists()) {
		println("file $inputFile does not exist")
		return null
	}
	val res = mutableListOf<List<String>>()
	File(inputFile).forEachLine {
		res.add(parseFileLine(it))
	}
	return res.toList()
}

fun parseFileLine(line: String): List<String> {
	val separator = ','
	return line.split(separator)
}