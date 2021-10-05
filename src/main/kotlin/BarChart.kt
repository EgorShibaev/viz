import org.jetbrains.skija.Canvas
import org.jetbrains.skija.Font
import org.jetbrains.skija.Paint
import org.jetbrains.skija.Rect

fun barChart(
	canvas: Canvas,
	left: Float,
	top: Float,
	right: Float,
	bottom: Float,
	content: List<ChartCell>,
	font: Font,
	stroke: Paint,
	thinStroke: Paint,
	paint: Paint
) {
	assert(content.all { it.value >= 0 })
	val max = content.maxOf { it.value }
	val leftIndent = 80f
	// widthOfColumn * cnt + widthOfColumn / 2 * (cnt - 1) = width
	val widthOfColumn = (right - (left + leftIndent)) / (1.5F * content.size - 0.5F)
	val factor = (bottom - font.size - 5F - (top + font.size + 5F)) / max
	verticalLines(canvas, max, left, right, bottom, factor, font, thinStroke, paint)
	drawColumns(canvas, left + leftIndent, bottom, widthOfColumn, factor, content, font, stroke, paint)
}

private fun verticalLines(
	canvas: Canvas,
	max: Float,
	left: Float,
	right: Float,
	bottom: Float,
	factor: Float,
	font: Font,
	thinStroke: Paint,
	paint: Paint
) {
	val step = max / 10f
	(0..10).forEach {
		canvas.drawLine(
			left,
			bottom - font.size - 5F - it * step * factor,
			right,
			bottom - font.size - 5F - it * step * factor,
			thinStroke
		)
		canvas.drawString("%.2f".format(it * step), left, bottom - font.size - 8F - it * step * factor, font, paint)

	}
}

private fun drawColumns(
	canvas: Canvas,
	left: Float,
	bottom: Float,
	widthOfColumn: Float,
	factor: Float,
	content: List<ChartCell>,
	font: Font,
	stroke: Paint,
	paint: Paint
) {
	var currX = left
	content.forEach {
		val value = it.value
		val text = it.text
		val rect = Rect(
			currX,
			bottom - font.size - 5F - factor * value,
			currX + widthOfColumn,
			bottom - font.size - 5F
		)
		canvas.drawRect(rect, randomColor(value))
		canvas.drawRect(rect, stroke)
		canvas.drawString(
			text, currX + widthOfColumn / 2 - text.length * font.size * 0.6F / 2F, bottom - 2F, font, paint
		)
		if (checkInRect(rect, State.mouseX, State.mouseY))
			canvas.drawString(value.toString(), State.mouseX, State.mouseY, font, paint)
		currX += 1.5F * widthOfColumn
	}
}

private fun checkInRect(rect: Rect, x: Float, y: Float) = rect.run {
	x in left..right && y in top..bottom
}