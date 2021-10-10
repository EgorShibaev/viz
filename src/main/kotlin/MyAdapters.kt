import java.awt.event.*
import kotlin.math.abs

object MyMouseMotionAdapter : MouseMotionAdapter() {
	override fun mouseMoved(event: MouseEvent) {
		State.mouseX = event.x.toFloat()
		State.mouseY = event.y.toFloat()
	}

	override fun mouseDragged(e: MouseEvent?) {
		e?.apply {
			val lastX = State.lastDraggedX
			val lastY = State.lastDraggedY
			if (lastX == null || lastY == null || abs(lastX - x) > 20f || abs(lastY - y) > 20f) {
				State.lastDraggedX = x.toFloat()
				State.lastDraggedY = y.toFloat()
			} else {
				State.vectorToMoveX += x - lastX
				State.vectorToMoveY += y - lastY
				State.lastDraggedX = x.toFloat()
				State.lastDraggedY = y.toFloat()
			}
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