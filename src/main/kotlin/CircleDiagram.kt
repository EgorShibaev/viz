import org.jetbrains.skija.Canvas
import kotlin.math.cos
import kotlin.math.sin

private data class DiagramSegment(val beginAngle: Float, val endAngle: Float, val text: String) {
	fun draw(canvas: Canvas, centerX: Float, centerY: Float, r: Float) {
		val sweepAngle = endAngle - beginAngle
		drawArcInCircle(canvas, centerX, centerY, r, beginAngle, sweepAngle)
		val angleForText = (beginAngle + endAngle) / 2
		val yForText = centerY - (r + 15F) * cos(angleForText)
		val xForText = if (sin(angleForText) > 0)
			centerX + (r + 15F) * sin(angleForText)
		else
			centerX + (r + 15F) * sin(angleForText) - getTextWidth(text, font)
		canvas.drawString(text, xForText, yForText, font, paint)
	}
}

fun drawArcInCircle(canvas: Canvas, centerX: Float, centerY: Float, r: Float, beginAngle: Float, sweepAngle: Float) {
	canvas.drawArc(
		centerX - r, centerY - r, centerX + r, centerY + r,
		toDegree(beginAngle) - 90F, toDegree(sweepAngle), true, randomColor(sweepAngle)
	)
}

fun separatedCircle(canvas: Canvas, centerX: Float, centerY: Float, r: Float, content: List<ChartCell>) {
	assert(content.all { it.value >= 0 })
	val sum = content.sumOf { it.value.toDouble() }.toFloat()
	var angle = 0F
	val diagramSegments = content.map {
		val sweepAngle = it.value / sum * Math.PI.toFloat() * 2
		val text = "${it.text} - ${it.value} (${(it.value / sum * 100).toInt()}%)"
		angle += sweepAngle
		DiagramSegment(angle - sweepAngle, angle, text)
	}
	diagramSegments.forEach {
		it.draw(canvas, centerX, centerY, r)
		lineInCircle(canvas, centerX, centerY, r, it.endAngle)
	}
	canvas.drawCircle(centerX, centerY, r, stroke)
}

private fun toDegree(angleInRadian: Float) = angleInRadian / Math.PI.toFloat() * 180F

private fun lineInCircle(canvas: Canvas, centerX: Float, centerY: Float, length: Float, angle: Float) {
	canvas.drawLine(
		centerX, centerY,
		centerX + length * sin(angle),
		centerY - length * cos(angle),
		stroke
	)
}