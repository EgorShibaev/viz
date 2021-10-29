package diagram

import ChartCell
import drawStringInRect
import fontsAndPaints.*
import getRoundNumberMore
import getTextWidth
import org.jetbrains.skija.Canvas
import org.jetbrains.skija.Rect

fun barChart(canvas: Canvas, rect: Rect, content: List<ChartCell>) {
	assert(content.all { it.value >= 0 })
	font = getFont(rect.width)
	val max = getRoundNumberMore(content.maxOf { it.value })
	val leftIndent = 80f
	// widthOfColumn * cnt + widthOfColumn / 2 * (cnt - 1) = width
	val widthOfColumn = (rect.right - (rect.left + leftIndent)) / (1.5F * content.size - 0.5F)
	val factor = (rect.bottom - font.size - 5F - (rect.top + font.size + 5F)) / max
	verticalLines(canvas, max, rect, factor)
	drawColumns(canvas, Rect(rect.left + leftIndent, rect.top, rect.right, rect.bottom), widthOfColumn, factor, content)
}

private fun verticalLines(canvas: Canvas, max: Float, rect: Rect, factor: Float) {
	val step = max / 10f
	(0..10).forEach {
		canvas.drawLine(
			rect.left,
			rect.bottom - font.size - 5F - it * step * factor,
			rect.right,
			rect.bottom - font.size - 5F - it * step * factor,
			thinStroke
		)
		canvas.drawString(
			"%.2f".format(it * step),
			rect.left,
			rect.bottom - font.size - 8F - it * step * factor,
			font,
			paint
		)
	}
}

data class Column(val rect: Rect, val name: String, val value: Float, val info: String) {
	fun draw(canvas: Canvas) {
		canvas.drawRect(rect, randomColor(value))
		canvas.drawRect(rect, stroke)
		canvas.drawString(
			name, (rect.right + rect.left) / 2 - getTextWidth(name, font) / 2F, rect.bottom + font.size, font, paint
		)
		canvas.drawString(
			value.toString(), (rect.right + rect.left) / 2 - getTextWidth(value.toString(), font) / 2F,
			rect.bottom - 3f, font, paint
		)
	}
}

private fun drawColumns(
	canvas: Canvas, rect: Rect, widthOfColumn: Float, factor: Float, content: List<ChartCell>
) {
	var currX = rect.left
	val columns = content.map {
		val columnRect = Rect(
			currX,
			rect.bottom - font.size - 5F - factor * it.value,
			currX + widthOfColumn,
			rect.bottom - font.size - 5F
		)
		currX += widthOfColumn * 1.5f
		Column(columnRect, it.name, it.value, it.detailedInfo)
	}
	columns.forEach {
		it.draw(canvas)
	}
	columns.forEach {
		if (checkInRect(it.rect, State.mouseX, State.mouseY))
			drawStringInRect(canvas, it.info, Rect(State.mouseX, State.mouseY + 10f, rect.right, rect.bottom), font)
	}
}

private fun checkInRect(rect: Rect, x: Float, y: Float) = rect.run {
	x in left..right && y in top..bottom
}