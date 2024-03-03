package blue.endless.minesweeper.client;

import org.lwjgl.glfw.GLFW;

/**
 * Represents a control which is either *active* or *inactive*.
 */
public class DigitalControl {
	public boolean isPressed = false;
	private int scanCode;
	
	public void bindKey(int keyCode) {
		scanCode = GLFW.glfwGetKeyScancode(keyCode);
	}
	
	public void bindScanCode(int scanCode) {
		this.scanCode = scanCode;
	}
	
	public DigitalControl(int defaultScanCode) {
		this.scanCode = defaultScanCode;
	}
	
	public void accept(int scanCode, boolean status) {
		if (this.scanCode == scanCode) isPressed = status;
	}
	
	public boolean isPressed() {
		return isPressed;
	}
}
