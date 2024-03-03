package com.playsawdust.glow;

import java.util.function.Consumer;

public class Timing {
	private Consumer<Timing> frame;
	private Consumer<Timing> tick;
	private Runnable nonFrame;
	
	private long nanosPerFrame = cyclesPerSecond(60);
	private long nanosPerTick = cyclesPerSecond(20);
	
	private long partialTicks = 0L;
	private long partialFrames = 0L;
	private long lastFrame = System.nanoTime();
	private long lastCycle = System.nanoTime();
	
	public Timing() {}
	
	public void runCycle() {
		final long now = System.nanoTime();
		final long expectedNextFrame = lastFrame + nanosPerFrame;
		
		final long delta = now - lastCycle;
		
		partialFrames += delta;
		if (partialFrames >= nanosPerFrame) {
			partialFrames %= nanosPerFrame;
			frame.accept(this);
			lastFrame = now;
		} else {
			nonFrame.run();
		}
		
		partialTicks += delta;
		if (partialTicks >= nanosPerTick) {
			partialTicks %= nanosPerTick;
			tick.accept(this);
		}
		
		final long expectedDelta = expectedNextFrame - now;
		
		if (expectedDelta > 0) {
			try {
				Thread.sleep(expectedDelta / 1_000_000L);
			} catch (InterruptedException ex) {}
		}
		
		lastCycle = now;
	}
	
	public Timing setFrameCallback(Consumer<Timing> callback) {
		this.frame = callback;
		return this;
	}
	
	public Timing setTickCallback(Consumer<Timing> callback) {
		this.tick = callback;
		return this;
	}
	
	public Timing setNonFrameCallback(Runnable callback) {
		this.nonFrame = callback;
		return this;
	}
	
	public double getPartialTick() {
		return (double) partialTicks / (double) nanosPerTick;
	}
	
	public double getPartialFrame() {
		return (double) partialFrames / (double) nanosPerFrame;
	}
	
	/**
	 * Returns the specified cycles per second in nanos-per-cycle so they can be used to measure timing
	 * @param hz the desired number of cycles per second
	 * @return The same frequency expressed in nanoseconds per cycle
	 */
	public static final long cyclesPerSecond(double hz) {
		double secondsPerCycle = 1.0 / hz;
		double nanosPerCycle = secondsPerCycle * 1_000_000_000.0;
		
		return (long) nanosPerCycle;
	}

}
