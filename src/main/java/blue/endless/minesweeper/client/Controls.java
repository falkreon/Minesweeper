package blue.endless.minesweeper.client;

import java.util.HashMap;

import org.jetbrains.annotations.NotNull;

public class Controls {
	private HashMap<String, DigitalControl> controls = new HashMap<>();
	
	public void keyDown(int scanCode) {
		for(DigitalControl control : controls.values()) control.accept(scanCode, true);
	}
	
	public void keyUp(int scanCode) {
		for(DigitalControl control : controls.values()) control.accept(scanCode, false);
	}
	
	public void registerControl(String name, DigitalControl control) {
		controls.put(name, control);
	}
	
	/** Gets a digital control with the specified name. If there is no control with that name, adds one and returns it. */
	public @NotNull DigitalControl get(String name) {
		return controls.computeIfAbsent(name, (it) -> new DigitalControl(-1));
	}
}
