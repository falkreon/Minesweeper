package blue.endless.minesweeper.world;

public record Vector2i(int x, int y) {
	
	public double length() {
		return Math.sqrt(x*x + y*y);
	}
	
	public Vector2i multiply(int value) {
		return new Vector2i(x * value, y * value);
	}
	
	public Vector2i divide(int value) {
		return new Vector2i(x / value, y / value);
	}
	
	public Vector2i add(int value) {
		return new Vector2i(x + value, y + value);
	}
	
	public Vector2i add(int x, int y) {
		return new Vector2i(this.x + x, this.y + y);
	}
	
	public Vector2i add(Vector2i other) {
		return new Vector2i(x + other.x, y + other.y);
	}
	
	public Vector2i subtract(int value) {
		return new Vector2i(x - value, y - value);
	}
	
	public Vector2i subtract(Vector2i other) {
		return new Vector2i(x - other.x, y - other.y);
	}
	
	public double distanceSquared(Vector2i other) {
		int dx = x - other.x;
		int dy = y - other.y;
		return dx * dx + dy * dy;
	}
	
	public double distance(Vector2i other) {
		return subtract(other).length();
	}
}
