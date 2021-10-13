package diagram

import ChartCell
import distance
import drawByRightSide
import drawStringInRect
import fontsAndPaints.*
import getNearestRoundNumber
import org.jetbrains.skija.*
import kotlin.math.cos
import kotlin.math.sin

fun polarChart(canvas: Canvas, centerX: Float, centerY: Float, radius: Float, content: List<ChartCell>) {
	val stepValue = getNearestRoundNumber(content.maxOf { it.value } / 11)
	val stepNumber = 10
	drawGrid(canvas, centerX, centerY, radius, content.size, stepNumber, stepValue)
	drawCaption(canvas, centerX, centerY, radius, content)
	drawValue(canvas, centerX, centerY, radius, content, stepValue * stepNumber)
}

fun drawCaption(canvas: Canvas, centerX: Float, centerY: Float, radius: Float, content: List<ChartCell>) {
	getPolygon(centerX, centerY, radius + 10f, content.size).withIndex().forEach {
		val (x, y) = it.value
		val text = content[it.index].name
		if (x > centerX)
			canvas.drawString(text, x, y, font, paint)
		else
			drawByRightSide(canvas, text, x, y, font, paint)
	}
}

data class ScreenCell(val x: Float, val y: Float, val cell: ChartCell)

private fun getScreenCells(content: List<ChartCell>, radius: Float, max: Float, centerX: Float, centerY: Float) =
	content.withIndex().map {
		val chartCell = it.value
		val currRadius = radius * chartCell.value / max
		// calculate angle and rotate it
		val angle = Math.PI.toFloat() * 2 / content.size * it.index - Math.PI.toFloat() / 2
		val x = centerX + cos(angle) * currRadius
		val y = centerY + sin(angle) * currRadius
		ScreenCell(x, y, chartCell)
	}

/**
 * In this function segments between points are drawn.
 * For each pair program draw segment between them and filled triangle.
 * */
private fun drawPolygon(canvas: Canvas, centerX: Float, centerY: Float, screenCells: List<ScreenCell>) {
	val screenCellsPairs = (screenCells + screenCells[0]).windowed(2, 1, false)
	screenCellsPairs.forEach { (currCell, nextCell) ->
		val coordinates =
			arrayOf(Point(centerX, centerY), Point(currCell.x, currCell.y), Point(nextCell.x, nextCell.y))
		canvas.drawTriangles(coordinates, null, Paint().apply {
			color = 0x9fcfcfff.toInt()
			mode = PaintMode.FILL
		})
		canvas.drawLine(currCell.x, currCell.y, nextCell.x, nextCell.y, stroke)
	}
}

fun drawValue(
	canvas: Canvas, centerX: Float, centerY: Float, radius: Float, content: List<ChartCell>, max: Float
) {
	val screenCells = getScreenCells(content, radius, max, centerX, centerY)
	drawPolygon(canvas, centerX, centerY, screenCells)
	screenCells.forEach {
		canvas.drawCircle(it.x, it.y, 5f, randomColor(it.cell.value))
		if (distance(it.x, it.y, State.mouseX, State.mouseY) <= 5f) {
			// if user points to this point program circle it and write additional info
			canvas.drawCircle(it.x, it.y, 7f, Paint().apply {
				color = 0xff0000ff.toInt()
				mode = PaintMode.STROKE
				strokeWidth = 2f
			})
			val text = "${it.cell.value} - ${it.cell.detailedInfo}"
			drawStringInRect(
				canvas, text,
				Rect(it.x + 5f, it.y - 5f - font.size, centerX + radius, centerY + radius), font
			)
		}
	}
}

fun drawGrid(
	canvas: Canvas, centerX: Float, centerY: Float, radius: Float, unitNumber: Int, stepNumber: Int, minValue: Float
) {
	getPolygon(centerX, centerY, radius, unitNumber).forEach {
		canvas.drawLine(centerX, centerY, it.first, it.second, thinStroke)
	}
	for (step in 1..stepNumber) {
		val currRadius = radius / stepNumber * step
		val coordinates = getPolygon(centerX, centerY, currRadius, unitNumber).flatMap { it.toList() }.toFloatArray()
		canvas.drawPolygon(coordinates + coordinates[0] + coordinates[1], thinStroke)
		canvas.drawString((minValue * step).toString(), centerX, centerY - currRadius, thinFont, paint)
	}
}

/**
 * This function return regular polygon with center in (centerX, centerY).
 * unitNumber is count of sides.
 * */
fun getPolygon(centerX: Float, centerY: Float, radius: Float, unitNumber: Int) =
	(0 until unitNumber).map {
		val angle = Math.PI.toFloat() * 2 / unitNumber * it - Math.PI.toFloat() / 2
		val x = centerX + cos(angle) * radius
		val y = centerY + sin(angle) * radius
		Pair(x, y)
	}
