package blue.endless.minesweeper.world;

import blue.endless.jankson.api.document.ObjectElement;
import blue.endless.jankson.api.document.PrimitiveElement;

public class Tile {
	ObjectElement data = new ObjectElement();
	
	public boolean isForeground() {
		return data.getPrimitive("foreground").asBoolean().orElse(false);
	}
	
	public void setForeground(boolean foreground) {
		data.put("foreground", PrimitiveElement.of(foreground));
	}
	
	public ObjectElement data() { return data; }
}
