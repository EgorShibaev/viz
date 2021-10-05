import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.swing.Swing
import org.jetbrains.skija.*
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkiaRenderer
import org.jetbrains.skiko.SkiaWindow
import java.awt.Dimension
import java.awt.event.*
import javax.swing.WindowConstants
import kotlin.math.*
import kotlin.random.Random

fun main() {
	val content = listOf(
		ChartCell(100f, "text1"),
		ChartCell(64f, "text2"),
		ChartCell(127f, "text3"),
		ChartCell(1000f, "text4")
	)
	val intContent = listOf(
		PlotCell(1f, 1f, "text1"), PlotCell(5f, 5f, "text2"), PlotCell(1f, 5f, "text3"),
		PlotCell(5f, 1f, "text4")
	)
	createWindow("plot", Diagram.PLOT, intContent)
//	createWindow("bar chart", Diagram.BAR_CHART, content)
//	createWindow("circle", Diagram.CIRCLE, content)

}

open class Cell

class PlotCell(val x: Float, val y: Float, val text: String) : Cell()

class ChartCell(val value: Float, val text: String) : Cell()

enum class Diagram {
	CIRCLE, BAR_CHART, PLOT
}

fun createWindow(title: String, type: Diagram, content: List<Cell>) = runBlocking(Dispatchers.Swing) {
	val window = SkiaWindow()
	window.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
	window.title = title

	window.layer.renderer = Renderer(window.layer, type, content)
	window.layer.addMouseMotionListener(MyMouseMotionAdapter)
	window.layer.addMouseWheelListener(MyMouseWheelListener)
	window.layer.addKeyListener(MyKeyAdapter)

	window.preferredSize = Dimension(800, 600)
	window.minimumSize = Dimension(100, 100)
	window.pack()
	window.layer.awaitRedraw()
	window.isVisible = true
}

class Renderer(
	private val layer: SkiaLayer,
	private val type: Diagram,
	private val content: List<Cell>
) : SkiaRenderer {
	private val typeface = Typeface.makeFromFile("fonts/JetBrainsMono-Regular.ttf")
	private val font = Font(typeface, 15f)
	private val thinFont = Font(typeface, 12f)
	private val paint = Paint().apply {
		color = 0xff000000.toInt()
		mode = PaintMode.FILL
		strokeWidth = 1f
	}

	private val stroke = Paint().apply {
		color = 0xFF000000.toInt()
		mode = PaintMode.STROKE
		strokeWidth = 2f
	}

	private val thinStroke = Paint().apply {
		color = 0x5F000000
		mode = PaintMode.STROKE
		strokeWidth = 0.5f
	}

	private fun randomColor(seed: Float) = Paint().apply {
		color = 0xFF000000.toInt() + Random((ln(seed + 1) * 1e7).toInt()).nextInt() % 0x1000000
	}

	override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
		val contentScale = layer.contentScale
		canvas.scale(contentScale, contentScale)
		val w = (width / contentScale).toInt()
		val h = (height / contentScale).toInt()

		assert(content.isNotEmpty())
		when (type) {
			Diagram.BAR_CHART -> barChart(
				canvas, w / 100F, h / 100F, w.toFloat() - 10F, h.toFloat() - 10F,
				content.map { it as? ChartCell ?: throw IllegalArgumentException("Wrong type for content") }
			)
			Diagram.CIRCLE -> separatedCircle(
				canvas, w / 2F, h / 2F, min(w / 2F, h / 2F) - 30F,
				content.map { it as? ChartCell ?: throw IllegalArgumentException("Wrong type for content") }
			)

			Diagram.PLOT -> {
				plot(
					canvas, w / 100f, h / 100f, w.toFloat() - 10F, h.toFloat() - 10F,
					content.map { it as? PlotCell ?: throw IllegalArgumentException("Wrong type for content") }
				)
			}
		}
		layer.needRedraw()
	}

	private fun separatedCircle(
		canvas: Canvas, centerX: Float, centerY: Float, r: Float, content: List<ChartCell>
	) {
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
				centerX + (r + 15F) * sin(angleForText) - font.size * 0.6F * text.length
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

	private fun barChart(
		canvas: Canvas, left: Float, top: Float, right: Float, bottom: Float, content: List<ChartCell>
	) {
		assert(content.all { it.value >= 0 })
		val max = content.maxOf { it.value }
		val leftIndent = 80f
		// widthOfColumn * cnt + widthOfColumn / 2 * (cnt - 1) = width
		val widthOfColumn = (right - (left + leftIndent)) / (1.5F * content.size - 0.5F)
		val factor = (bottom - font.size - 5F - (top + font.size + 5F)) / max
		verticalLines(canvas, max, left, right, bottom, factor)
		drawColumns(canvas, left + leftIndent, bottom, widthOfColumn, factor, content)
	}

	private fun verticalLines(
		canvas: Canvas, max: Float, left: Float, right: Float, bottom: Float, factor: Float
	) {
		val step = max / 10f
		(0..10).forEach {
			canvas.drawLine(
				left,
				bottom - font.size - 5F - it * step * factor,
				right,
				bottom - font.size - 5F - it * step * factor,
				thinStroke
			)
			canvas.drawString("%.2f".format(it * step), left, bottom - font.size - 8F - it * step * factor, font, paint)

		}
	}

	private fun drawColumns(
		canvas: Canvas,
		left: Float,
		bottom: Float,
		widthOfColumn: Float,
		factor: Float,
		content: List<ChartCell>
	) {
		var currX = left
		content.forEach {
			val value = it.value
			val text = it.text
			val rect = Rect(
				currX,
				bottom - font.size - 5F - factor * value,
				currX + widthOfColumn,
				bottom - font.size - 5F
			)
			canvas.drawRect(rect, randomColor(value))
			canvas.drawRect(rect, stroke)
			canvas.drawString(
				text, currX + widthOfColumn / 2 - text.length * font.size * 0.6F / 2F, bottom - 2F, font, paint
			)
			if (checkInRect(rect, State.mouseX, State.mouseY))
				canvas.drawString(value.toString(), State.mouseX, State.mouseY, font, paint)
			currX += 1.5F * widthOfColumn
		}
	}

	private fun checkInRect(rect: Rect, x: Float, y: Float) = rect.run {
		x in left..right && y in top..bottom
	}

	private var x0 = -10f
	private var y0 = -10f
	private var x1 = 10f
	private var y1 = 10f

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

	private fun plot(
		canvas: Canvas,
		left: Float,
		top: Float,
		right: Float,
		bottom: Float,
		content: List<PlotCell>
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
		drawNet(canvas, getNearestRoundNumber((x1 - x0) / 10), left, top, right, bottom)
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
			canvas.drawString(it.text, x + 5f, y + 5f, font, paint)
		}
	}

	private fun drawNet(canvas: Canvas, step: Float, left: Float, top: Float, right: Float, bottom: Float) {
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
			if (horizontal * step in y0..y1) {
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
}

object State {
	var mouseX = 0f
	var mouseY = 0f
	var e: MouseWheelEvent? = null
	var pressedKeyCode: Int? = null
}

object MyMouseMotionAdapter : MouseMotionAdapter() {
	override fun mouseMoved(event: MouseEvent) {
		State.mouseX = event.x.toFloat()
		State.mouseY = event.y.toFloat()
	}
}

object MyMouseWheelListener : MouseWheelListener {
	override fun mouseWheelMoved(e: MouseWheelEvent?) {
		State.e = e
	}
}

object MyKeyAdapter : KeyAdapter() {
	override fun keyPressed(e: KeyEvent?) {
		State.pressedKeyCode = e?.keyCode
	}
}