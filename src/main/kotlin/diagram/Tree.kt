package diagram

import TreeCell
import distance
import drawStringInRect
import fontsAndPaints.*
import org.jetbrains.skija.Canvas
import org.jetbrains.skija.Paint
import org.jetbrains.skija.PaintMode
import org.jetbrains.skija.Rect

fun tree(canvas: Canvas, rect: Rect, root: TreeCell) {
	val points = getPoints(rect, root, null, null, getDepth(root))
	drawPoints(canvas, rect, points)
}

data class ScreenPoint(
	val x: Float, val y: Float, val cell: TreeCell,
	val paint: Paint, val parentX: Float?, val parentY: Float?
)

fun drawPoints(canvas: Canvas, rect: Rect, points: List<ScreenPoint>) {
	val radius = 5f
	points.forEach {
		if (it.parentX != null && it.parentY != null)
			canvas.drawLine(it.x, it.y, it.parentX, it.parentY, thinStroke)
	}
	points.forEach {
		canvas.drawCircle(it.x, it.y, radius, it.paint)
	}
	points.forEach {
		canvas.drawString(it.cell.name, it.x + 5, it.y - 5, font, paint)
	}
	points.forEach {
		if (distance(it.x, it.y, State.mouseX, State.mouseY) <= radius) {
			canvas.drawCircle(it.x, it.y, radius + 2, Paint().apply {
				color = 0xff0000ff.toInt()
				mode = PaintMode.STROKE
				strokeWidth = 2f
			})
			val text = "${it.cell.name} - ${it.cell.detailedInfo}"
			drawStringInRect(canvas, text, Rect(it.x + 5f, it.y - 5f - font.size, rect.right, rect.bottom), font)
		}
	}
}

fun getDepth(node: TreeCell): Int = (node.children.maxOfOrNull { getDepth(it) } ?: 0) + 1

fun childrenCount(node: TreeCell): Int = node.children.sumOf { childrenCount(it) } + 1

fun getPoints(rect: Rect, root: TreeCell, parentX: Float?, parentY: Float?, depth: Int): List<ScreenPoint> {
	val levelHeight = rect.height / (depth - 1)
	val x = (rect.left + rect.right) / 2
	val y = rect.top
	val paint = randomColor(root.name.sumOf { it.code } * childrenCount(root).toFloat())
	val rootPoint = ScreenPoint(x, y, root, paint, parentX, parentY)
	var currLeft = rect.left
	val nodeCount = childrenCount(root) - 1
	val otherPoints = root.children.map {
		val width = childrenCount(it).toFloat() / nodeCount * rect.width
		val left = currLeft
		val right = left + width
		currLeft += width
		getPoints(Rect(left, rect.top + levelHeight, right, rect.bottom), it, x, y, depth - 1)
	}.flatten()
	return listOf(rootPoint) + otherPoints
}