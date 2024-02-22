package blue.endless.minesweeper.world;

import java.util.List;
import java.util.Optional;
import java.util.random.RandomGenerator;

public class BaseAreaGenerator implements AreaGenerator {
	private Area area;
	private RandomGenerator rnd;
	private float bombThreshold = 0.2f;
	
	@Override
	public void startGenerating(Area area) {
		this.area = area;
		rnd = RandomGenerator.of("Xoshiro256PlusPlus");
	}

	@Override
	public int generateBackgroundTile(int x, int y) {
		
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public int generateForegroundTile(int x, int y) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public Optional<TileEntity> generateTileEntity(int x, int y) {
		if (rnd.nextFloat() < bombThreshold) {
			return Optional.of(new TileEntity());
		}
		
		return Optional.empty();
	}

	@Override
	public void generateFreeEntities(int xofs, int yofs, int width, int height, List<FreeEntity> entities) {
		//Do nothing.
	}
}
