package diagram

import PlotCell
import State
import distance
import drawStringInRect
import drawByRightSide
import fontsAndPaints.*
import getNearestRoundNumber
import org.jetbrains.skija.*
import kotlin.math.*

/**
 * This object contains State of field: x0, y0, x1, y1 : coordinates of visible space
 * lastHeight, lastWidth - size of window in previous rendering
 */
private object PlotState {
	var lastHeight = 0f
	var lastWidth = 0f
	var x0 = -10f
	var y0 = -10f
	var x1 = 10f
	var y1 = 10f
}

private fun convertPlotX(plotX: Float, rect: Rect) =
	rect.left + (plotX - PlotState.x0) / (PlotState.x1 - PlotState.x0) * (rect.right - rect.left)

private fun convertPlotY(plotY: Float, rect: Rect) =
	rect.bottom - (plotY - PlotState.y0) / (PlotState.y1 - PlotState.y0) * (rect.bottom - rect.top)

private fun convertScreenX(screenX: Float, rect: Rect) =
	PlotState.x0 + (screenX - rect.left) / (rect.right - rect.left) * (PlotState.x1 - PlotState.x0)

private fun convertScreenY(screenY: Float, rect: Rect) =
	PlotState.y1 - (screenY - rect.top) / (rect.bottom - rect.top) * (PlotState.y1 - PlotState.y0)


/**
 * This function works when user change window size. Task of it function is to
 * change PlotState.x0/x1/y0/y1. Firstly, it assigned these parameters as min and max of values.
 * Then function stretches visible space so that cell grid is square.
 */
private fun updateField(content: List<PlotCell>, rect: Rect) {
	if (rect.right - rect.left != PlotState.lastWidth || rect.bottom - rect.top != PlotState.lastHeight) {
		PlotState.x0 = content.minOf { it.x } - 1
		PlotState.x1 = content.maxOf { it.x } + 1
		PlotState.y0 = content.minOf { it.y } - 1
		PlotState.y1 = content.maxOf { it.y } + 1
		// height / (bottom - left) should be equal width / (right - left)
		val height = PlotState.y1 - PlotState.y0
		val width = PlotState.x1 - PlotState.x0
		when {
			height / (rect.bottom - rect.left) < width / (rect.right - rect.left) -> {
				val expectedHeight = width / (rect.right - rect.left) * (rect.bottom - rect.left)
				PlotState.y0 -= (expectedHeight - height) / 2
				PlotState.y1 + (expectedHeight - height) / 2
			}
			else -> {
				val expectedWidth = height / (rect.bottom - rect.top) * (rect.right - rect.left)
				PlotState.x0 -= (expectedWidth - width) / 2
				PlotState.x1 += (expectedWidth - width) / 2
			}
		}
		PlotState.lastWidth = rect.right - rect.left
		PlotState.lastHeight = rect.bottom - rect.top
	}
}

/**
 * MyMouseMotionAdapter change PlotState.vectorToMove when mouse is dragged.
 * This function process this changes.
 * */
private fun moveByMouse(rect: Rect) {
	val deltaX = -State.vectorToMoveX / (rect.right - rect.left) * (PlotState.x1 - PlotState.x0)
	val deltaY = State.vectorToMoveY / (rect.bottom - rect.top) * (PlotState.y1 - PlotState.y0)
	State.vectorToMoveX = 0f
	State.vectorToMoveY = 0f
	PlotState.x0 += deltaX
	PlotState.y0 += deltaY
	PlotState.x1 += deltaX
	PlotState.y1 += deltaY
}

fun plot(canvas: Canvas, rect: Rect, content: List<PlotCell>) {
	moveByMouse(rect)
	updateField(content, rect)
	processWheelRotation(rect)
	processPressedKey()
	val screenStep = 100f
	val plotStep = getNearestRoundNumber(screenStep / (rect.right - rect.left) * (PlotState.x1 - PlotState.x0))
	drawGrid(canvas, plotStep, rect)
	drawCoordinateAxes(rect, canvas)
	drawSegments(canvas, content, rect)
	drawPoints(canvas, rect, content)
}

private fun processPressedKey() {
	State.pressedKeyCode?.let {
		precessKeyCode(it)
	}
	State.pressedKeyCode = null
}

/**
 * Move visible space if user pres corresponding key.
 */
private fun precessKeyCode(code: Int) {
	val step = (PlotState.x1 - PlotState.x0) / 30f
	when (code) {
		37 -> {
			PlotState.x0 -= step
			PlotState.x1 -= step
		}
		38 -> {
			PlotState.y0 += step
			PlotState.y1 += step
		}
		39 -> {
			PlotState.x0 += step
			PlotState.x1 += step
		}
		40 -> {
			PlotState.y0 -= step
			PlotState.y1 -= step
		}
	}
}

/**
 * Scratch visible space if user twist mouse wheel
 */
private fun processWheelRotation(rect: Rect) {
	State.e?.let {
		changeZoom(it.preciseWheelRotation.toFloat(), rect)
	}
	State.e = null
}

private fun changeZoom(preciseWheelRotation: Float, rect: Rect) {
	val x = convertScreenX(State.mouseX, rect)
	val y = convertScreenY(State.mouseY, rect)
	val zoomFactor = 0.95f
	val currentFactor = if (preciseWheelRotation == 1f) zoomFactor else 1f / zoomFactor
	PlotState.x1 = x + (PlotState.x1 - x) * currentFactor
	PlotState.y1 = y + (PlotState.y1 - y) * currentFactor
	PlotState.x0 = x - (x - PlotState.x0) * currentFactor
	PlotState.y0 = y - (y - PlotState.y0) * currentFactor
}


private fun drawSegments(canvas: Canvas, content: List<PlotCell>, rect: Rect) {
	val sortedPoints = content.sorted()
	sortedPoints.windowed(2, 1, false).forEach { (curr, next) ->
		canvas.drawLine(
			convertPlotX(curr.x, rect),
			convertPlotY(curr.y, rect),
			convertPlotX(next.x, rect),
			convertPlotY(next.y, rect),
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
	content.filter { it.x in PlotState.x0..PlotState.x1 && it.y in PlotState.y0..PlotState.y1 }.forEach {
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
	if (0f in PlotState.x0..PlotState.x1 && 0f in PlotState.y0..PlotState.y1)
		canvas.drawString("0", convertPlotX(0f, rect), convertPlotY(0f, rect) - 1f, thinFont, paint)
}

private fun drawHorizontallyGrid(canvas: Canvas, step: Float, rect: Rect) {
	val startY = ceil(PlotState.y0 / step).toInt()
	val finishY = floor(PlotState.y1 / step).toInt()
	for (horizontal in (startY..finishY) - 0) {
		val screenY = convertPlotY(horizontal * step, rect)
		canvas.drawLine(rect.left, screenY, rect.right, screenY, thinStroke)
		val inscription = (step * horizontal).toString()
		when {
			0 < PlotState.x0 -> canvas.drawString(inscription, rect.left, screenY, thinFont, paint)
			0 > PlotState.x1 -> drawByRightSide(canvas, inscription, rect.right, screenY, font, paint)
			else -> canvas.drawString(inscription, convertPlotX(0f, rect) + 1f, screenY, thinFont, paint)
		}
	}
}

private fun drawVerticallyGrid(canvas: Canvas, rect: Rect, step: Float) {
	val startX = ceil(PlotState.x0 / step).toInt()
	val finishX = floor(PlotState.x1 / step).toInt()
	for (vertical in (startX..finishX) - 0) {
		val screenX = convertPlotX(step * vertical, rect)
		canvas.drawLine(screenX, rect.top, screenX, rect.bottom, thinStroke)
		val inscription = (step * vertical).toString()
		when {
			0 < PlotState.y0 -> canvas.drawString(inscription, screenX, rect.bottom, thinFont, paint)
			0 > PlotState.y1 -> canvas.drawString(inscription, screenX, rect.top + thinFont.size, thinFont, paint)
			else -> canvas.drawString(inscription, screenX, convertPlotY(0f, rect) - 1f, thinFont, paint)
		}
	}
}

private fun drawCoordinateAxes(rect: Rect, canvas: Canvas) {
	if (0f in PlotState.x0..PlotState.x1) {
		val centerX = convertPlotX(0f, rect)
		drawArrow(canvas, centerX, rect.bottom, centerX, rect.top)
	}
	if (0f in PlotState.y0..PlotState.y1) {
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