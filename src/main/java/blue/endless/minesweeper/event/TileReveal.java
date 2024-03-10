package blue.endless.minesweeper.event;

import blue.endless.minesweeper.world.Area;
import blue.endless.tinyevents.Event;

public interface TileReveal {
	public void accept(Area area, int x, int y);
	
	public static Event<TileReveal> newEvent() {
		return new Event<TileReveal>((handlers) -> (area, x, y) -> {
			for(Event.Entry<TileReveal> entry : handlers) {
				entry.handler().accept(area, x, y);
			}
		});
	}
}
