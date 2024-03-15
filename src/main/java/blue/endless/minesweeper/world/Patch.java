package blue.endless.minesweeper.world;

import java.util.HashMap;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.BiConsumer;

import blue.endless.minesweeper.world.te.TileEntity;

/**
 * This is a segment of an Area, usually 1024 x 1024.
 */
public class Patch {
	private IntTable background;
	private IntTable foreground;
	private HashMap<Vector2i, TileEntity> tileEntities = new HashMap<>();
	private HashMap<Vector2i, TileEntity> topTileEntities = new HashMap<>();
	
	public Patch(int width, int height) {
		background = new IntTable(width, height);
		//Background is zeroes by default
		foreground = new IntTable(width, height);
		foreground.clear(-1);
	}
	
	public int width() {
		return background.width();
	}
	
	public int height() {
		return background.height();
	}
	
	public int background(int x, int y) {
		return background.getTile(x, y);
	}
	
	public OptionalInt foreground(int x, int y) {
		if (x<0 || y<0 || x>=foreground.width || y>=foreground.height) return OptionalInt.empty();
		int value = foreground.getTile(x, y);
		if (value == -1) return OptionalInt.empty(); //TODO: allow IntTable to be sparse somehow?
		return OptionalInt.of(value);
	}
	
	public void setBackground(int x, int y, int value) {
		background.set(x, y, value);
	}
	
	public void setForeground(int x, int y, int value) {
		foreground.set(x, y, value);
	}
	
	public void setForeground(int x, int y, OptionalInt value) {
		foreground.set(x, y, value.orElse(-1));
	}
	
	public void clearForeground(int x, int y) {
		foreground.set(x, y, -1);
	}
	
	public Optional<TileEntity> tileEntityAt(Vector2i pos) {
		return Optional.ofNullable(tileEntities.get(pos));
	}
	
	public Optional<TileEntity> tileEntityAt(int x, int y) {
		return tileEntityAt(new Vector2i(x, y));
	}
	
	public void setTileEntity(Vector2i pos, TileEntity te) {
		tileEntities.put(pos, te);
	}
	
	public void setTileEntity(Vector2i pos, Optional<TileEntity> te) {
		te.ifPresentOrElse(
			it -> tileEntities.put(pos, it),
			() -> tileEntities.remove(pos)
		);
	}
	
	public Optional<TileEntity> flagAt(Vector2i pos) {
		return Optional.ofNullable(topTileEntities.get(pos));
	}
	
	public boolean isFlagged(int x, int y) {
		return topTileEntities.get(new Vector2i(x, y)) != null;
	}
	
	public Optional<TileEntity> flagAt(int x, int y) {
		return flagAt(new Vector2i(x, y));
	}
	
	public void setFlag(Vector2i pos, Optional<TileEntity> te) {
		te.ifPresentOrElse(
			it -> topTileEntities.put(pos, it),
			() -> topTileEntities.remove(pos)
		);
	}
	
	public void forEachTopTileEntity(BiConsumer<Vector2i, TileEntity> consumer) {
		topTileEntities.forEach(consumer);
	}
}
