package blue.endless.minesweeper.client;

import org.joml.Matrix4f;
import com.playsawdust.glow.render.Painter;
import com.playsawdust.glow.image.color.RGBColor;

public class MinesweeperClient {
	public void init() {
		Thread.currentThread().setName("Render thread");
		
		Window gameWindow = new Window("Minesweeper");
		gameWindow.setVisible(true);
		
		//Matrix4f transform = new Matrix4f().identity().ortho2D(0, gameWindow.getWidth(), gameWindow.getHeight(), 0);
		RGBColor triangleColor = new RGBColor(0x88_FF00FF);
		
		
		
		while(!gameWindow.shouldClose()) {
			//Minesweeper.LOGGER.info(".");
			gameWindow.clear(0.5f, (float) Math.random() * 0.5f + 0.5f, 1.0f, 0.0f);
			
			Painter g = gameWindow.startDrawing();
			
			
			//g.color(triangleColor);
			//g.getShader().set("Matrix", transform);
			//g.rect(10, 10, 100, 100);
			//g.rect(30, 20, 150, 100);
			g.fillRect(0, 0, 10, 10, triangleColor);
			g.fillRect(5, 5, 10, 10, triangleColor);
			
			
			
			gameWindow.swapBuffers();
			try {
				Thread.sleep(10L);
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		gameWindow.setVisible(false);
		gameWindow.delete();
	}
}
