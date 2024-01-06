package blue.endless.minesweeper.client;

public class MinesweeperClient {
	public void init() {
		Thread.currentThread().setName("Render thread");
		
		Window gameWindow = new Window("Minesweeper");
		gameWindow.setVisible(true);
		
		while(!gameWindow.shouldClose()) {
			//Minesweeper.LOGGER.info(".");
			gameWindow.clear(0.5f, (float) Math.random() * 0.5f + 0.5f, 1.0f, 0.0f);
			
			gameWindow.swapBuffers();
			try {
				Thread.sleep(10L);
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		gameWindow.setVisible(false);
	}
}
