import org.jetbrains.skija.*
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import kotlin.io.path.Path

fun getContentFromFile(args: Array<String>): Pair<Diagram, List<Cell>>? {
	val commandLine = processCommandLine(args) ?: return null
	val content = readInputFile(commandLine.inputFile) ?: return null
	return Pair(
		commandLine.type,
		content.map {
			getCell(commandLine.type, it) ?: return null
		}
	)
}

fun getCell(type: Diagram, line: List<String>): Cell? {
	when (type) {
		Diagram.CIRCLE, Diagram.BAR_CHART -> {
			when {
				line.size != 3 -> {
					println("3 arguments expected")
					return null
				}
				line[0].toFloatOrNull() == null ->  {
					println("Float expected")
					return null
				}
			}
			return ChartCell(line[0].toFloat(), line[1], line[2])
		}
		Diagram.PLOT -> {
			when {
				line.size != 4 -> {
					println("4 arguments expected")
					return null
				}
				line[0].toFloatOrNull() == null || line[1].toFloatOrNull() == null -> {
					println("Float expected")
					return null
				}
			}
			return PlotCell(line[0].toFloat(), line[1].toFloat(), line[2], line[3])
		}
	}
}

data class CommandLine(val inputFile: String, val outputFile: String, val type: Diagram)

fun processCommandLine(args: Array<String>): CommandLine? {
	if (args.size !in 2..3){
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
	if (!File(inputFile).exists()){
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

fun writeToFile(outputFIle: String, type: Diagram, content: List<Cell>) {
	val w = 1000
	val h = 1000
	val surface = Surface.makeRasterN32Premul(w, h)
	val canvas = surface.canvas
	canvas.drawRect(Rect(0f, 0f, w.toFloat(), h.toFloat()), Paint().apply { color = 0xffffffff.toInt() })
	drawDiagram(canvas, type, content, w.toFloat(), h.toFloat())
	val image : Image = surface.makeImageSnapshot()
	val pngData = image.encodeToData(EncodedImageFormat.PNG)!!
	val pngBytes = pngData.toByteBuffer()
	val path = Path(outputFIle)
	val channel = Files.newByteChannel(path,
		StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)
	channel.write(pngBytes)
	channel.close()
}