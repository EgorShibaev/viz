import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.swing.Swing
import org.jetbrains.skija.*
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkiaRenderer
import org.jetbrains.skiko.SkiaWindow
import java.awt.Dimension
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionAdapter
import kotlin.math.cos
import javax.swing.WindowConstants
import kotlin.math.ln
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

fun main() {
	val content = listOf(
		Pair(0.4F, "text1"), Pair(0.2F, "text2"), Pair(0.1F, "text3")
	)
	createWindow("pf-2021-viz", Diagram.BAR_CHART, content)
	createWindow("pf-2021-viz", Diagram.CIRCLE, content)

}

enum class Diagram {
	CIRCLE, BAR_CHART
}

fun createWindow(title: String, type: Diagram, content: List<Pair<Float, String>>) = runBlocking(Dispatchers.Swing) {
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
	private val content: List<Pair<Float, String>>
) : SkiaRenderer {
	private val typeface = Typeface.makeFromFile("fonts/JetBrainsMono-Regular.ttf")
	private val font = Font(typeface, 15f)
	private val bigFont = Font(typeface, 20f)
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

	private fun randomColor(seed: Float) = Paint().apply {
		color = 0xFF000000.toInt() + Random((ln(seed) * 1000).toInt()).nextInt() % 0x1000000
	}

	override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
		val contentScale = layer.contentScale
		canvas.scale(contentScale, contentScale)
		val w = (width / contentScale).toInt()
		val h = (height / contentScale).toInt()

		when (type) {
			Diagram.BAR_CHART ->
				barChart(canvas, w / 100F, h / 100F, w.toFloat() - 10F, h.toFloat() - 10F, content)
			Diagram.CIRCLE ->
				separatedCircle(canvas, w / 2F, h / 2F, min(w / 2F, h / 2F) - 30F, content)
		}
		layer.needRedraw()
	}

	private fun separatedCircle(
		canvas: Canvas, centerX: Float, centerY: Float, r: Float, content: List<Pair<Float, String>>
	) {
		assert(content.sumOf { it.first.toDouble() } <= 2F * Math.PI.toFloat())
		val otherAngle = 2F * Math.PI.toFloat() - content.sumOf { it.first.toDouble() }.toFloat()
		val fullContent = content + Pair(otherAngle, "other")
		var angle = 0F
		lineInCircle(canvas, centerX, centerY, r, angle)
		fullContent.forEach {
			val sweepAngle = it.first
			val text = "${it.second} - ${(100 * sweepAngle / Math.PI / 2F).toInt()}%"
			val prevAngle = angle
			angle += sweepAngle
			canvas.drawArc(
				centerX - r, centerY - r, centerX + r, centerY + r,
				toDegree(prevAngle) - 90F, toDegree(sweepAngle), true, randomColor(sweepAngle)
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
		fullContent.forEach {
			angle += it.first
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
		assert(content.sumOf { it.first.toDouble() } <= 1F)
		val otherAngle = 1F - content.sumOf { it.first.toDouble() }.toFloat()
		val fullContent = content + Pair(otherAngle, "other")
		// widthOfColumn * cnt + widthOfColumn / 2 * (cnt - 1) = width
		val widthOfColumn = (right - left) / (1.5F * fullContent.size - 0.5F)
		val forText = fullContent.size * bigFont.size + 10F
		val height = (bottom - font.size - 5F - (top + forText)) / fullContent.maxOf { it.first }
		var currX = left
		fullContent.withIndex().forEach {
			val value = it.value.first
			val index = it.index
			val rect = Rect(
				currX,
				bottom - font.size - 5F - height * value,
				currX + widthOfColumn,
				bottom - font.size - 5F
			)
			canvas.drawRect(rect, randomColor(value))
			canvas.drawRect(rect, stroke)
			canvas.drawString("${index + 1}", currX + widthOfColumn / 2, bottom - 2F, font, paint)
			currX += 1.5F * widthOfColumn
		}
		fullContent.withIndex().forEach {
			val index = it.index + 1
			val text = it.value.second
			val percent = (it.value.first * 100F).toInt()
			canvas.drawString("$index. $text - ${percent}%", left + 3F, top + index * bigFont.size, bigFont, paint)
		}
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