import diagram.barChart
import diagram.separatedCircle
import diagram.plot
import diagram.polarChart
import fontsAndPaints.paint
import org.jetbrains.skija.*
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

fun getTextWidth(text: String, font: Font) = text.length * font.size * 0.6f

fun drawByRightSide(canvas: Canvas, text: String, right: Float, y: Float, font: Font, paint: Paint) {
	canvas.drawString(text, right - getTextWidth(text, font), y, font, paint)
}

fun drawStringInRect(canvas: Canvas, text: String, rect: Rect, font: Font) {
	fun drawStringOrCalcSize(b : Boolean): Pair<Float, Float> {
		var maxX = rect.left
		var currX = rect.left
		var currY = rect.top + font.size
		val words = text.split(' ')
		words.forEach {
			if (currX + font.size * 0.6f * it.length > rect.right && currX != rect.left){
				currX = rect.left
				currY += font.size + 2f
			}
			if (b)
				canvas.drawString(it, currX, currY, font, paint)
			currX += font.size * 0.6f * (it.length + 1)
			maxX = max(maxX, currX)
		}
		return Pair(currY, maxX)
	}

	val (bottom, right) = drawStringOrCalcSize(false)
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
	}
}

fun checkIsContentCorrect(type: Diagram, content: List<Cell>) {
	when (type) {
		Diagram.PLOT -> assert(content.all { it is PlotCell })
		Diagram.CIRCLE, Diagram.BAR_CHART, Diagram.POLAR_CHART -> assert(content.all { it is ChartCell })
	}
}
