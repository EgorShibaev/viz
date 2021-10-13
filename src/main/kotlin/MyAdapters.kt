import java.awt.event.*
import kotlin.math.abs

object MyMouseMotionAdapter : MouseMotionAdapter() {
	override fun mouseMoved(event: MouseEvent) {
		State.mouseX = event.x.toFloat()
		State.mouseY = event.y.toFloat()
	}

	/**
	 * This method is called when mouse is dragged. It is used for plot moving.
	 * This method works with four variables in State: lastDraggedX, lastDraggedY, vectorToMoveX, vectorToMoveY
	 * If distance between point with last coordinates and current position is small,
	 * program believe that mouse was dragged from (lastDraggedX; lastDraggedY) to current position.
	 * Then program add vector which connect last and current point to vector (vectorToMoveX, vectorToMoveY).
	 * The corresponding function in Plot.kt work with vectorToMoveX and vectorToMoveY.
	 * */
	override fun mouseDragged(e: MouseEvent?) {
		e?.apply {
			val lastX = State.lastDraggedX
			val lastY = State.lastDraggedY
			if (lastX != null && lastY != null && abs(lastX - x) < 20f && abs(lastY - y) < 20f) {
				State.vectorToMoveX += x - lastX
				State.vectorToMoveY += y - lastY
			}
			// update last cursor position
			State.lastDraggedX = x.toFloat()
			State.lastDraggedY = y.toFloat()
		}
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