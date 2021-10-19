import diagram.*
import fontsAndPaints.paint
import org.jetbrains.skija.*
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

fun getTextWidth(text: String, font: Font) = text.length * font.size * 0.6f

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
	fun drawStringOrCalcSize(shouldDraw: Boolean): Pair<Float, Float> {
		var maxX = rect.left
		var currX = rect.left
		var currY = rect.top + font.size
		val words = text.split(' ')
		words.forEach {
			if (currX + font.size * 0.6f * it.length > rect.right && currX != rect.left) {
				currX = rect.left
				currY += font.size + 2f
			}
			if (shouldDraw)
				canvas.drawString(it, currX, currY, font, paint)
			currX += font.size * 0.6f * (it.length + 1)
			maxX = max(maxX, currX)
		}
		return Pair(currY, maxX)
	}

	val (bottom, right) = drawStringOrCalcSize(false)
	// fill and stroke
	canvas.drawRect(Rect(rect.left, rect.top, right, bottom + 2f), Paint().apply {
		color = 0xcfcfcfff.toInt()
	})
	canvas.drawRect(Rect(rect.left, rect.top, right, bottom + 2f), Paint().apply {
		color = 0xff000000.toInt()
		mode = PaintMode.STROKE
	})

	drawStringOrCalcSize(true)
}

fun distance(x0: Float, y0: Float, x1: Float, y1: Float) = sqrt((x0 - x1).pow(2) + (y0 - y1).pow(2))

/**
 * Check if content correct, cast to corresponding type and then call corresponding function
 * */
fun drawDiagram(canvas: Canvas, type: Diagram, content: List<Cell>, w: Float, h: Float) {
	assert(content.isNotEmpty())
	checkIsContentCorrect(type, content)

	when (type) {
		Diagram.BAR_CHART -> barChart(
			canvas, Rect(w / 100F, h / 100F, w - 10F, h - 10F),
			content.map { it as ChartCell }
		)
		Diagram.CIRCLE -> separatedCircle(
			canvas, w / 2F, h / 2F, min(w / 2F, h / 2F) - 30F,
			content.map { it as ChartCell }
		)
		Diagram.PLOT -> plot(
			canvas, Rect(w / 100f, h / 100f, w - 10F, h - 10F),
			content.map { it as PlotCell }
		)
		Diagram.POLAR_CHART -> polarChart(
			canvas, w / 2, h / 2, min(w / 2F, h / 2F) - 50F,
			content.map { it as ChartCell }
		)
		Diagram.TREE -> tree(
			canvas, Rect(w / 100, h / 15, w - 10f, h - 40f),
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
