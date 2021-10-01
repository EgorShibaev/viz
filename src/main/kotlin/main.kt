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
import kotlin.math.sin

fun main() {
	createWindow("pf-2021-viz")
}

fun createWindow(title: String) = runBlocking(Dispatchers.Swing) {
	val window = SkiaWindow()
	window.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
	window.title = title

	window.layer.renderer = Renderer(window.layer)
	window.layer.addMouseMotionListener(MyMouseMotionAdapter)

	window.preferredSize = Dimension(800, 600)
	window.minimumSize = Dimension(100, 100)
	window.pack()
	window.layer.awaitRedraw()
	window.isVisible = true
}

class Renderer(private val layer: SkiaLayer) : SkiaRenderer {
	private val typeface = Typeface.makeFromFile("fonts/JetBrainsMono-Regular.ttf")
	private val font = Font(typeface, 15f)
	private val paint = Paint().apply {
		color = 0xff9BC730L.toInt()
		mode = PaintMode.FILL
		strokeWidth = 1f
	}

	private val colors = mapOf(
		"yellow" to Paint().apply {
			color = 0xFFE4FF01.toInt()
		},
		"red" to Paint().apply {
			color = 0xFFFF0000.toInt()
		},
		"blue" to Paint().apply {
			color = 0xFF78CAD2.toInt()
		},
		"gray" to Paint().apply {
			color = 0xFF7C8483.toInt()
		})

	private val stroke = Paint().apply {
		color = 0xFF000000.toInt()
		mode = PaintMode.STROKE
		strokeWidth = 2f
	}

	private fun colorByText(text : String) = Paint().apply {
		val hash = text.hashCode()
		color = 0xFF000000.toInt() + (hash * hash * hash) % 0x1000000
	}

	override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
		val contentScale = layer.contentScale
		canvas.scale(contentScale, contentScale)
		val w = (width / contentScale).toInt()
		val h = (height / contentScale).toInt()

		separatedCircle(
			canvas, w / 2F, h / 2F, minOf(w / 2F, h / 2F) - 35f,
			listOf(
				Pair(0.5F, "text1"), Pair(0.1F, "text2"),
				Pair(1F, "text3"), Pair(1F, "text4"),
				Pair(1F, "text7"), Pair(1F, "text6"),
				Pair(1F, "text5")
			)
		)

		layer.needRedraw()
	}

	private fun separatedCircle(
		canvas: Canvas, centerX: Float, centerY: Float, r: Float, content: List<Pair<Float, String>>
	) {
		assert(content.sumOf { it.first.toDouble() } <= 2F * Math.PI.toFloat())
		val otherAngle = 2F * Math.PI.toFloat() - content.sumOf { it.first.toDouble() }.toFloat()
		val fullContent = content + Pair(otherAngle, "other")
		canvas.drawCircle(centerX, centerY, r, colors.getValue("gray"))
		var angle = 0F
		lineInCircle(canvas, centerX, centerY, r, angle)
		fullContent.forEach {
			val prevAngle = angle
			angle += it.first
			canvas.drawArc(centerX - r, centerY - r, centerX + r, centerY + r,
				toDegree(prevAngle) - 90F, toDegree(it.first), true, colorByText(it.second))
			val angleForText = (prevAngle + angle) / 2
			val xForText = if (angleForText < Math.PI.toFloat()) centerX + (r + 15F) * sin(angleForText) else
				centerX + (r + 15F) * sin(angleForText) - font.size * 0.6F * it.second.length
			val yForText = centerY - (r + 15F) * cos(angleForText)
			canvas.drawString(it.second, xForText, yForText, font, paint)
		}
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