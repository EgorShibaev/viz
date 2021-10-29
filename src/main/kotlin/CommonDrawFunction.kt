import diagram.*
import fontsAndPaints.paint
import org.jetbrains.skija.*
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

fun getTextWidth(text: String, font: Font): Float {
	val relativeCharWidth = 0.6f
	return text.length * font.size * relativeCharWidth
}

/** Draw text so that right side is fixed */
fun drawByRightSide(canvas: Canvas, text: String, right: Float, y: Float, font: Font, paint: Paint) {
	canvas.drawString(text, right - getTextWidth(text, font), y, font, paint)
}

/**
 * Program draw string in given rect.
 * There is subfunction which calculate coordinates of each word in string.
 * and draw it if corresponding flag is true. Firstly, program call this subfunction
 * with flag false and draw rectangle with background for string.
 * Secondly, program call subfunction with flag true and draw string.
 * */
fun drawStringInRect(canvas: Canvas, text: String, rect: Rect, font: Font) {
	val horizontalIndent = 2f

	fun drawStringOrCalcSize(shouldDraw: Boolean): Pair<Float, Float> {
		var maxX = rect.left
		var currX = rect.left
		var currY = rect.top + font.size
		val words = text.split(' ')
		words.forEach {
			if (currX + getTextWidth(it, font) > rect.right && currX != rect.left) {
				currX = rect.left
				currY += font.size + horizontalIndent
			}
			if (shouldDraw)
				canvas.drawString(it, currX, currY, font, paint)
			currX += getTextWidth("$it ", font)
			maxX = max(maxX, currX)
		}
		return Pair(currY, maxX)
	}

	val (bottom, right) = drawStringOrCalcSize(false)
	// fill and stroke
	canvas.drawRect(Rect(rect.left, rect.top, right, bottom + horizontalIndent), Paint().apply {
		color = 0xcfcfcfff.toInt()
	})
	canvas.drawRect(Rect(rect.left, rect.top, right, bottom + horizontalIndent), Paint().apply {
		color = 0xff000000.toInt()
		mode = PaintMode.STROKE
	})

	drawStringOrCalcSize(true)
}

fun distance(x0: Float, y0: Float, x1: Float, y1: Float) = sqrt((x0 - x1).pow(2) + (y0 - y1).pow(2))

/**
 * Check if content correct, cast to corresponding type and then call corresponding function
 * */
fun drawDiagram(canvas: Canvas, type: Diagram, content: List<Cell>, width: Float, height: Float) {
	assert(content.isNotEmpty())
	checkIsContentCorrect(type, content)
	val horizontalIndent = 10f
	val verticalIndent = 20f
	val circleIndent = 50f

	when (type) {
		Diagram.BAR_CHART -> barChart(
			canvas, Rect(horizontalIndent, verticalIndent, width - horizontalIndent, height - verticalIndent),
			content.map { it as ChartCell }
		)
		Diagram.CIRCLE -> separatedCircle(
			canvas, width / 2, height / 2, min(width / 2, height / 2) - circleIndent,
			content.map { it as ChartCell }
		)
		Diagram.PLOT -> plot(
			canvas, Rect(horizontalIndent, verticalIndent, width - horizontalIndent, height - verticalIndent),
			content.map { it as PlotCell }
		)
		Diagram.POLAR_CHART -> polarChart(
			canvas, width / 2, height / 2, min(width / 2F, height / 2F) - circleIndent,
			content.map { it as ChartCell }
		)
		Diagram.TREE -> tree(
			canvas, Rect(horizontalIndent, verticalIndent, width - horizontalIndent, height - verticalIndent),
			content[0] as TreeCell
		)
	}
}

fun checkIsContentCorrect(type: Diagram, content: List<Cell>) {
	when (type) {
		Diagram.TREE -> {
			// For tree suppose that content contain only one node - root of all tree.
			assert(content.all { it is TreeCell })
			assert(content.size == 1)
		}
		Diagram.PLOT -> assert(content.all { it is PlotCell })
		Diagram.CIRCLE, Diagram.BAR_CHART, Diagram.POLAR_CHART -> assert(content.all { it is ChartCell })
	}
}
