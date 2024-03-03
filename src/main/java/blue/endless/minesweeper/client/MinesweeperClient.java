package blue.endless.minesweeper.client;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.glfw.GLFW;

import com.playsawdust.glow.Timing;
import com.playsawdust.glow.gl.Texture;
import com.playsawdust.glow.gl.Window;
import com.playsawdust.glow.gl.WindowPainter;
import com.playsawdust.glow.image.ImageData;
import com.playsawdust.glow.image.ImagePainter;
import com.playsawdust.glow.image.SrgbImageData;
import com.playsawdust.glow.image.color.BlendMode;

import blue.endless.minesweeper.Identifier;
import blue.endless.minesweeper.ImageSupplier;
import blue.endless.minesweeper.Minesweeper;
import blue.endless.minesweeper.world.Area;
import blue.endless.minesweeper.world.BaseAreaGenerator;
import blue.endless.minesweeper.world.FreeEntity;
import blue.endless.minesweeper.world.Vector2i;

public class MinesweeperClient {
	private static Area mainArea = null;
	
	private static int scale = 2;
	private static int scrollx = 0;
	private static int scrolly = 0;
	
	private static Map<Vector2i, Texture> bakedTiles = new HashMap<>(); //TODO: Replace with LRU cache?
	private static FreeEntity player = new FreeEntity();
	
	private static ImageSupplier images;
	
	private static Controls controls = new Controls();
	
	public void init() {
		Thread.currentThread().setName("Render thread");
		Window gameWindow = new Window("Minesweeper");
		
		gameWindow.setVisible(true);
		
		mainArea = new Area();
		
		player.setPosition(32*16, 32*16);
		mainArea.addEntity(player);
		
		mainArea.setGenerator(new BaseAreaGenerator());
		mainArea.generate(); //FIXME: This is temporary! In reality we will generate a patch at a time!
		Minesweeper.LOGGER.info("Doing initial reveal...");
		mainArea.revealAndChain(32, 32);
		Minesweeper.LOGGER.info("Map Complete.");
		
		images = new ImageSupplier();
		
		Minesweeper.LOGGER.info("Baking patches...");
		bakeAll(images, mainArea);
		Minesweeper.LOGGER.info("Complete.");
		
		//Minesweeper.LOGGER.info("Baking patch...");
		//bakedPatch = bake(images, mainArea, 0, 0, 32, 32);
		//Minesweeper.LOGGER.info("Complete.");
		
		gameWindow.onLeftMouseUp().register((x, y) -> {
			int tileX = ((x + scrollx) / scale) / 16;
			int tileY = ((y + scrolly) / scale) / 16;
			//Minesweeper.LOGGER.info("Handling click at tile "+tileX+", "+tileY+"...");
			mainArea.revealAndChain(tileX, tileY);
			
			Minesweeper.LOGGER.info("Rebaking at tile "+tileX+", "+tileY+"...");
			rebake(images, mainArea, tileX, tileY);
			Minesweeper.LOGGER.info("Complete.");
		});
		
		gameWindow.onRightMouseUp().register((x, y) -> {
			int tileX = ((x + scrollx) / scale) / 16;
			int tileY = ((y + scrolly) / scale) / 16;
			
			mainArea.flag(tileX, tileY, !mainArea.isFlagged(tileX, tileY));
			Minesweeper.LOGGER.info("Rebaking at tile "+tileX+", "+tileY+"...");
			rebake(images, mainArea, tileX, tileY);
			Minesweeper.LOGGER.info("Complete.");
			
			//bakedPatch.destroy();
			//bakedPatch = bake(images, mainArea, 0, 0, 32, 32);
		});
		
		gameWindow.onKeyDown().register(controls::keyDown);
		gameWindow.onKeyUp().register(controls::keyUp);
		
		controls.get("left").bindKey(GLFW.GLFW_KEY_A);
		controls.get("right").bindKey(GLFW.GLFW_KEY_D);
		controls.get("up").bindKey(GLFW.GLFW_KEY_W);
		controls.get("down").bindKey(GLFW.GLFW_KEY_S);
		/*
		try {
			tileset = PngImageIO.load(
				DataSlice.of(Files.readAllBytes(Path.of("tiles.png")))
				);
		} catch (IOException e) {
			e.printStackTrace();
			tileset = new SrgbImageData(16, 16);
		}*/
		
		//texture = new Texture(tileset);
		
		Timing timing = new Timing()
			.setFrameCallback((t) -> { frame(t, gameWindow); })
			.setTickCallback(MinesweeperClient::tick)
			.setNonFrameCallback(gameWindow::poll);
		
		while(!gameWindow.shouldClose()) {
			timing.runCycle();
		}
		
		gameWindow.setVisible(false);
		gameWindow.delete();
	}
	
	public void bakeAll(ImageSupplier images, Area area) {
		int tilesWide = area.getWidth() / 32;
		int tilesHigh = area.getHeight() / 32;
		
		for(int y=0; y<tilesHigh; y++) {
			for(int x=0; x<tilesWide; x++) {
				Texture t = bake(images, area, x*32, y*32, 32, 32);
				
				bakedTiles.put(new Vector2i(x, y), t);
			}
		}
	}
	
	public void rebake(ImageSupplier images, Area area, int x, int y) {
		if (x<0 || y<0 || x>=area.getWidth() || y>=area.getHeight()) return;
		
		int tx = x / 32;
		int ty = y / 32;
		Vector2i pos = new Vector2i(tx, ty);
		
		Texture t = bakedTiles.remove(pos);
		if (t != null) t.destroy();
		
		Minesweeper.LOGGER.info("Putting offset: "+(tx*32)+", "+(ty*32)+" pos: "+pos);
		
		bakedTiles.put(pos, bake(images, area, tx * 32, ty * 32, 32, 32));
	}
	
	public Texture bake(ImageSupplier images, Area area, int xofs, int yofs, int width, int height) {
		Minesweeper.LOGGER.info("Baking at offset "+xofs+", "+yofs);
		SrgbImageData data = new SrgbImageData(width * 16, height * 16);
		ImagePainter painter = new ImagePainter(data, BlendMode.NORMAL);
		
		//ImageData emptyBg = images.getImage(Identifier.of("ms:tiles.png:13"));
		ImageData num0 = images.getImage(Identifier.of("ms:tiles.png:10"));
		ImageData num1 = images.getImage(Identifier.of("ms:tiles.png:2"));
		ImageData num2 = images.getImage(Identifier.of("ms:tiles.png:3"));
		ImageData num3 = images.getImage(Identifier.of("ms:tiles.png:4"));
		ImageData num4 = images.getImage(Identifier.of("ms:tiles.png:5"));
		ImageData num5 = images.getImage(Identifier.of("ms:tiles.png:6"));
		ImageData num6 = images.getImage(Identifier.of("ms:tiles.png:7"));
		ImageData num7 = images.getImage(Identifier.of("ms:tiles.png:8"));
		ImageData num8 = images.getImage(Identifier.of("ms:tiles.png:9"));
		
		ImageData fg = images.getImage(Identifier.of("ms:tiles.png:0"));
		
		ImageData flag = images.getImage(Identifier.of("ms:tiles.png:1"));
		
		for(int y=0; y<height; y++) {
			for(int x=0; x<width; x++) {
				
				if (area.getForegroundTile(x + xofs, y + yofs).isPresent()) { //TODO: Get the tile instead and grab the image ID from it
					painter.drawImage(fg, x * 16, y * 16);
					
					if(area.isFlagged(x + xofs, y + yofs)) {
						painter.drawImage(flag, x * 16, y * 16);
					}
					
					continue;
				}
				
				int mineCount = area.adjacentMineCount(x + xofs, y + yofs);
				
				painter.drawImage(num0, x * 16, y * 16);
				
				if (area.getTileEntity(x + xofs, y + yofs).isPresent()) {
					painter.drawImage(flag, x * 16, y * 16);
				} else {
					if (mineCount > 0) {
						ImageData numImage = switch(mineCount) {
							case 1 -> num1;
							case 2 -> num2;
							case 3 -> num3;
							case 4 -> num4;
							case 5 -> num5;
							case 6 -> num6;
							case 7 -> num7;
							case 8 -> num8;
							default -> num0;
						};
						painter.drawImage(numImage, x * 16, y * 16);
					}
				}
				
			}
		}
		
		return new Texture(data);
	}
	
	public static void frame(Timing timing, Window gameWindow) {
		gameWindow.clear(0, 0, 0, 0);
		
		WindowPainter g = gameWindow.startDrawing();
		
		int tilesWide = mainArea.getWidth() / 32;
		int tilesHigh = mainArea.getHeight() / 32;
		
		ImageData image = images.getImage(Identifier.of("ms:player.png")); //TODO: Texture supplier to sit alongside the image supplier
		
		double t = timing.getPartialTick();
		if (t>1D) t=1D; if (t<0D) t=0D;
		Vector2i vec = player.position(t);
		
		/*
		 * Okay folks, the goal is to lock the camera (scrollx, scrolly) onto the center of the character.
		 * This means ADDING the character's center, and then SUBTRACTING half the window dimensions
		 */
		int halfWidth = gameWindow.getWidth() / 2;
		int halfHeight = gameWindow.getHeight() / 2;
		
		scrollx = (vec.x() * scale) - halfWidth; //Center of character is at the center of its sprite
		scrolly = ((vec.y() - (image.getHeight() / 2)) * scale) - halfHeight; //Center of character is halfway up the sprite from the entity location.
		//System.out.println("L: "+player.lastPosition()+" N: "+player.position()+" I: "+vec+" S: "+scrollx+", "+scrolly+" T:"+t);
		
		for(int y=0; y<tilesHigh; y++) {
			for(int x=0; x<tilesWide; x++) {
				Texture tile = bakedTiles.get(new Vector2i(x, y));
				if (tile != null) {
					g.drawImage(tile, (x * 16 * 32 * scale) - scrollx, (y * 16 * 32 * scale) - scrolly, tile.getWidth() * scale, tile.getHeight() * scale, 0, 0, tile.getWidth(), tile.getHeight(), 1.0f);
				}
			}
		}
		
		for(FreeEntity entity : mainArea.entities()) {
			//TODO: Grab texture for this entity
			Vector2i screenPosition = player.position(t).multiply(scale).add(-scrollx, -scrolly);
			int halfImageWidth = image.getWidth() / 2;
			
			g.drawImage(image, screenPosition.x() - halfImageWidth, screenPosition.y() - image.getHeight(), image.getWidth() * scale, image.getHeight() * scale, 0, 0, image.getWidth(), image.getHeight(), 1.0f);
		}
		
		gameWindow.swapBuffers();
	}
	
	public static void tick(Timing t) {
		for(FreeEntity entity : mainArea.entities()) {
			entity.catchUp();
		}
		
		//player.catchUp();
		
		if (controls.get("right").isPressed()) {
			//TODO: Check for collisions
			player.moveTo(player.position().add(8, 0));
		}
		
		if (controls.get("left").isPressed()) {
			//TODO: Check for collisions
			player.moveTo(player.position().add(-8, 0));
		}
		
		if (controls.get("up").isPressed()) {
			//TODO: Check for collisions
			player.moveTo(player.position().add(0, -8));
		}
		
		if (controls.get("down").isPressed()) {
			//TODO: Check for collisions
			player.moveTo(player.position().add(0, 8));
		}
		
		
	}

}
