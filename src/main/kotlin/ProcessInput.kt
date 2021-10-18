import java.io.File

fun getContentFromFile(commandLine: CommandLine): Pair<Diagram, List<Cell>>? {
	val content = readInputFile(commandLine.inputFile) ?: return null
	var processedContent = content.map { getCell(commandLine.type, it) ?: return null }
	if (commandLine.type == Diagram.TREE) {
		processedContent = transformTreeCells(processedContent.map { it as TreeCell }) ?: return null
		if (processedContent.size != 1) {
			logger.error { "Tree expected" }
			return null
		}
	}
	return Pair(
		commandLine.type,
		if (commandLine.type == Diagram.TREE)
			processedContent
		else
			processedContent
	)
}


data class CommandLine(val inputFile: String, val outputFile: String, val type: Diagram)

fun processCommandLine(args: Array<String>): CommandLine? {
	if (args.size !in 2..3) {
		logger.error { "Three or two arguments expected" }
		return null
	}
	val type = when (args[0].lowercase()) {
		"tree" -> Diagram.TREE
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
		logger.error { "3 arguments expected" }
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

fun getTreeCell(line: List<String>): TreeCell? = when {
	line.size < 2 -> {
		logger.error { "At least 2 arguments expected is line: $line" }
		null
	}
	else -> TreeCell(
		line.subList(2, line.size).map { name ->
			TreeCell(emptyList(), name, "")
		}, line[0], line[1]
	)
}

fun getCell(type: Diagram, line: List<String>) = when (type) {
	Diagram.CIRCLE, Diagram.BAR_CHART, Diagram.POLAR_CHART -> getChartCell(line)
	Diagram.PLOT -> getPlotCell(line)
	Diagram.TREE -> getTreeCell(line)
}

fun haveDuplicates(strings: List<String>) = strings.map { it.hashCode() }.sorted().windowed(2).any { it[0] == it[1] }

fun transformTreeCells(cells: List<TreeCell>): List<TreeCell>? {
	if (haveDuplicates(cells.map { it.name })) {
		logger.error { "Name must be different" }
		return null
	}
	if (cells.isEmpty())
		return emptyList()
	// all nodes that aren't children og an node
	val roots = cells.filter { node -> cells.all { it.children.all { child -> child.name != node.name } } }
	if (roots.isEmpty()) {
		logger.error { "content must me tree" }
		return null
	}
	// { all } \ { roots }
	val below = transformTreeCells(cells.filter { roots.all { node -> node.name != it.name } }) ?: return null
	// if some nodes don't have parent or have two parents
	if (below.any { node -> roots.count { root -> root.children.any { child -> child.name == node.name } } != 1 }) {
		logger.error { "content must me tree" }
		return null
	}
	// if some root does not have children
	if (roots.any { root -> root.children.any { child -> below.all { it.name != child.name } } }) {
		logger.error { "content must me tree" }
		return null
	}
	return roots.map {
		TreeCell(it.children.map { below.find { node -> node.name == it.name }!! }, it.name, it.detailedInfo)
	}
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

/**
 * This function split line by commas, but program ignore commas in quotes.
 * Firstly, program find all commas out of quotes and then change it to a new separator.
 * New separator is char that not contain in string. Finally, program split line by new separator.
 * */
fun parseFileLine(line: String): List<String>? {
	val separator = ','
	val newSeparator = getCharForSeparate(line) ?: return null
	val quotes = setOf('"', '\'')
	var isQuote = false
	val lineWithNewSeparator = line.withIndex().map {
		if (it.value in quotes)
			isQuote = !isQuote
		when {
			isQuote || it.value != separator -> it.value
			else -> newSeparator
		}
	}.joinToString(separator = "")
	return lineWithNewSeparator.split(newSeparator)
}