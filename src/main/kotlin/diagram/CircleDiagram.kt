package diagram

import ChartCell
import State
import distance
import drawStringInRect
import fontsAndPaints.*
import getTextWidth
import org.jetbrains.skija.Canvas
import org.jetbrains.skija.Rect
import kotlin.math.*

data class DiagramSegment(
	val beginAngle: Float, val endAngle: Float, val text: String,
	val centerX: Float, val centerY: Float, val r: Float, val info: String
) {
	fun draw(canvas: Canvas) {
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

	/**
	 * calculate angle relatively center of diagram
	 * */
	fun getAngle(x: Float, y: Float): Float {
		// return angle in range [0; 2 * pi) which equals to argument
		fun angleToDefaultRange(angle: Float): Float {
			var res = angle
			while (res < 0)
				res += PI.toFloat() * 2
			while (res >= PI.toFloat() * 2)
				res -= PI.toFloat() * 2
			return res
		}

		val vectorX = x - centerX
		val vectorY = y - centerY
		val vectorSin = vectorY / distance(0f, 0f, vectorX, vectorY)
		val vectorCos = vectorX / distance(0f, 0f, vectorX, vectorY)
		// + pi / 2 because program should start draw at top.
		val angle = if (vectorCos < 0)
			PI.toFloat() - asin(vectorSin) + PI.toFloat() / 2
		else
			asin(vectorSin) + PI.toFloat() / 2
		return angleToDefaultRange(angle)
	}

	fun checkInSegment(x: Float, y: Float): Boolean {
		if (distance(x, y, centerX, centerY) > r)
			return false
		return getAngle(x, y) in beginAngle..endAngle
	}
}

fun separatedCircle(canvas: Canvas, centerX: Float, centerY: Float, r: Float, content: List<ChartCell>) {
	assert(content.all { it.value >= 0 })
	val diagramSegments = getDiagramSegments(centerX, centerY, r, content)
	diagramSegments.forEach {
		it.draw(canvas)
		lineInCircle(canvas, centerX, centerY, r, it.endAngle)
	}
	diagramSegments.forEach {
		if (it.checkInSegment(State.mouseX, State.mouseY))
			drawStringInRect(canvas, it.info, Rect(State.mouseX, State.mouseY + 10f, centerX + r, centerY + r), font)
	}
	canvas.drawCircle(centerX, centerY, r, stroke)
}

/**
 * This function for each segment calculate begin and end angles and create DiagramSegment
 * */
fun getDiagramSegments(centerX: Float, centerY: Float, radius: Float, content: List<ChartCell>): List<DiagramSegment> {
	val sum = content.sumOf { it.value.toDouble() }.toFloat()
	var angle = 0F
	return content.map {
		val sweepAngle = it.value / sum * Math.PI.toFloat() * 2
		val text = "${it.name} - ${it.value} (${(it.value / sum * 100).toInt()}%)"
		angle += sweepAngle
		DiagramSegment(angle - sweepAngle, angle, text, centerX, centerY, radius, it.detailedInfo)
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

fun drawArcInCircle(canvas: Canvas, centerX: Float, centerY: Float, r: Float, beginAngle: Float, sweepAngle: Float) {
	canvas.drawArc(
		centerX - r, centerY - r, centerX + r, centerY + r,
		toDegree(beginAngle) - 90F, toDegree(sweepAngle), true, randomColor(sweepAngle)
	)
}