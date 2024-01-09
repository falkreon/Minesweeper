package blue.endless.minesweeper.client;

public record ImageData(int width, int height, int[] data) {
	public ImageData(int width, int height) {
		this(width, height, new int[width * height]);
	}
	
	public int get(int x, int y) {
		if (x < 0 || y < 0 || x >= width || y >= height) return 0;
		return data[y * width + x];
	}
	
	public void set(int x, int y, int color) {
		if (x < 0 || y < 0 || x >= width || y >= height) return;
		data[y * width + x] = color;
	}
}
