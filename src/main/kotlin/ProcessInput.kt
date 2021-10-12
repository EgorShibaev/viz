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


data class CommandLine(val inputFile: String, val outputFile: String, val type: Diagram)

fun processCommandLine(args: Array<String>): CommandLine? {
	if (args.size !in 2..3) {
		logger.error { "Three or two arguments expected" }
		return null
	}
	val type = when (args[0].lowercase()) {
		"circle", "circlediagram" -> Diagram.CIRCLE
		"barchart", "bar_chart" -> Diagram.BAR_CHART
		"plot" -> Diagram.PLOT
		"polarchart", "polar" -> Diagram.POLAR_CHART
		else -> {
			logger.error { "Unknown diagram type" }
			return null
		}
	}
	return when (args.size) {
		2 -> CommandLine(args[1], "Diagram.png", type)
		else -> CommandLine(args[1], args[2], type)
	}
}


fun getChartCell(line: List<String>): ChartCell? = when {
	line.size != 3 -> {
		logger.error {"3 arguments expected" }
		null
	}
	line[1].toFloatOrNull() == null -> {
		logger.error { "Float expected" }
		null
	}
	else -> ChartCell(line[1].toFloat(), line[0], line[2])
}

fun getPlotCell(line: List<String>): PlotCell? = when {
	line.size != 4 -> {
		logger.error { "4 arguments expected" }
		null
	}
	line[0].toFloatOrNull() == null || line[1].toFloatOrNull() == null -> {
		logger.error { "Float expected" }
		null
	}
	else -> PlotCell(line[0].toFloat(), line[1].toFloat(), line[2], line[3])
}

fun getCell(type: Diagram, line: List<String>) = when (type) {
	Diagram.CIRCLE, Diagram.BAR_CHART, Diagram.POLAR_CHART -> getChartCell(line)
	Diagram.PLOT -> getPlotCell(line)
}

fun readInputFile(inputFile: String): List<List<String>>? {
	if (!File(inputFile).exists()) {
		logger.error { "file $inputFile does not exist" }
		return null
	}
	return File(inputFile).readLines().map {
		parseFileLine(it) ?: return null
	}
}

fun getCharForSeparate(text: String): Char? {
	val set = text.toSet()
	for (ch in Char.MIN_VALUE..Char.MAX_VALUE)
		if (ch !in set)
			return ch
	// if we are here text unlikely has any sense
	logger.error { "Incorrect line in input file: $text" }
	return null
}

fun parseFileLine(line: String): List<String>? {
	val separator = ','
	val newSeparator = getCharForSeparate(line) ?: return null
	val quotes = '"'
	var isQuote = false
	val lineWithNewSeparator = line.withIndex().map {
		if (it.value == quotes)
			isQuote = !isQuote
		when {
			isQuote || it.value != separator -> it.value
			else -> newSeparator
		}
	}.joinToString(separator = "")
	return lineWithNewSeparator.split(newSeparator)
}