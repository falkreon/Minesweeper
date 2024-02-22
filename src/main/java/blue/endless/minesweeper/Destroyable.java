package blue.endless.minesweeper;

/**
 * Classes which implement Destroyable represent a resource whose scope is not defined by the JVM garbage collector.
 * This could be a network resource, something on the GPU, it doesn't say what kind of resource it is, only that it can
 * be immediately destroyed, and that interacting with it after being destroyed is invalid.
 */
public interface Destroyable {
	public void destroy();
}
