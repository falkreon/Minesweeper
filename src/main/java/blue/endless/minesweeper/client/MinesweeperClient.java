package blue.endless.minesweeper.client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.playsawdust.glow.Timing;
import com.playsawdust.glow.gl.Texture;
import com.playsawdust.glow.gl.Window;
import com.playsawdust.glow.image.ImageData;
import com.playsawdust.glow.image.SrgbImageData;
import com.playsawdust.glow.image.io.PngImageIO;
import com.playsawdust.glow.io.DataSlice;
import com.playsawdust.glow.render.Painter;

import blue.endless.minesweeper.world.Area;
import blue.endless.minesweeper.world.BaseAreaGenerator;

public class MinesweeperClient {
	
	private static ImageData tileset = null;
	private static Texture texture = null;
	private static Area mainArea = null;
	
	public void init() {
		Thread.currentThread().setName("Render thread");
		
		Window gameWindow = new Window("Minesweeper");
		gameWindow.setVisible(true);
		
		mainArea = new Area();
		mainArea.setGenerator(new BaseAreaGenerator());
		mainArea.generate(); //FIXME: This is temporary! In reality we will generate a patch at a time!
		
		try {
			tileset = PngImageIO.load(
				DataSlice.of(Files.readAllBytes(Path.of("tiles.png")))
				);
		} catch (IOException e) {
			e.printStackTrace();
			tileset = new SrgbImageData(16, 16);
		}
		
		texture = new Texture(tileset);
		
		Timing timing = new Timing()
			.setFrameCallback((t) -> { frame(t, gameWindow); })
			.setTickCallback(MinesweeperClient::tick)
			.setNonFrameCallback(gameWindow::poll);
		
		while(!gameWindow.shouldClose()) {
			timing.runCycle();
		}
		
		texture.destroy();
		
		gameWindow.setVisible(false);
		gameWindow.delete();
	}
	
	public static void frame(Timing timing, Window gameWindow) {
		gameWindow.clear(0.5f, (float) Math.random() * 0.5f + 0.5f, 1.0f, 0.0f);
		
		Painter g = gameWindow.startDrawing();
		
		g.drawImage(tileset, 100, 100);
		g.drawImage(texture, 200, 100);
		
		gameWindow.swapBuffers();
	}
	
	public static void tick(Timing t) {
	}

}
