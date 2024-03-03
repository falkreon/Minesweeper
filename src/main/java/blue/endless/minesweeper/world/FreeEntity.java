package blue.endless.minesweeper.world;

import com.playsawdust.glow.vecmath.Vector2d;

public class FreeEntity implements Entity {
	private Vector2d lastPos = new Vector2d(0,0);
	private Vector2d nextPos = new Vector2d(0,0);
	private Vector2d velocity = new Vector2d(0,0);
	
	/**
	 * There is a rectangle, from (pos.x-(size/2), pos.y-size) to (pos.x+(size/2), pos.y) that governs collision.
	 * "Long" entities are not possible; entities need to be able to turn freely without colliding with anything.
	 * "height" for entities is cosmetic, and determined by the sprite. FreeEntities are always located at the bottom
	 * center of their sprite.
	 */
	private int size = 12;
	
	public void setPosition(Vector2d pos) {
		lastPos = pos;
		nextPos = pos;
	}
	
	public void moveTo(double x, double y) {
		moveTo(new Vector2d(x, y));
	}
	
	public void moveTo(Vector2d pos) {
		this.nextPos = pos;
	}
	
	/** Sets all the "last" values to the "cur" values so there is no further interpolation until position changes. */
	public void catchUp() {
		lastPos = nextPos;
	}
	
	public Vector2d lastPos() {
		return lastPos;
	}
	
	public Vector2d nextPos() {
		return nextPos;
	}
	
	public Vector2d interpolatedPosition(double t) {
		if (t<0) t=0; if (t>1) t=1;
		
		return lastPos.multiply(1.0-t).add(nextPos.multiply(t));
	}
	
	public Vector2d velocity() {
		return velocity;
	}
	
	public void setVelocity(Vector2d velocity) {
		this.velocity = velocity;
	}
	
	public int size() {
		return size;
	}
	
	public void setSize(int size) {
		this.size = size;
	}
}
