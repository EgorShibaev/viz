import org.jetbrains.skija.Canvas
import org.jetbrains.skija.Font
import org.jetbrains.skija.Paint
import org.jetbrains.skija.PaintMode
import kotlin.math.*

enum class PlotMode {
	WITH_SEGMENTS, USUAL
}

fun convertPlotX(plotX: Float, left: Float, right: Float) =
	left + (plotX - State.x0) / (State.x1 - State.x0) * (right - left)

fun convertPlotY(plotY: Float, top: Float, bottom: Float) =
	bottom - (plotY - State.y0) / (State.y1 - State.y0) * (bottom - top)

fun convertScreenX(screenX: Float, left: Float, right: Float) =
	State.x0 + (screenX - left) / (right - left) * (State.x1 - State.x0)

fun convertScreenY(screenY: Float, top: Float, bottom: Float) =
	State.y1 - (screenY - top) / (bottom - top) * (State.y1 - State.y0)

private fun precessKey(code: Int) {
	val step = (State.x1 - State.x0) / 30f
	when (code) {
		37 -> {
			State.x0 -= step
			State.x1 -= step
		}
		38 -> {
			State.y0 += step
			State.y1 += step
		}
		39 -> {
			State.x0 += step
			State.x1 += step
		}
		40 -> {
			State.y0 -= step
			State.y1 -= step
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
	State.x1 = x + (State.x1 - x) * factor
	State.y1 = y + (State.y1 - y) * factor
	State.x0 = x - (x - State.x0) * factor
	State.y0 = y - (y - State.y0) * factor

}

private fun initField(content: List<PlotCell>, left: Float, top: Float, right: Float, bottom: Float) {
	State.x0 = content.minOf { it.x } - 1
	State.x1 = content.maxOf { it.x } + 1
	State.y0 = content.minOf { it.y } - 1
	State.y1 = content.maxOf { it.y } + 1
	// height / (bottom - left) should be equal width / (right - left)
	val height = State.y1 - State.y0
	val width = State.x1 - State.x0
	when {
		height / (bottom - left) < width / (right - left) -> {
			val expectedHeight = width / (right - left) * (bottom - left)
			State.y0 -= (expectedHeight - height) / 2
			State.y1 + (expectedHeight - height) / 2
		}
		else -> {
			val expectedWidth = height / (bottom - top) * (right - left)
			State.x0 -= (expectedWidth - width) / 2
			State.x1 += (expectedWidth - width) / 2
		}
	}
}

private fun moveByMouse(left: Float, top: Float, right: Float, bottom: Float) {
	val deltaX = -State.vectorToMoveX / (right - left) * (State.x1 - State.x0)
	val deltaY = State.vectorToMoveY / (bottom - top) * (State.y1 - State.y0)
	State.vectorToMoveX = 0f
	State.vectorToMoveY = 0f
	State.x0 += deltaX
	State.y0 += deltaY
	State.x1 += deltaX
	State.y1 += deltaY
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
	moveByMouse(left, top, right, bottom)
	if (right - left != State.lastWidth || bottom - top != State.lastHeight) {
		initField(content, left, top, right, bottom)
		State.lastWidth = right - left
		State.lastHeight = bottom - top
	}
	State.e?.let {
		changeZoom(it.preciseWheelRotation.toFloat(), left, top, right, bottom)
	}
	State.e = null
	State.pressedKeyCode?.let {
		precessKey(it)
	}
	State.pressedKeyCode = null
	drawNet(
		canvas, getNearestRoundNumber((State.x1 - State.x0) / 10),
		left, top, right, bottom, paint, thinStroke, thinFont
	)
	if (0f in State.x0..State.x1) {
		val centerX = convertPlotX(0f, left, right)
		drawArrow(canvas, centerX, bottom, centerX, top, paint)
	}
	if (0f in State.y0..State.y1) {
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
	content.filter { it.x in State.x0..State.x1 && it.y in State.y0..State.y1 }.forEach {
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
	for (vertical in (State.x0 / step).toInt() - 2..(State.x1 / step).toInt() + 2) {
		if (step * vertical in State.x0..State.x1 &&
			(vertical != 0 || 0f !in State.x0..State.x1 || (0f in State.x0..State.x1 && 0f in State.y0..State.y1))
		) {
			val screenX = convertPlotX(step * vertical, left, right)
			canvas.drawLine(screenX, top, screenX, bottom, thinStroke)
			when {
				0 < State.y0 -> canvas.drawString("${step * vertical}", screenX, bottom, thinFont, paint)
				0 > State.y1 -> canvas.drawString(
					"${step * vertical}", screenX, top + thinFont.size, thinFont, paint
				)
				else -> canvas.drawString(
					"${step * vertical}",
					screenX, convertPlotY(0f, top, bottom) - 1f, thinFont, paint
				)
			}
		}
	}
	for (horizontal in (State.y0 / step).toInt() - 2..(State.y1 / step).toInt() + 2) {
		if (horizontal * step in State.y0..State.y1 && (horizontal != 0 || 0F !in State.y0..State.y1)) {
			val screenY = convertPlotY(horizontal * step, top, bottom)
			canvas.drawLine(left, screenY, right, screenY, thinStroke)
			val inscription = (step * horizontal).toString()
			when {
				0 < State.x0 -> canvas.drawString(inscription, left, screenY, thinFont, paint)
				0 > State.x1 -> canvas.drawString(
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