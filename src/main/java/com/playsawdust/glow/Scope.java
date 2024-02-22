package com.playsawdust.glow;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import blue.endless.minesweeper.Destroyable;

/**
 * Takes a number of objects that require disposal and 
 */
public class Scope implements AutoCloseable {
	private List<WeakReference<Destroyable>> items = new ArrayList<>();
	private List<WeakReference<AutoCloseable>> closeables = new ArrayList<>();
	
	private Scope() {}
	
	/**
	 * Adds a Destroyable object to this Scope to be automatically destroyed when the scope closes.
	 * @param value The object to add to this scope for automatic destruction
	 * @return the passed-in value
	 */
	public <T extends Destroyable> T add(T value) {
		items.add(new WeakReference<>(value));
		return value;
	}
	
	/**
	 * Adds an AutoCloseable object to this Scope to be automatically closed when the scope closes. Note that any errors
	 * caused by closing will be eaten.
	 * @param value The object to add to this scope for automatic closing when the scope closes.
	 * @return the passed-in value
	 */
	public <T extends AutoCloseable> T add(T value) {
		closeables.add(new WeakReference<>(value));
		return value;
	}
	
	public void close() {
		items.stream()
			.map(WeakReference::get)
			.filter(it -> it != null)
			.forEach(Destroyable::destroy);
		
		closeables.stream()
			.map(WeakReference::get)
			.filter(it -> it != null)
			.forEach(it -> {
				try {
					it.close();
				} catch (Exception e) {
					//Eat the error
				}
			});
	}
	
	public static void run(Consumer<Scope> r) {
		Scope scope = new Scope();
		r.accept(scope);
		scope.close();
	}
	
	public static Scope create() {
		return new Scope();
	}
	
	public static Scope with(Destroyable ... destroyables) {
		Scope scope = new Scope();
		for(Destroyable d : destroyables) {
			scope.add(d);
		}
		return scope;
	}
}
