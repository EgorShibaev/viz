import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.swing.Swing
import org.jetbrains.skija.*
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkiaRenderer
import org.jetbrains.skiko.SkiaWindow
import org.jetbrains.skiko.orderEmojiAndSymbolsPopup
import java.awt.Dimension
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionAdapter
import javax.swing.WindowConstants
import kotlin.math.*
import kotlin.random.Random

fun main() {
	val content = listOf(
		Pair(100f, "text1"), Pair(64f, "text2"), Pair(127f, "text3"), Pair(1000f, "text4")
	)
	val intContent = listOf(
		Pair(1f, 1f), Pair(0f, 2f), Pair(3f, 5f)
	)
	createWindow("plot", Diagram.PLOT, intContent)
//	createWindow("bar chart", Diagram.BAR_CHART, content)
//	createWindow("circle", Diagram.CIRCLE, content)

}

enum class Diagram {
	CIRCLE, BAR_CHART, PLOT
}

fun createWindow(title: String, type: Diagram, content: List<Pair<Float, Any>>) = runBlocking(Dispatchers.Swing) {
	val window = SkiaWindow()
	window.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
	window.title = title

	window.layer.renderer = Renderer(window.layer, type, content)
	window.layer.addMouseMotionListener(MyMouseMotionAdapter)

	window.preferredSize = Dimension(800, 600)
	window.minimumSize = Dimension(100, 100)
	window.pack()
	window.layer.awaitRedraw()
	window.isVisible = true
}

class Renderer(
	private val layer: SkiaLayer,
	private val type: Diagram,
	private val content: List<Pair<Float, Any>>
) : SkiaRenderer {
	private val typeface = Typeface.makeFromFile("fonts/JetBrainsMono-Regular.ttf")
	private val font = Font(typeface, 15f)
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
		strokeWidth = 1f
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
			Diagram.BAR_CHART -> {
				content.forEach {
					assert(it.second is String)
				}
				barChart(
					canvas, w / 100F, h / 100F, w.toFloat() - 10F, h.toFloat() - 10F,
					content.map { Pair(it.first, it.second as String) }
				)
			}
			Diagram.CIRCLE -> {
				content.forEach {
					assert(it.second is String)
				}
				separatedCircle(
					canvas, w / 2F, h / 2F, min(w / 2F, h / 2F) - 30F,
					content.map { Pair(it.first, it.second as String) }
				)
			}
			Diagram.PLOT -> {
				content.forEach {
					assert(it.second is Int)
				}
				plot(
					canvas, w / 100f, h / 100f, w.toFloat() - 10F, h.toFloat() - 10F,
					content.map { Pair(it.first, it.second as Float) }
				)
			}
		}
		layer.needRedraw()
	}

	private fun separatedCircle(
		canvas: Canvas, centerX: Float, centerY: Float, r: Float, content: List<Pair<Float, String>>
	) {
		assert(content.all { it.first >= 0 })
		val sum = content.sumOf { it.first.toDouble() }.toFloat()
		var angle = 0F
		lineInCircle(canvas, centerX, centerY, r, angle)
		content.forEach {
			val sweepAngle = it.first / sum * Math.PI.toFloat() * 2
			val text = "${it.second} - ${it.first} (${(it.first / sum * 100).toInt()}%)"
			val prevAngle = angle
			angle += sweepAngle
			canvas.drawArc(
				centerX - r, centerY - r, centerX + r, centerY + r,
				toDegree(prevAngle) - 90F, toDegree(sweepAngle), true, randomColor(it.first)
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
			angle += it.first / sum * Math.PI.toFloat() * 2
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
		canvas: Canvas, left: Float, top: Float, right: Float, bottom: Float, content: List<Pair<Float, String>>
	) {
		assert(content.all { it.first >= 0 })
		val max = content.maxOf { it.first }
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
		content: List<Pair<Float, String>>
	) {
		var currX = left
		content.forEach {
			val value = it.first
			val text = it.second
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
			if (checkInRect(rect))
				canvas.drawString(value.toString(), State.mouseX, State.mouseY, font, paint)
			currX += 1.5F * widthOfColumn
		}
	}

	private fun checkInRect(rect: Rect) = rect.run {
		State.mouseX in left..right && State.mouseY in top..bottom
	}

	private fun plot(
		canvas: Canvas,
		left: Float,
		top: Float,
		right: Float,
		bottom: Float,
		content: List<Pair<Float, Float>>
	) {
		drawArrow(canvas, left, bottom, left, top, paint)
		drawArrow(canvas, left, bottom, right, bottom, paint)
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
		val rotatedVectorX2 = (vectorX * cos(-	angle) - vectorY * sin(-angle)) * len / vectorLen
		val rotatedVectorY2 = (vectorX * sin(-angle) + vectorY * cos(-angle)) * len / vectorLen
		canvas.drawLine(x1 - rotatedVectorX2, y1 - rotatedVectorY2, x1, y1, paint)
	}
}

object State {
	var mouseX = 0f
	var mouseY = 0f
}

object MyMouseMotionAdapter : MouseMotionAdapter() {
	override fun mouseMoved(event: MouseEvent) {
		State.mouseX = event.x.toFloat()
		State.mouseY = event.y.toFloat()
	}
}