package blue.endless.minesweeper.world;

import java.util.List;
import java.util.Optional;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

import blue.endless.minesweeper.Minesweeper;
import blue.endless.minesweeper.world.te.BombTileEntity;
import blue.endless.minesweeper.world.te.TileEntity;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

public class BaseAreaGenerator implements AreaGenerator {
	public static final int TERRAIN_SEED = 0;
	public static final int BOMB_SEED = 1;
	
	private Area area;
	RandomGeneratorFactory<RandomGenerator> randomFactory = RandomGeneratorFactory.of("Xoshiro256PlusPlus");
	private double bombCoverage = 0.08f;
	private long bombsPerPatch = 0L;
	
	@Override
	public void startGenerating(Area area) {
		this.area = area;
		
		bombsPerPatch = (long) (bombCoverage * 64.0 * 64.0);
	}
	
	@Override
	public int bombsPerPatch() {
		return (int) bombsPerPatch;
	}

	@Override
	public int allocateBombs(int bombCount, Area area, Patch patch, int xofs, int yofs) {
		
		//overwrite bombCount for now
		bombCount = (int) (bombCoverage * patch.width() * patch.height());
		
		Minesweeper.LOGGER.info("Allocating " + bombCount + " bombs...");
		
		RandomGenerator generator = getPatchRandom(xofs, yofs, BOMB_SEED);
		int allocated = 0;
		
		int mx = patch.width() / 2; //mx = 32;
		int my = patch.height() / 2; //my = 32;
		
		final int originalBombCount = bombCount;
		
		//TODO: If we have a lot of bombs to place, try to bulk-allocate some with the shotgun method.
		if (bombCount > 0) {
			for(int i=0; i<bombCount * 10; i++) {
				int x = generator.nextInt(patch.width());
				int y = generator.nextInt(patch.height());
				if (isValidBombLocation(x, y, patch, mx, my)) {
					addBomb(patch, x, y);
					bombCount--;
					allocated++;
					if (allocated >= originalBombCount) break;
				}
			}
		}
		
		Minesweeper.LOGGER.info(allocated + " bombs shotgunned. Calculating for comprehensive allocation...");
		
		IntList validLocations = new IntArrayList();
		for(int y=0; y<patch.height(); y++) {
			for(int x=0; x<patch.width(); x++) {
				if (isValidBombLocation(x, y, patch, mx, my)) {
					validLocations.add(y * patch.width() + x);
				}
			}
		}
		
		Minesweeper.LOGGER.info("Allocating " + bombCount + " remaining bombs...");
		
		for(int i=0; i<bombCount; i++) {
			if (validLocations.size() < 1) break;
			
			int pick = validLocations.removeInt(generator.nextInt(validLocations.size()));
			addBomb(patch, pick);
			allocated++;
			
		}
		
		area.receiveGeneratedMines(allocated);
		Minesweeper.LOGGER.info(allocated + " bombs allocated and placed.");
		
		return allocated;
	}
	
	private boolean isValidBombLocation(int locationIndex, Patch patch, int mx, int my) {
		int y = locationIndex / patch.width();
		int x = locationIndex % patch.width();
		return isValidBombLocation(x, y, patch, mx, my);
	}
	
	private boolean isValidBombLocation(int x, int y, Patch patch, int mx, int my) {
		if (Math.abs(x-mx) <= 1 && Math.abs(y-my) <= 1) return false;
		return !patch.tileEntityAt(x, y).isPresent();
	}
	
	private void addBomb(Patch p, int x, int y) {
		TileEntity bomb = new BombTileEntity();
		p.setTileEntity(new Vector2i(x, y), bomb);
	}
	
	private void addBomb(Patch p, int locationIndex) {
		int y = locationIndex / p.width();
		int x = locationIndex % p.width();
		addBomb(p, x, y);
	}

	@Override
	public Tile generateBackgroundTile(int x, int y) {
		return area.baseBackgroundTile();
	}

	@Override
	public Optional<Tile> generateForegroundTile(int x, int y) {
		return Optional.of(area.baseForegroundTile());
		//return Optional.empty();
	}
	
	@Override
	public void generateFreeEntities(int xofs, int yofs, int width, int height, List<FreeEntity> entities) {
		//Do nothing.
	}
	
	public static long getPatchSeed(int xofs, int yofs, int index) {
		final long locationHash = (xofs * 31L) ^ yofs;
		final long hash = (locationHash * 31L) ^ index;
		return hash;
	}
	
	public RandomGenerator getPatchRandom(int xofs, int yofs, int index) {
		return randomFactory.create(getPatchSeed(xofs, yofs, index));
	}
}
