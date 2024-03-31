package blue.endless.minesweeper.client;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Optional;

import org.lwjgl.glfw.GLFW;

import com.playsawdust.glow.Cache;
import com.playsawdust.glow.Timing;
import com.playsawdust.glow.gl.Texture;
import com.playsawdust.glow.gl.Window;
import com.playsawdust.glow.gl.WindowPainter;
import com.playsawdust.glow.image.ImageData;
import com.playsawdust.glow.image.ImagePainter;
import com.playsawdust.glow.image.SrgbImageData;
import com.playsawdust.glow.image.color.BlendMode;
import com.playsawdust.glow.image.color.RGBColor;
import com.playsawdust.glow.vecmath.Vector2d;

import blue.endless.minesweeper.Identifier;
import blue.endless.minesweeper.ImageSupplier;
import blue.endless.minesweeper.Minesweeper;
import blue.endless.minesweeper.Resources;
import blue.endless.minesweeper.Resources.Resource;
import blue.endless.minesweeper.world.Area;
import blue.endless.minesweeper.world.BaseAreaGenerator;
import blue.endless.minesweeper.world.FreeEntity;
import blue.endless.minesweeper.world.Vector2i;
import blue.endless.minesweeper.world.te.FlagTileEntity;
import blue.endless.minesweeper.world.te.TileEntity;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;

public class MinesweeperClient {
	private static Area mainArea = null;
	
	private static int scale = 2;
	private static int scrollx = 0;
	private static int scrolly = 0;
	
	private static final int BAKE_SIZE = 32;
	
	private static Cache<Vector2i, Texture> bakedTiles = new Cache<>(300);
	private static ArrayDeque<Vector2i> dirtyTiles = new ArrayDeque<>();
	private static FreeEntity player = new FreeEntity();
	
	private static ImageSupplier images;
	
	private static Controls controls = new Controls();
	
	private static BitmapFont font = new BitmapFont();
	
	private static int inferenceLevel = 1;
	
	public void init() {
		Thread.currentThread().setName("Render thread");
		Window gameWindow = new Window("Minesweeper");
		
		gameWindow.setVisible(true);
		
		Resources.init();
		images = new ImageSupplier();
		
		Minesweeper.LOGGER.info("Loading Unifont...");
		Optional<Resource> fontResource = Resources.get(Identifier.of("ms:unifont.hex")).stream().findFirst();
		if (fontResource.isEmpty()) throw new RuntimeException("Unifont not found.");
		try(InputStream unifont = Files.newInputStream(fontResource.get().path(), StandardOpenOption.READ)) {
		
		//try(InputStream unifont = Files.newInputStream(Path.of("unifont-15.1.05.hex"),StandardOpenOption.READ)) {
			font.loadHex(unifont);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		
		Minesweeper.LOGGER.info("Loaded.");
		
		mainArea = new Area();
		mainArea.chooseRandomSeed();
		
		player.setPosition(new Vector2d((mainArea.getWidth()/2)*16, (mainArea.getHeight()/2)*16));
		mainArea.addEntity(player);
		
		mainArea.setGenerator(new BaseAreaGenerator());
		mainArea.generate(); //FIXME: This is temporary! In reality we will generate a patch at a time!
		Minesweeper.LOGGER.info("Doing initial reveal...");
		mainArea.revealAndChain(mainArea.getWidth() / 2, mainArea.getHeight() / 2, MinesweeperClient::markDirty);
		Minesweeper.LOGGER.info("Map Complete.");
		
		gameWindow.onLeftMouseUp().register((x, y) -> {
			int tileX = ((x + scrollx) / scale) / 16;
			int tileY = ((y + scrolly) / scale) / 16;
			//Minesweeper.LOGGER.info("Handling click at tile "+tileX+", "+tileY+"...");
			
			Optional<TileEntity> ote = mainArea.getTileEntity(tileX, tileY);
			
			if (ote.isPresent()) {
				TileEntity te = ote.get();
				
				if (te.isBomb()) {
					mainArea.revealSimple(tileX, tileY);
					markDirty(tileX, tileY);
					
					mainArea.addPoints(-mainArea.points());
					player.setPosition(new Vector2d((mainArea.getWidth()/2)*16, (mainArea.getHeight()/2)*16));
					
					//TODO: Animate this
				}
				
			} else {
				int revealed = mainArea.revealAndChain(tileX, tileY, MinesweeperClient::markDirty);
				mainArea.addPoints(revealed);
			}
			
		});
		
		gameWindow.onRightMouseUp().register((x, y) -> {
			int tileX = ((x + scrollx) / scale) / 16;
			int tileY = ((y + scrolly) / scale) / 16;
			
			if (mainArea.getForegroundTile(tileX, tileY).isPresent()) {
				mainArea.flag(tileX, tileY, !mainArea.isFlagged(tileX, tileY), MinesweeperClient::markDirty);
			} else {
				
				int adjacent = mainArea.adjacentMineCount(tileX, tileY);
				
				switch(adjacent) {
					case 1 -> {
						if (inferenceLevel < 1) break; // Flagging an *adjacent* square by right-clicking a 1 requires inference-level 1
						
						ArrayList<Vector2i> candidates = new ArrayList<>();
						
						for(int yi=-1; yi<=1; yi++) {
							for(int xi=-1; xi<=1; xi++) {
								if (xi==0 && yi==0) continue;
								if (mainArea.getForegroundTile(tileX + xi, tileY + yi).isPresent()) {
									candidates.add(new Vector2i(tileX + xi, tileY + yi));
								}
							}
						}
						
						if (candidates.size() == 1) {
							Vector2i vec = candidates.get(0);
							mainArea.flag(vec.x(), vec.y(), true, MinesweeperClient::markDirty);
						}
					}
				}
			}
			
			
		});
		
		gameWindow.onKeyDown().register(controls::keyDown);
		gameWindow.onKeyUp().register(controls::keyUp);
		
		controls.get("left").bindKey(GLFW.GLFW_KEY_A);
		controls.get("right").bindKey(GLFW.GLFW_KEY_D);
		controls.get("up").bindKey(GLFW.GLFW_KEY_W);
		controls.get("down").bindKey(GLFW.GLFW_KEY_S);
		
		Timing timing = new Timing()
			.setFrameCallback((t) -> { frame(t, gameWindow); })
			.setTickCallback(MinesweeperClient::tick)
			.setNonFrameCallback(gameWindow::poll);
		
		for(EntrypointContainer<ModInitializer> container : FabricLoader.getInstance().getEntrypointContainers("main", ModInitializer.class)) {
			container.getEntrypoint().onInitialize();
		}
		
		while(!gameWindow.shouldClose()) {
			timing.runCycle();
		}
		
		gameWindow.setVisible(false);
		gameWindow.delete();
	}
	
	public static void markDirty(int x, int y) {
		Vector2i pos = new Vector2i(x / 32, y / 32);
		if (!dirtyTiles.contains(pos)) dirtyTiles.addLast(pos);
	}
	
	public void bakeAll(ImageSupplier images, Area area) {
		int tilesWide = area.getWidth() / BAKE_SIZE;
		int tilesHigh = area.getHeight() / BAKE_SIZE;
		
		for(int y=0; y<tilesHigh; y++) {
			for(int x=0; x<tilesWide; x++) {
				Texture t = bake(images, area, x*BAKE_SIZE, y*BAKE_SIZE, BAKE_SIZE, BAKE_SIZE);
				
				bakedTiles.put(new Vector2i(x, y), t);
			}
		}
	}
	
	public void rebake(ImageSupplier images, Area area, int x, int y) {
		if (x<0 || y<0 || x>=area.getWidth() || y>=area.getHeight()) return;
		
		int tx = x / BAKE_SIZE;
		int ty = y / BAKE_SIZE;
		Vector2i pos = new Vector2i(tx, ty);
		
		Texture t = bakedTiles.remove(pos);
		if (t != null) t.destroy();
		
		//Minesweeper.LOGGER.info("Putting offset: "+(tx*32)+", "+(ty*32)+" pos: "+pos);
		
		bakedTiles.put(pos, bake(images, area, tx * BAKE_SIZE, ty * BAKE_SIZE, BAKE_SIZE, BAKE_SIZE));
	}
	
	public static void rebakeTile(ImageSupplier images, Area area, Vector2i tilePos) {
		Texture t = bakedTiles.remove(tilePos);
		if (t != null) t.destroy();
		
		bakedTiles.put(tilePos, bake(images, area, tilePos.x() * BAKE_SIZE, tilePos.y() * BAKE_SIZE, BAKE_SIZE, BAKE_SIZE));
	}
	
	public static Texture bake(ImageSupplier images, Area area, int xofs, int yofs, int width, int height) {
		//Minesweeper.LOGGER.info("Baking at offset "+xofs+", "+yofs);
		SrgbImageData data = new SrgbImageData(width * 16, height * 16);
		ImagePainter painter = new ImagePainter(data, BlendMode.NORMAL);
		
		//ImageData emptyBg = images.getImage(Identifier.of("ms:tiles.png:13"));
		ImageData coin = images.getImage(Identifier.of("ms:textures/tiles.png:11")); //Temp
		ImageData bomb = images.getImage(Identifier.of("ms:textures/tiles.png:12")); //Temp
		ImageData num0 = images.getImage(Identifier.of("ms:textures/tiles.png:10"));
		ImageData num1 = images.getImage(Identifier.of("ms:textures/tiles.png:2"));
		ImageData num2 = images.getImage(Identifier.of("ms:textures/tiles.png:3"));
		ImageData num3 = images.getImage(Identifier.of("ms:textures/tiles.png:4"));
		ImageData num4 = images.getImage(Identifier.of("ms:textures/tiles.png:5"));
		ImageData num5 = images.getImage(Identifier.of("ms:textures/tiles.png:6"));
		ImageData num6 = images.getImage(Identifier.of("ms:textures/tiles.png:7"));
		ImageData num7 = images.getImage(Identifier.of("ms:textures/tiles.png:8"));
		ImageData num8 = images.getImage(Identifier.of("ms:textures/tiles.png:9"));
		
		ImageData fg = images.getImage(Identifier.of("ms:textures/tiles.png:0"));
		
		ImageData flag = images.getImage(Identifier.of("ms:textures/tiles.png:1"));
		
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
				
				Identifier id = Identifier.of(area.getBackgroundTile(x, y).data().getPrimitive("texture").asString().orElse("ms:textures/missing.png"));
				ImageData tileTexture = images.getImage(id);
				painter.drawImage(tileTexture, x * 16, y * 16);
				
				Optional<TileEntity> te = area.getTileEntity(x + xofs, y + yofs);
				if (te.isPresent()) {
					if (te.get() instanceof FlagTileEntity flagTE) {
						painter.drawImage(flag, x * 16, y * 16); //TODO: Honor the json in the TE
					} else {
						//For now assume it's a score coin
						if (te.get().isBomb()) {
							painter.drawImage(bomb, x * 16, y * 16);
						} else {
							painter.drawImage(coin, x * 16, y * 16);
						}
					}
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
		//Before we process this frame, let's rebake tiles that need it
		Vector2i dirty = dirtyTiles.pollFirst();
		if (dirty != null) {
			//System.out.println("Baking "+dirty);
			rebakeTile(images, mainArea, dirty);
		}
		
		
		gameWindow.clear(0, 0, 0, 0);
		
		WindowPainter g = gameWindow.startDrawing();
		
		int tilesWide = mainArea.getWidth() / BAKE_SIZE;
		int tilesHigh = mainArea.getHeight() / BAKE_SIZE;
		
		ImageData image = images.getImage(Identifier.of("ms:textures/player.png")); //TODO: Texture supplier to sit alongside the image supplier
		
		double t = timing.getPartialTick();
		if (t>1D) t=1D; if (t<0D) t=0D;
		Vector2d vec = player.interpolatedPosition(t);
		
		/*
		 * Okay folks, the goal is to lock the camera (scrollx, scrolly) onto the center of the character.
		 * This means ADDING the character's center, and then SUBTRACTING half the window dimensions
		 */
		int halfWidth = gameWindow.getWidth() / 2;
		int halfHeight = gameWindow.getHeight() / 2;
		
		int playerX = (int) Math.round(vec.x());
		int playerY = (int) Math.round(vec.y());
		scrollx = (playerX * scale) - halfWidth; //Center of character is at the center of its sprite
		scrolly = ((playerY - (image.getHeight() / 2)) * scale) - halfHeight; //Center of character is halfway up the sprite from the entity location.
		
		int playerTileX = playerX / 16 / BAKE_SIZE;
		int playerTileY = playerY / 16 / BAKE_SIZE;
		
		for (int y=playerTileY-2; y<=playerTileY+2; y++) {
			for(int x=playerTileX-2; x<=playerTileX+2; x++) {
				
				Texture tile = bakedTiles.get(new Vector2i(x, y));
				if (tile != null) {
					g.drawImage(tile, (x * 16 * BAKE_SIZE * scale) - scrollx, (y * 16 * BAKE_SIZE * scale) - scrolly, tile.getWidth() * scale, tile.getHeight() * scale, 0, 0, tile.getWidth(), tile.getHeight(), 1.0f);
				} else {
					if (x<0 || y<0 || x>=tilesWide || y>=tilesHigh) {
						continue;
					} else {
						markDirty(x*BAKE_SIZE, y*BAKE_SIZE);
					}
				}
			}
		}
		
		for(FreeEntity entity : mainArea.entities()) {
			//TODO: Grab texture for this entity
			Vector2d screenPosition = entity.interpolatedPosition(t).multiply(scale).add(new Vector2d(-scrollx, -scrolly));
			int halfImageWidth = (image.getWidth() / 2) * scale;
			
			g.drawImage(image, (int) Math.round(screenPosition.x()) - halfImageWidth, (int) Math.round(screenPosition.y()) - (image.getHeight() * scale), image.getWidth() * scale, image.getHeight() * scale, 0, 0, image.getWidth(), image.getHeight(), 1.0f);
		}
		
		if (!mainArea.isClear()) {
			font.drawShadowString(g, 12, 12, "Bombs: " + mainArea.mineCount() + " Flags: "+mainArea.flagCount()+" Points: "+mainArea.points(), RGBColor.fromGamma(1, 1, 1, 0.5f), 3);
		} else {
			font.drawShadowString(g, 12, 12, "Clear!", RGBColor.fromGamma(1, 1, 1, 0.5f), 3);
		}
		
		//font.drawShadowString(g, 12, 20, "Tiles: "+bakedTiles.size(), RGBColor.fromGamma(1, 1, 1, 0.5f), 2);
		
		gameWindow.swapBuffers();
	}
	
	public static final double CONTROL_VELOCITY = 2.0;
	public static final double CONTROL_SPEED_CAP = 6.0;
	public static final double TURN_DRAG = 4.0f;
	public static final double IDLE_DRAG = 2.0f;
	public static final double VELOCITY_DEADZONE = 0.5;
	
	public static void tick(Timing t) {
		for(FreeEntity entity : mainArea.entities()) {
			entity.catchUp();
			
			Vector2d proposedLocation = entity.nextPos().add(entity.velocity());
			
			//Check/Move in X direction first
			if (checkCollision((int) proposedLocation.x(), (int) entity.nextPos().y(), entity.size(), mainArea)) {
				int edgeDisplacement = (int) ((entity.size() / 2) * Math.signum(entity.velocity().x()));
				int edge = (int) entity.nextPos().x() + edgeDisplacement;
				
				if (entity.velocity().x() > 0) {
					int snapped = (edge / 16) * 16 + 15;
					int newCenter = snapped - edgeDisplacement;
					entity.moveTo(new Vector2d(newCenter, entity.nextPos().y()));
				} else if (entity.velocity().x() < 0) {
					int snapped = (edge / 16) * 16;
					if (snapped < 0) snapped = 0;
					int newCenter = snapped - edgeDisplacement;
					entity.moveTo(new Vector2d(newCenter, entity.nextPos().y()));
				}
			} else {
				entity.moveTo(proposedLocation.x(), entity.nextPos().y());
			}
			
			//Check/Move in Y direction
			if (checkCollision((int) entity.nextPos().x(), (int) proposedLocation.y(), entity.size(), mainArea)) {
				int edgeDisplacement = (entity.velocity().y() < 0) ? -entity.size() : 0;
				int edge = (int) entity.nextPos().y() + edgeDisplacement;
				
				if (entity.velocity().y() > 0) {
					int snapped = (edge / 16) * 16 + 15;
					int newY = snapped - edgeDisplacement;
					entity.moveTo(new Vector2d(entity.nextPos().x(), newY));
				} else if (entity.velocity().y() < 0) {
					int snapped = (edge / 16) * 16;
					if (snapped < 0) snapped = 0;
					int newY = snapped - edgeDisplacement;
					entity.moveTo(new Vector2d(entity.nextPos().x(), newY));
				}
			} else {
				entity.moveTo(entity.nextPos().x(), proposedLocation.y());
			}
		}
		
		//player.catchUp();
		boolean rightPressed = controls.get("right").isPressed();
		boolean leftPressed = controls.get("left").isPressed();
		boolean upPressed = controls.get("up").isPressed();
		boolean downPressed = controls.get("down").isPressed();
		
		if (rightPressed && !leftPressed) {
			Vector2d velocity = player.velocity();
			if (velocity.x() < 0) {
				//Apply extra drag to turn us around quick
				double vx = softReduce(velocity.x(), TURN_DRAG);
				player.setVelocity(new Vector2d(vx, velocity.y()));
			}
			
			velocity = player.velocity();
			double vx = addWithSoftCap(velocity.x(), CONTROL_VELOCITY, CONTROL_SPEED_CAP);
			player.setVelocity(new Vector2d(vx, velocity.y()));
		}
		
		if (leftPressed && !rightPressed) {
			Vector2d velocity = player.velocity();
			if (velocity.x() > 0) {
				//Apply extra drag to turn us around quick
				double vx = softReduce(velocity.x(), TURN_DRAG);
				player.setVelocity(new Vector2d(vx, velocity.y()));
			}
			
			velocity = player.velocity();
			double vx = addWithSoftCap(velocity.x(), -CONTROL_VELOCITY, CONTROL_SPEED_CAP);
			player.setVelocity(new Vector2d(vx, velocity.y()));
			//player.setVelocity(player.velocity().add(new Vector2d(-CONTROL_VELOCITY, 0)));
		}
		
		if (upPressed && !downPressed) {
			Vector2d velocity = player.velocity();
			if (velocity.y() > 0) {
				//Apply extra drag to turn us around quick
				double vy = softReduce(velocity.y(), TURN_DRAG);
				player.setVelocity(new Vector2d(velocity.x(), vy));
			}
			
			velocity = player.velocity();
			double vy = addWithSoftCap(velocity.y(), -CONTROL_VELOCITY, CONTROL_SPEED_CAP);
			player.setVelocity(new Vector2d(velocity.x(), vy));
			//player.setVelocity(player.velocity().add(new Vector2d(0, -CONTROL_VELOCITY)));
		}
		
		if (downPressed && !upPressed) {
			Vector2d velocity = player.velocity();
			if (velocity.y() < 0) {
				//Apply extra drag to turn us around quick
				double vy = softReduce(velocity.y(), TURN_DRAG);
				player.setVelocity(new Vector2d(velocity.x(), vy));
			}
			
			velocity = player.velocity();
			double vy = addWithSoftCap(velocity.y(), CONTROL_VELOCITY, CONTROL_SPEED_CAP);
			player.setVelocity(new Vector2d(velocity.x(), vy));
			//player.setVelocity(player.velocity().add(new Vector2d(0, CONTROL_VELOCITY)));
		}
		
		if (!(upPressed || downPressed)) {
			//Apply drag in the y direction
			Vector2d oldVelocity = player.velocity();
			
			double vy = softReduce(oldVelocity.y(), IDLE_DRAG);
			if (Math.abs(vy) < VELOCITY_DEADZONE) vy = 0;
			player.setVelocity(new Vector2d(oldVelocity.x(), vy));
		}
		
		if (!(leftPressed || rightPressed)) {
			//Apply drag in the x direction
			Vector2d oldVelocity = player.velocity();
			
			double vx = softReduce(oldVelocity.x(), IDLE_DRAG);
			if (Math.abs(vx) < VELOCITY_DEADZONE) vx = 0;
			player.setVelocity(new Vector2d(vx, oldVelocity.y()));
		}
		
	}
	
	private static double softReduce(double input, double reduceBy) {
		if (input > 0) {
			double result = input - reduceBy;
			return (result < 0) ? 0 : result;
		} else if (input < 0) {
			double result = input + reduceBy;
			return (result > 0) ? 0 : result;
		}
		
		return 0;
	}
	
	private static double addWithSoftCap(double input, double toAdd, double softCap) {
		if (Math.abs(input) > softCap) {
			//We behave specially in this case, only applying the delta if it is "pushing against" the direction we're
			//already capped in
			if (Math.signum(input) == Math.signum(toAdd)) return input;
			double proposed = input + toAdd;
			
			//Now we make sure we're not going all the way from cap to cap
			if ((Math.abs(proposed) > softCap) && (Math.signum(proposed) == Math.signum(toAdd))) {
				//We went from past-cap to past-cap. Return the new cap.
				return softCap * Math.signum(toAdd);
			} else {
				//We've come back in-bounds
				return proposed;
			}
		} else {
			//We are not capped. Just make sure we don't cap when we add.
			double proposed = input + toAdd;
			if (Math.abs(proposed) > softCap) {
				//We did cap, so return the cap in the direction we're traveling
				return softCap * Math.signum(toAdd);
			} else {
				return proposed;
			}
		}
	}
	
	private static boolean checkCollision(int x, int y, int size, Area area) {
		int x1 = x - (size/2);
		int y1 = y - (size);
		
		int tx = x1 / 16;
		int ty = y1 / 16;
		int tx2 = (x1 + size) / 16;
		int ty2 = (y1 + size) / 16;
		
		if (x1 < 0 || y1 < 0) return true;
		
		for(int yi=ty; yi<=ty2; yi++) {
			for(int xi=tx; xi<=tx2; xi++) {
				if (xi < 0 || yi < 0 || xi >= area.getWidth() || yi >= area.getHeight()) return true;
				if (area.getForegroundTile(xi, yi).isPresent()) return true;
			}
		}
		
		return false;
	}

}
