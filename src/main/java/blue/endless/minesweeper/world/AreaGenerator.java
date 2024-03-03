package blue.endless.minesweeper.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface AreaGenerator {
	
	/**
	 * Initializes this AreaGenerator. This is a great moment for a Generator to select a RandomGenerator method, select
	 * tiles for use, and initialize or acquire subordinate feature generators. It is also the only notice the Generator
	 * gets which is appropriate for doing whole-Area planning for large-scale features.
	 * 
	 * <p>This method MUST be called before any other methods are valid on this interface.
	 * @param area The Area being generated in. This instance MUST only be used for this Area.
	 */
	public void startGenerating(Area area);
	
	/**
	 * Generates tiles, mines, and structures. This method MAY also request/discover additional Tiles from the Area.
	 * This method is called frequently during map initialization and generation
	 * @param area  The same area this generator was initialized with, for convenience.
	 * @param patch The tile values for this region of space. Each Area has unique Tile instances, so ask area for Tiles to use
	 * @param xofs  The x coordinate of the leftmost column of tiles in the IntTable
	 * @param yofs  The y coordinate of the topmost row of tiles in the IntTable
	 */
	public default void generate(Area area, Patch patch, int xofs, int yofs) {
		int missingId = area.getId(area.getMissingTile()).orElse(0);
		
		for(int y=0; y<patch.width(); y++) {
			for(int x=0; x<patch.height(); x++) {
				
				Tile bg = generateBackgroundTile(xofs+x, yofs+y);
				patch.setBackground(x, y, area.getId(bg).orElse(missingId));
				
				Optional<Tile> fg = generateForegroundTile(xofs+x, yofs+y);
				if (fg.isPresent()) {
					patch.setForeground(x, y, area.getId(fg.get()));
				} else {
					patch.clearForeground(x, y);
				}
			}
		}
		
		allocateBombs(bombsPerPatch(), area, patch, xofs, yofs);
		
		ArrayList<FreeEntity> entities = new ArrayList<>();
		generateFreeEntities(xofs, yofs, patch.width(), patch.height(), entities);
	}
	
	public int bombsPerPatch();
	public int allocateBombs(int bombCount, Area area, Patch patch, int xofs, int yofs);
	
	public Tile generateBackgroundTile(int x, int y);
	public Optional<Tile> generateForegroundTile(int x, int y);
	public void generateFreeEntities(int xofs, int yofs, int width, int height, List<FreeEntity> entities);
}
