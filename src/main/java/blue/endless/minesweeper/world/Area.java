package blue.endless.minesweeper.world;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Stream;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public class Area {
	private record Vector2i(int x, int y) {};
	
	private Tile baseBackgroundTile = new Tile();
	private Tile baseForegroundTile = new Tile();
	private Tile missingTile = new Tile();
	
	private Int2ObjectOpenHashMap<Tile> tileset = new Int2ObjectOpenHashMap<>();
	private Map<Vector2i, TileEntity> tileEntities = new HashMap<>();
	private AreaGenerator generator;
	
	/**
	 * In the future, Area will disambiguate to several IntTables, one per 1024x1024 "patch"
	 */
	private IntTable foreground;
	private IntTable background;
	
	public Area() {
		baseForegroundTile.setForeground(true);
		
		tileset.put(-1, missingTile);
		tileset.put(0, baseBackgroundTile);
		tileset.put(1, baseForegroundTile);
		
		background = new IntTable(1024, 1024);
		foreground = new IntTable(1024, 1024);
	}
	
	public Tile getMissingTile() {
		return missingTile;
	}
	
	public Tile baseForegroundTile() {
		return baseForegroundTile;
	}
	
	public Tile baseBackgroundTile() {
		return baseBackgroundTile;
	}
	
	//TODO: Some way for apps to query all tiles and find a specific, desired tile.
	/*
	 * If people really need to, as long as they have access to all the tiles, they can mapreduce them.
	 * 
	 */
	public Stream<Tile> tiles() {
		return tileset.values().stream();
	}
	
	public OptionalInt getId(Tile tile) {
		Optional<Int2ObjectMap.Entry<Tile>> opt = tileset.int2ObjectEntrySet().stream()
			.filter(it -> it.getValue() == tile)
			.findFirst();
		
		//No good way to skip Optional<Integer> and go straight to OptionalInt
		if (opt.isPresent()) return OptionalInt.of(opt.get().getIntKey());
		return OptionalInt.empty();
	}
	
	//FIXME: THIS IS TEMPORARY
	public void generate() {
		generator.generate(this, background, foreground, 0, 0);
	}
	
	public Tile getBackgroundTile(int x, int y) {
		int i = background.getTile(x, y);
		if (i<0) return baseForegroundTile;
		return tileset.getOrDefault(i, missingTile);
	}
	
	public void setTileEntity(int x, int y, TileEntity entity) {
		Vector2i pos = new Vector2i(x, y);
		tileEntities.put(pos, entity);
	}
	
	public void setTileEntity(int x, int y, Optional<TileEntity> optEntity) {
		Vector2i pos = new Vector2i(x, y);
		optEntity.ifPresentOrElse(
			it -> tileEntities.put(pos, it),
			() -> tileEntities.remove(pos)
		);
	}
	
	public Optional<TileEntity> getTileEntity(int x, int y) {
		return Optional.ofNullable(tileEntities.get(new Vector2i(x, y)));
	}

	public void setGenerator(AreaGenerator generator) {
		this.generator = generator;
	}
}
