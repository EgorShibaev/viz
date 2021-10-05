import org.jetbrains.skija.Canvas
import org.jetbrains.skija.Font
import org.jetbrains.skija.Paint
import org.jetbrains.skija.PaintMode
import kotlin.math.*

private var x0 = -10f
private var y0 = -10f
private var x1 = 10f
private var y1 = 10f

enum class PlotMode {
	WITH_SEGMENTS, USUAL
}

fun convertPlotX(plotX: Float, left: Float, right: Float) = left + (plotX - x0) / (x1 - x0) * (right - left)

fun convertPlotY(plotY: Float, top: Float, bottom: Float) = bottom - (plotY - y0) / (y1 - y0) * (bottom - top)

fun convertScreenX(screenX: Float, left: Float, right: Float) = x0 + (screenX - left) / (right - left) * (x1 - x0)

fun convertScreenY(screenY: Float, top: Float, bottom: Float) = y1 - (screenY - top) / (bottom - top) * (y1 - y0)

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

private fun changeZoom(
	preciseWheelRotation: Float,
	left: Float,
	top: Float,
	right: Float,
	bottom: Float
) {
	val x = convertScreenX(State.mouseX, left, right)
	val y = convertScreenY(State.mouseY, top, bottom)
	assert(x >= 0 && y >= 0)
	val factor = if (preciseWheelRotation == 1f) 0.95f else 1f / 0.95f
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
	thinStroke: Paint,
	plotMode: PlotMode
) {
	State.e?.let {
		changeZoom(it.preciseWheelRotation.toFloat(), left, top, right, bottom)
	}
	State.e = null
	State.pressedKeyCode?.let {
		precessKey(it)
	}
	State.pressedKeyCode = null
	drawNet(canvas, getNearestRoundNumber((x1 - x0) / 10), left, top, right, bottom, paint, thinStroke, thinFont)
	if (0f in x0..x1) {
		val centerX = convertPlotX(0f, left, right)
		drawArrow(canvas, centerX, bottom, centerX, top, paint)
	}
	if (0f in y0..y1) {
		val centerY = convertPlotY(0f, top, bottom)
		drawArrow(canvas, left, centerY, right, centerY, paint)
	}
	if (plotMode == PlotMode.WITH_SEGMENTS)
		drawSegments(canvas, content, left, top, right, bottom)
	drawPoints(canvas, left, top, right, bottom, content, font, paint)
}

private fun drawSegments(
	canvas: Canvas,
	content: List<PlotCell>,
	left: Float,
	top: Float,
	right: Float,
	bottom: Float,
) {
	val sortedPoints = content.sorted()
	for (pointIndex in 0 until sortedPoints.size - 1) {
		canvas.drawLine(
			convertPlotX(sortedPoints[pointIndex].x, left, right),
			convertPlotY(sortedPoints[pointIndex].y, top, bottom),
			convertPlotX(sortedPoints[pointIndex + 1].x, left, right),
			convertPlotY(sortedPoints[pointIndex + 1].y, top, bottom),
			Paint().apply {
				color = 0X6f0000a0
				strokeWidth = 1.5f
			}
		)
	}

}

private fun drawPoints(
	canvas: Canvas,
	left: Float,
	top: Float,
	right: Float,
	bottom: Float,
	content: List<PlotCell>,
	font: Font,
	paint: Paint
) {
	content.filter { it.x in x0..x1 && it.y in y0..y1 }.forEach {
		val x = convertPlotX(it.x, left, right)
		val y = convertPlotY(it.y, top, bottom)
		val radius = 5f
		canvas.drawCircle(x, y, radius, randomColor(it.x * it.y))
		if (abs(State.mouseX - x).pow(2) + abs(State.mouseY - y).pow(2) <= radius.pow(2)) {
			canvas.drawCircle(x, y, radius + 2, Paint().apply {
				color = 0xff0000ff.toInt()
				mode = PaintMode.STROKE
				strokeWidth = 2f
			})
			canvas.drawString("${it.text} - (${it.x}; ${it.y})", x + 5f, y - 5f, font, paint)
		} else
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
		if (step * vertical in x0..x1 && (vertical != 0 || 0f !in x0..x1 || (0f in x0..x1 && 0f in y0..y1))) {
			val screenX = convertPlotX(step * vertical, left, right)
			canvas.drawLine(screenX, top, screenX, bottom, thinStroke)
			when {
				0 < y0 -> canvas.drawString("${step * vertical}", screenX, bottom, thinFont, paint)
				0 > y1 -> canvas.drawString(
					"${step * vertical}", screenX, top + thinFont.size, thinFont, paint
				)
				else -> canvas.drawString(
					"${step * vertical}",
					screenX, convertPlotY(0f, top, bottom) - 1f, thinFont, paint
				)
			}
		}
	}
	for (horizontal in (y0 / step).toInt() - 2..(y1 / step).toInt() + 2) {
		if (horizontal * step in y0..y1 && (horizontal != 0 || 0F !in y0..y1)) {
			val screenY = convertPlotY(horizontal * step, top, bottom)
			canvas.drawLine(left, screenY, right, screenY, thinStroke)
			val inscription = (step * horizontal).toString()
			when {
				0 < x0 -> canvas.drawString(inscription, left, screenY, thinFont, paint)
				0 > x1 -> canvas.drawString(
					inscription, right - thinFont.size * 0.6f * inscription.length,
					screenY, thinFont, paint
				)
				else -> canvas.drawString(
					inscription, convertPlotX(0f, left, right) + 1f,
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