package blue.endless.minesweeper.world;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;
import java.util.stream.Stream;

import blue.endless.jankson.api.document.PrimitiveElement;
import blue.endless.minesweeper.world.te.FlagTileEntity;
import blue.endless.minesweeper.world.te.TileEntity;
import blue.endless.tinyevents.function.IntBiConsumer;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public class Area {
	private long seed = 4L; // Chosen by fair diceroll, guaranteed to be random. See https://xkcd.com/221/
	private Tile baseBackgroundTile = new Tile();
	private Tile baseForegroundTile = new Tile();
	private Tile missingTile = new Tile();
	
	private Int2ObjectOpenHashMap<Tile> tileset = new Int2ObjectOpenHashMap<>();
	private AreaGenerator generator;
	
	private final int patchSize;
	private final int patchesWide;
	private final int patchesHigh;
	
	List<FreeEntity> entities = new ArrayList<>();
	
	private boolean isClear = false;
	private int totalMines = 0;
	private int totalFlags = 0;
	private int wrongFlags = 0;
	private int totalPoints = 0; //TODO: Move this to a per-player object
	
	/**
	 * In the future, Area will disambiguate to several Patches, one per 1024x1024 "patch"
	 */
	private Patch patch;
	
	public Area() {
		//patchSize = 1024;
		patchSize = 64;
		patchesWide = 1;
		patchesHigh = 1;
		
		baseForegroundTile.setForeground(true);
		
		tileset.put(-1, missingTile);
		tileset.put(0, baseBackgroundTile);
		baseBackgroundTile.data().put("texture", PrimitiveElement.of("ms:textures/tiles.png:10"));
		tileset.put(1, baseForegroundTile);
		baseForegroundTile.data().put("texture", PrimitiveElement.of("ms:textures/tiles.png:0"));
		
		patch = new Patch(patchSize, patchSize);
	}
	
	public void chooseRandomSeed() {
		this.seed = RandomGenerator.of("Xoshiro256PlusPlus").nextLong();
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
	
	public long getSeed() {
		return seed;
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
	
	public void addEntity(FreeEntity entity) {
		entities.add(entity);
	}
	
	public void removeEntity(FreeEntity entity) {
		entities.remove(entity);
	}
	
	public List<FreeEntity> entities() {
		return List.copyOf(entities);
	}
	
	public void removeEntityIf(Predicate<FreeEntity> predicate) {
		Iterator<FreeEntity> iterator = entities.iterator();
		while (iterator.hasNext()) {
			FreeEntity cur = iterator.next();
			if (predicate.test(cur)) iterator.remove();
		}
	}
	
	//FIXME: THIS IS TEMPORARY
	public void generate() {
		generator.generate(this, patch, 0, 0);
	}
	
	public void receiveGeneratedMines(int mineCount) {
		totalMines += mineCount;
	}
	
	public int adjacentMineCount(Vector2i pos) {
		return adjacentMineCount(pos.x(), pos.y());
	}
	
	public int adjacentMineCount(int x, int y) {
		int result = 0;
		for(int dy=-1; dy<=1; dy++) {
			for(int dx=-1; dx<=1; dx++) {
				if (dx==0 && dy==0) continue;
				if (getTileEntity(x + dx, y + dy).filter(TileEntity::isBomb).isPresent()) {
					result++;
				}
			}
		}
		
		return result;
	}
	
	public void revealSimple(int x, int y) {
		patch.clearForeground(x, y);
		patch.setFlag(new Vector2i(x, y), Optional.empty()); //Clear flag if there is one here
	}
	
	private static final int MAX_ITERATIONS = 5000;
	/**
	 * Reveals the indicated tile
	 * @param x
	 * @param y
	 * @param markDirtyCallback
	 */
	public int revealAndChain(int x, int y, IntBiConsumer markDirtyCallback) {
		Optional<TileEntity> toUncover = getTileEntity(x, y);
		if (toUncover.isPresent()) {
			//TODO: Losing the Game
			
			return 0;
		} else {
			//Figure out if this tile is covered and if there's nothing to reveal, early-out
			if (patch.foreground(x, y).isEmpty()) return 0;
			
			//Do a regular recursive search
			Deque<Vector2i> queue = new ArrayDeque<>();
			Set<Vector2i> searched = new HashSet<>();
			queue.add(new Vector2i(x, y));
			searched.add(new Vector2i(x, y));
			
			int uncovered = 0;
			
			int iterations = 0;
			while(!queue.isEmpty() && iterations < MAX_ITERATIONS) {
				Vector2i pos = queue.removeFirst();
				
				//Even if this tile would normally be eligible for uncovering, don't uncover it if it's flagged.
				if (patch.isFlagged(pos.x(), pos.y())) continue;
				
				//If at any point we arrive at an uncovered tile, stop and search somewhere else.
				if (patch.foreground(pos.x(), pos.y()).isPresent()) {
					
					int countAtLocation = adjacentMineCount(pos);
					
					revealSimple(pos.x(), pos.y());
					uncovered++;
					markDirtyCallback.accept(pos.x(), pos.y());
					if (countAtLocation == 0) {
						for(int dy=-1; dy<=1; dy++) {
							for(int dx=-1; dx<=1; dx++) {
								if (dx==0 && dy==0) continue;
								Vector2i v = pos.add(dx, dy);
								if (!searched.contains(v)) {
									queue.addLast(v);
									searched.add(v);
								}
							}
						}
					}
				}
				iterations++;
			}
			
			return uncovered;
		}
	}
	
	public int getWidth() {
		return patchesWide * patchSize;
	}
	
	public int getHeight() {
		return patchesHigh * patchSize;
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
		patch.setTileEntity(pos, optEntity);
	}
	
	public Optional<TileEntity> getTileEntity(int x, int y) {
		return patch.tileEntityAt(x, y);
	}

	public void setGenerator(AreaGenerator generator) {
		this.generator = generator;
		generator.startGenerating(this);
	}

	public boolean isFlagged(int x, int y) {
		return patch.isFlagged(x, y);
	}
	
	public boolean isBomb(int x, int y) {
		return patch.tileEntityAt(x, y).map(TileEntity::isBomb).orElse(false);
	}
	
	public void flag(int x, int y, boolean flag, IntBiConsumer markDirtyCallback) {
		if (patch.foreground(x, y).isEmpty()) return; //Can't flag empty tiles
		
		if (isClear) return;
		
		if (flag) {
			if (!patch.isFlagged(x, y)) {
				if (!isBomb(x, y)) wrongFlags++;
				
				patch.setFlag(new Vector2i(x, y), Optional.of(new FlagTileEntity())); // TODO: Record our flag image
				this.totalFlags++;
			}
		} else {
			if (patch.isFlagged(x, y)) {
				if (!isBomb(x, y)) wrongFlags--;
				
				patch.setFlag(new Vector2i(x, y), Optional.empty());
				this.totalFlags--;
			}
		}
		
		if (totalMines == totalFlags && wrongFlags == 0) {
			isClear = true;
			//TODO: Remove all flags, replace all bombs with collectables, and probably mark everything dirty.
			
			//System.out.println("Game is complete. Doing swaps...");
			
			record FlagSwap(Vector2i pos, FlagTileEntity flag) {};
			ArrayList<FlagSwap> swaps = new ArrayList<>();
			
			forEachFlag((pos, f) -> {
				swaps.add(new FlagSwap(pos, f));
			});
			
			for(FlagSwap swap : swaps) {
				//TODO: Maybe reward flag's owner?
				patch.setFlag(swap.pos(), Optional.empty());
				patch.setTileEntity(swap.pos(), Optional.of(new TileEntity())); // TODO: Replace with points entity
				patch.clearForeground(swap.pos().x(), swap.pos().y());
				//System.out.println("Marking "+swap.pos()+" dirty...");
				markDirtyCallback.accept(swap.pos().x(), swap.pos().y());
			}
			
			//System.out.println("Swaps complete.");
		}
		
		markDirtyCallback.accept(x, y);
	}
	
	public void forEachFlag(BiConsumer<Vector2i, FlagTileEntity> consumer) {
		//for each patch
		int xofs = 0;
		int yofs = 0;
		patch.forEachTopTileEntity((pos, te) -> {
			if (te instanceof FlagTileEntity flag) {
				consumer.accept(pos.add(xofs, yofs), flag);
			}
		});
	}

	public int mineCount() {
		return totalMines;
	}
	
	public int flagCount() {
		return totalFlags;
	}
	
	public void addPoints(int toAdd) {
		this.totalPoints += toAdd;
	}
	
	public int points() {
		return totalPoints;
	}

	public int wrongFlags() {
		return wrongFlags;
	}
	
	public boolean isClear() {
		return isClear;
	}
}
