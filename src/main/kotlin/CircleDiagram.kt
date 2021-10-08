import org.jetbrains.skija.Canvas
import org.jetbrains.skija.Font
import org.jetbrains.skija.Paint
import kotlin.math.cos
import kotlin.math.sin

fun separatedCircle(canvas: Canvas, centerX: Float, centerY: Float, r: Float, content: List<ChartCell>) {
	assert(content.all { it.value >= 0 })
	val sum = content.sumOf { it.value.toDouble() }.toFloat()
	var angle = 0F
	lineInCircle(canvas, centerX, centerY, r, angle)
	content.forEach {
		val sweepAngle = it.value / sum * Math.PI.toFloat() * 2
		val text = "${it.text} - ${it.value} (${(it.value / sum * 100).toInt()}%)"
		val prevAngle = angle
		angle += sweepAngle
		canvas.drawArc(
			centerX - r, centerY - r, centerX + r, centerY + r,
			toDegree(prevAngle) - 90F, toDegree(sweepAngle), true, randomColor(it.value)
		)
		val angleForText = (prevAngle + angle) / 2
		val yForText = centerY - (r + 15F) * cos(angleForText)
		val xForText = if (sin(angleForText) > 0)
			centerX + (r + 15F) * sin(angleForText)
		else
			centerX + (r + 15F) * sin(angleForText) - getTextWidth(text, font)
		canvas.drawString(text, xForText, yForText, font, paint)
	}
	canvas.drawCircle(centerX, centerY, r, stroke)
	angle = 0F
	content.forEach {
		angle += it.value / sum * Math.PI.toFloat() * 2
		lineInCircle(canvas, centerX, centerY, r, angle)
	}
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