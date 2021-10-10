import org.jetbrains.skija.Font
import org.jetbrains.skija.Paint
import org.jetbrains.skija.PaintMode
import org.jetbrains.skija.Typeface
import kotlin.math.ln
import kotlin.random.Random

fun randomColor(seed: Float) = Paint().apply {
	color = 0xFF000000.toInt() + Random((ln(seed + 1) * 1e7).toInt()).nextInt() % 0x1000000
}

private val typeface = Typeface.makeFromFile("fonts/JetBrainsMono-Regular.ttf")
val font = Font(typeface, 15f)
val thinFont = Font(typeface, 12f)
val paint = Paint().apply {
	color = 0xff000000.toInt()
	mode = PaintMode.FILL
	strokeWidth = 1f
}

val stroke = Paint().apply {
	color = 0xFF000000.toInt()
	mode = PaintMode.STROKE
	strokeWidth = 2f
}

val thinStroke = Paint().apply {
	color = 0x5F000000
	mode = PaintMode.STROKE
	strokeWidth = 0.5f
}