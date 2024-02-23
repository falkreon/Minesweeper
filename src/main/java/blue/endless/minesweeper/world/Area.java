package blue.endless.minesweeper.world;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Stream;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public class Area {
	
	private Tile baseBackgroundTile = new Tile();
	private Tile baseForegroundTile = new Tile();
	private Tile missingTile = new Tile();
	
	private Int2ObjectOpenHashMap<Tile> tileset = new Int2ObjectOpenHashMap<>();
	private Map<Vector2i, TileEntity> tileEntities = new HashMap<>();
	private AreaGenerator generator;
	
	/**
	 * In the future, Area will disambiguate to several Patches, one per 1024x1024 "patch"
	 */
	private Patch patch;
	
	public Area() {
		baseForegroundTile.setForeground(true);
		
		tileset.put(-1, missingTile);
		tileset.put(0, baseBackgroundTile);
		tileset.put(1, baseForegroundTile);
		
		patch = new Patch(1024, 1024);
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
		return tileset.int2ObjectEntrySet().stream()
			.filter(it -> it.getValue() == tile)
			.mapToInt(it -> it.getIntKey())
			.findFirst();
	}
	
	//FIXME: THIS IS TEMPORARY
	public void generate() {
		generator.generate(this, patch, 0, 0);
	}
	
	public Optional<Tile> getForegroundTile(int x, int y) {
		OptionalInt rawId = patch.foreground(x, y);
		if (rawId.isEmpty()) return Optional.empty();
		return Optional.of(tileset.getOrDefault(rawId.getAsInt(), missingTile));
	}
	
	public Tile getBackgroundTile(int x, int y) {
		int rawTile = patch.background(x, y);
		if (rawTile < 0) return baseForegroundTile;
		return tileset.getOrDefault(rawTile, missingTile);
	}
	
	public void setTileEntity(int x, int y, TileEntity entity) {
		Vector2i pos = new Vector2i(x, y);
		patch.setTileEntity(pos, entity);
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
