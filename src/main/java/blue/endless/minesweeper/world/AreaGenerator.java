package blue.endless.minesweeper.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface AreaGenerator {
	
	/**
	 * Initializes
	 * This method MUST be called before any other methods are valid on this interface.
	 * @param area The Area (world? dimension?) being generated in. This instance will ONLY be used for this Area.
	 */
	public void startGenerating(Area area);
	
	/**
	 * Generates tiles, mines, and structures.
	 * @param table The raw numeric tile values for this region of space. Ask Area to translate tile IDs.
	 * @param xofs  The x coordinate of the leftmost column of tiles in the IntTable
	 * @param yofs  The y coordinate of the topmost row of tiles in the IntTable
	 */
	public default void generate(Area area, IntTable background, IntTable foreground, int xofs, int yofs) {
		for(int y=0; y<background.width(); y++) {
			for(int x=0; x<background.height(); x++) {
				int tileId = generateBackgroundTile(xofs+x, yofs+y);
				background.set(x, y, tileId);
				
				tileId = generateForegroundTile(xofs+x, yofs+y);
				foreground.set(x, y, tileId);
				
				Optional<TileEntity> te = generateTileEntity(xofs+x, yofs+y);
				if (te.isPresent()) {
					area.setTileEntity(xofs+x, yofs+y, te.get());
				}
			}
		}
		
		ArrayList<FreeEntity> entities = new ArrayList<>();
		generateFreeEntities(xofs, yofs, background.width(), background.height(), entities);
	}
	
	public int generateBackgroundTile(int x, int y);
	public int generateForegroundTile(int x, int y);
	public Optional<TileEntity> generateTileEntity(int x, int y);
	public void generateFreeEntities(int xofs, int yofs, int width, int height, List<FreeEntity> entities);
}
