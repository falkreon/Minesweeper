package blue.endless.minesweeper.world;

public class FreeEntity implements Entity {
	private int lastX = 0;
	private int lastY = 0;
	private int nextX = 0;
	private int nextY = 0;
	private int vx = 0;
	private int vy = 0;
	
	public void setPosition(int x, int y) {
		this.nextX = x;
		this.nextY = y;
		this.lastX = x;
		this.lastY = y;
	}
	
	public void moveTo(int x, int y) {
		this.nextX = x;
		this.nextY = y;
	}
	
	public void moveTo(Vector2i pos) {
		this.nextX = pos.x();
		this.nextY = pos.y();
	}
	
	/** Sets all the "last" values to the "cur" values so there is no further interpolation until position changes. */
	public void catchUp() {
		lastX = nextX;
		lastY = nextY;
	}
	
	public Vector2i lastPosition() {
		return new Vector2i(lastX, lastY);
	}
	
	public Vector2i position() {
		return new Vector2i(nextX, nextY);
	}
	
	public Vector2i position(double t) {
		if (t<0) t=0; if (t>1) t=1;
		
		double xt = (nextX * t) + (lastX * (1-t));
		double yt = (nextY * t) + (lastY * (1-t));
		
		return new Vector2i((int) Math.round(xt), (int) Math.round(yt));
	}
	
	public Vector2i velocity() {
		return new Vector2i(vx, vy);
	}
	
	public void setVelocity(int vx, int vy) {
		this.vx = vx;
		this.vy = vy;
	}
	
	public void setVelocity(Vector2i vec) {
		this.vx = vec.x();
		this.vy = vec.y();
	}
}
