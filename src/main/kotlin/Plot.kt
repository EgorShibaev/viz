import org.jetbrains.skija.Canvas
import org.jetbrains.skija.Font
import org.jetbrains.skija.Paint
import kotlin.math.*

private var x0 = -10f
private var y0 = -10f
private var x1 = 10f
private var y1 = 10f

private fun precessKey(code: Int) {
	val step = (x1 - x0) / 30f
	when (code) {
		37 -> {
			x0 -= step
			x1 -= step
		}
		38 -> {
			y0 += step
			y1 += step
		}
		39 -> {
			x0 += step
			x1 += step
		}
		40 -> {
			y0 -= step
			y1 -= step
		}
	}
}

private fun changeZoom(preciseWheelRotation: Float, mouseX: Float, mouseY: Float, h: Float, w: Float) {
	val x = mouseX / h * (x1 - x0) + x0
	val y = (y1 - y0) - mouseY / w * (y1 - y0) + y0
	assert(x >= 0 && y >= 0)
	val factor = if (preciseWheelRotation == 1f) 0.98f else 1f / 0.98f
	x1 = x + (x1 - x) * factor
	y1 = y + (y1 - y) * factor
	x0 = x - (x - x0) * factor
	y0 = y - (y - y0) * factor

}

fun plot(
	canvas: Canvas,
	left: Float,
	top: Float,
	right: Float,
	bottom: Float,
	content: List<PlotCell>,
	font: Font,
	paint: Paint,
	thinFont: Font,
	thinStroke: Paint
) {
	State.e?.let {
		changeZoom(
			it.preciseWheelRotation.toFloat(),
			State.mouseX - left,
			State.mouseY - top,
			right - left,
			bottom - top
		)
	}
	State.e = null
	State.pressedKeyCode?.let {
		precessKey(it)
	}
	State.pressedKeyCode = null
	drawNet(canvas, getNearestRoundNumber((x1 - x0) / 10), left, top, right, bottom, paint, thinStroke, thinFont)
	if (0f in x0..x1) {
		val centerX = left + (-x0) / (x1 - x0) * (right - left)
		drawArrow(canvas, centerX, bottom, centerX, top, paint)
	}
	if (0f in y0..y1) {
		val centerY = bottom - (-y0) / (y1 - y0) * (bottom - top)
		drawArrow(canvas, left, centerY, right, centerY, paint)
	}
	content.filter { it.x in x0..x1 && it.y in y0..y1 }.forEach {
		val x = left + (it.x - x0) / (x1 - x0) * (right - left)
		val y = bottom - (it.y - y0) / (y1 - y0) * (bottom - top)
		canvas.drawCircle(x, y, 5f, paint)
		canvas.drawString(it.text, x + 5f, y - 5f, font, paint)
	}
}

private fun drawNet(
	canvas: Canvas,
	step: Float,
	left: Float,
	top: Float,
	right: Float,
	bottom: Float,
	paint: Paint,
	thinStroke: Paint,
	thinFont: Font
) {
	for (vertical in (x0 / step).toInt() - 2..(x1 / step).toInt() + 2) {
		if (step * vertical in x0..x1) {
			val screenX = left + (step * vertical - x0) / (x1 - x0) * (right - left)
			canvas.drawLine(screenX, top, screenX, bottom, thinStroke)
			when {
				0 < y0 -> canvas.drawString("${step * vertical}", screenX, bottom, thinFont, paint)
				0 > y1 -> canvas.drawString(
					"${step * vertical}", screenX, top + thinFont.size, thinFont, paint
				)
				else -> canvas.drawString(
					"${step * vertical}",
					screenX, bottom - (-y0) / (y1 - y0) * (bottom - top) - 2f, thinFont, paint
				)
			}
		}
	}
	for (horizontal in (y0 / step).toInt() - 2..(y1 / step).toInt() + 2) {
		if (horizontal * step in y0..y1 && (horizontal != 0 || 0F !in y0..y1)) {
			val screenY = bottom - (horizontal * step - y0) / (y1 - y0) * (bottom - top)
			canvas.drawLine(left, screenY, right, screenY, thinStroke)
			val inscription = (step * horizontal).toString()
			when {
				0 < x0 -> canvas.drawString(inscription, left, screenY, thinFont, paint)
				0 > x1 -> canvas.drawString(
					inscription, right - thinFont.size * 0.6f * inscription.length,
					screenY, thinFont, paint
				)
				else -> canvas.drawString(
					inscription, left + (-x0) / (x1 - x0) * (right - left),
					screenY, thinFont, paint
				)
			}
		}
	}
}

private fun drawArrow(canvas: Canvas, x0: Float, y0: Float, x1: Float, y1: Float, paint: Paint) {
	canvas.drawLine(x0, y0, x1, y1, paint)
	val len = 15f
	val angle = Math.PI.toFloat() / 6
	val vectorX = x1 - x0
	val vectorY = y1 - y0
	val vectorLen = sqrt(vectorX.pow(2) + vectorY.pow(2))
	val rotatedVectorX = (vectorX * cos(angle) - vectorY * sin(angle)) * len / vectorLen
	val rotatedVectorY = (vectorX * sin(angle) + vectorY * cos(angle)) * len / vectorLen
	canvas.drawLine(x1 - rotatedVectorX, y1 - rotatedVectorY, x1, y1, paint)
	val rotatedVectorX2 = (vectorX * cos(-angle) - vectorY * sin(-angle)) * len / vectorLen
	val rotatedVectorY2 = (vectorX * sin(-angle) + vectorY * cos(-angle)) * len / vectorLen
	canvas.drawLine(x1 - rotatedVectorX2, y1 - rotatedVectorY2, x1, y1, paint)
}

private fun getNearestRoundNumber(value: Float): Float {
	val roundNumbers = (-10..10).map { listOf(10f.pow(it), 10f.pow(it) * 2, 10f.pow(it) * 5) }.flatten()
	return roundNumbers.minByOrNull { abs(it - value) } ?: 1f
}