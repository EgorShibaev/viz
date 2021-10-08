import org.jetbrains.skija.*
import kotlin.math.*

enum class PlotMode {
	WITH_SEGMENTS, USUAL
}

fun convertPlotX(plotX: Float, rect: Rect) =
	rect.left + (plotX - State.x0) / (State.x1 - State.x0) * (rect.right - rect.left)

fun convertPlotY(plotY: Float, rect: Rect) =
	rect.bottom - (plotY - State.y0) / (State.y1 - State.y0) * (rect.bottom - rect.top)

fun convertScreenX(screenX: Float, rect: Rect) =
	State.x0 + (screenX - rect.left) / (rect.right - rect.left) * (State.x1 - State.x0)

fun convertScreenY(screenY: Float, rect: Rect) =
	State.y1 - (screenY - rect.top) / (rect.bottom - rect.top) * (State.y1 - State.y0)


private fun updateField(content: List<PlotCell>, rect: Rect) {
	if (rect.right - rect.left != State.lastWidth || rect.bottom - rect.top != State.lastHeight) {
		State.x0 = content.minOf { it.x } - 1
		State.x1 = content.maxOf { it.x } + 1
		State.y0 = content.minOf { it.y } - 1
		State.y1 = content.maxOf { it.y } + 1
		// height / (bottom - left) should be equal width / (right - left)
		val height = State.y1 - State.y0
		val width = State.x1 - State.x0
		when {
			height / (rect.bottom - rect.left) < width / (rect.right - rect.left) -> {
				val expectedHeight = width / (rect.right - rect.left) * (rect.bottom - rect.left)
				State.y0 -= (expectedHeight - height) / 2
				State.y1 + (expectedHeight - height) / 2
			}
			else -> {
				val expectedWidth = height / (rect.bottom - rect.top) * (rect.right - rect.left)
				State.x0 -= (expectedWidth - width) / 2
				State.x1 += (expectedWidth - width) / 2
			}
		}
		State.lastWidth = rect.right - rect.left
		State.lastHeight = rect.bottom - rect.top
	}
}

private fun moveByMouse(rect: Rect) {
	val deltaX = -State.vectorToMoveX / (rect.right - rect.left) * (State.x1 - State.x0)
	val deltaY = State.vectorToMoveY / (rect.bottom - rect.top) * (State.y1 - State.y0)
	State.vectorToMoveX = 0f
	State.vectorToMoveY = 0f
	State.x0 += deltaX
	State.y0 += deltaY
	State.x1 += deltaX
	State.y1 += deltaY
}

fun plot(canvas: Canvas, rect: Rect, content: List<PlotCell>, plotMode: PlotMode) {
	moveByMouse(rect)
	updateField(content, rect)
	processWheelRotation(rect)
	processPressedKey()
	val screenStep = 100f
	val plotStep = getNearestRoundNumber(screenStep / (rect.right - rect.left) * (State.x1 - State.x0))
	drawGrid(canvas, plotStep, rect)
	drawCoordinateAxes(rect, canvas)
	if (plotMode == PlotMode.WITH_SEGMENTS)
		drawSegments(canvas, content, rect)
	drawPoints(canvas, rect, content)
}

private fun processPressedKey() {
	State.pressedKeyCode?.let {
		precessKeyCode(it)
	}
	State.pressedKeyCode = null
}

private fun precessKeyCode(code: Int) {
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

private fun processWheelRotation(rect: Rect) {
	State.e?.let {
		changeZoom(it.preciseWheelRotation.toFloat(), rect)
	}
	State.e = null
}

private fun changeZoom(preciseWheelRotation: Float, rect: Rect) {
	val x = convertScreenX(State.mouseX, rect)
	val y = convertScreenY(State.mouseY, rect)
	assert(x >= 0 && y >= 0)
	val zoomFactor = 0.95f
	val currentFactor = if (preciseWheelRotation == 1f) zoomFactor else 1f / zoomFactor
	State.x1 = x + (State.x1 - x) * currentFactor
	State.y1 = y + (State.y1 - y) * currentFactor
	State.x0 = x - (x - State.x0) * currentFactor
	State.y0 = y - (y - State.y0) * currentFactor
}


private fun drawSegments(canvas: Canvas, content: List<PlotCell>, rect: Rect) {
	val sortedPoints = content.sorted()
	for (pointIndex in 0 until sortedPoints.size - 1) {
		canvas.drawLine(
			convertPlotX(sortedPoints[pointIndex].x, rect),
			convertPlotY(sortedPoints[pointIndex].y, rect),
			convertPlotX(sortedPoints[pointIndex + 1].x, rect),
			convertPlotY(sortedPoints[pointIndex + 1].y, rect),
			Paint().apply {
				color = 0X6f0000a0
				strokeWidth = 1.5f
			}
		)
	}
}

private fun drawPoints(canvas: Canvas, rect: Rect, content: List<PlotCell>) {
	fun drawCaption(x: Float, y: Float, radius: Float, point: PlotCell) {
		if (distance(x, y, State.mouseX, State.mouseY) <= radius) {
			canvas.drawCircle(x, y, radius + 2, Paint().apply {
				color = 0xff0000ff.toInt()
				mode = PaintMode.STROKE
				strokeWidth = 2f
			})
			val text = "${point.name} - (${point.x}; ${point.y}) - ${point.detailedInfo}"
			drawStringInRect(canvas, text, Rect(x + 5f, y - 5f - font.size, rect.right, rect.bottom), font)
		}
	}

	val radius = 5f
	content.filter { it.x in State.x0..State.x1 && it.y in State.y0..State.y1 }.forEach {
		val x = convertPlotX(it.x, rect)
		val y = convertPlotY(it.y, rect)
		canvas.drawCircle(x, y, radius, randomColor(it.x * it.y))
		canvas.drawString(it.name, x + 5f, y - 5f, font, paint)
	}
	content.forEach {
		drawCaption(convertPlotX(it.x, rect),  convertPlotY(it.y, rect), radius, it)
	}
}


private fun drawGrid(canvas: Canvas, step: Float, rect: Rect) {
	drawVerticallyGrid(canvas, rect, step)
	drawHorizontallyGrid(canvas, step, rect)
}

private fun drawHorizontallyGrid(canvas: Canvas, step: Float, rect: Rect) {
	val startY = ceil(State.y0 / step).toInt()
	val finishY = floor(State.y1 / step).toInt()
	for (horizontal in startY..finishY) {
		val screenY = convertPlotY(horizontal * step, rect)
		canvas.drawLine(rect.left, screenY, rect.right, screenY, thinStroke)
		val inscription = (step * horizontal).toString()
		when {
			0 < State.x0 -> canvas.drawString(inscription, rect.left, screenY, thinFont, paint)
			0 > State.x1 -> drawWithByRight(canvas, inscription, rect.right, screenY, font, paint)
			else -> canvas.drawString(inscription, convertPlotX(0f, rect) + 1f, screenY, thinFont, paint)
		}
	}
}

private fun drawVerticallyGrid(canvas: Canvas, rect: Rect, step: Float) {
	val startX = ceil(State.x0 / step).toInt()
	val finishX = floor(State.x1 / step).toInt()
	for (vertical in startX..finishX) {
		val screenX = convertPlotX(step * vertical, rect)
		canvas.drawLine(screenX, rect.top, screenX, rect.bottom, thinStroke)
		val inscription = (step * vertical).toString()
		when {
			0 < State.y0 -> canvas.drawString(inscription, screenX, rect.bottom, thinFont, paint)
			0 > State.y1 -> canvas.drawString(inscription, screenX, rect.top + thinFont.size, thinFont, paint)
			else -> canvas.drawString(inscription, screenX, convertPlotY(0f, rect) - 1f, thinFont, paint)
		}
	}
}

private fun drawCoordinateAxes(rect: Rect, canvas: Canvas) {
	if (0f in State.x0..State.x1) {
		val centerX = convertPlotX(0f, rect)
		drawArrow(canvas, centerX, rect.bottom, centerX, rect.top)
	}
	if (0f in State.y0..State.y1) {
		val centerY = convertPlotY(0f, rect)
		drawArrow(canvas, rect.left, centerY, rect.right, centerY)
	}
}

private fun drawArrow(canvas: Canvas, x0: Float, y0: Float, x1: Float, y1: Float) {
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