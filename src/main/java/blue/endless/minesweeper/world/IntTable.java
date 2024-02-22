package blue.endless.minesweeper.world;

import java.util.Arrays;

public class IntTable {
	protected int width;
	protected int height;
	protected int[] data;
	
	public IntTable(int width, int height) {
		this.width = width;
		this.height = height;
		data = new int[width * height];
	}
	
	public int width() { return width; }
	public int height() { return height; }
	
	public int getTile(int x, int y) {
		if (x < 0 || y < 0 || x >= width || y >= height) return -1;
		return data[width * y + x];
	}
	
	public void set(int x, int y, int value) {
		if (x < 0 || y < 0 || x >= width || y >= height) return;
		data[width*y + x] = value;
	}
	
	public void clear(int value) {
		Arrays.fill(data, value);
	}
}
