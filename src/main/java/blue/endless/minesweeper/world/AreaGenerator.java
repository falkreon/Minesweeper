package blue.endless.minesweeper.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface AreaGenerator {
	
	/**
	 * Initializes this AreaGenerator.
	 * This method MUST be called before any other methods are valid on this interface.
	 * @param area The Area being generated in. This instance MUST only be used for this Area.
	 */
	public void startGenerating(Area area);
	
	/**
	 * Generates tiles, mines, and structures.
	 * @param table The raw numeric tile values for this region of space. Ask Area to translate tile IDs.
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
		
		ArrayList<FreeEntity> entities = new ArrayList<>();
		generateFreeEntities(xofs, yofs, patch.width(), patch.height(), entities);
	}
	
	public Tile generateBackgroundTile(int x, int y);
	public Optional<Tile> generateForegroundTile(int x, int y);
	public void generateFreeEntities(int xofs, int yofs, int width, int height, List<FreeEntity> entities);
}
