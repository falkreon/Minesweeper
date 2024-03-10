package blue.endless.minesweeper;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

public class Resources {
	private static List<Node> nodes = new ArrayList<>();
	
	public static void init() {
		List<Node> unresolved = new ArrayList<>();
		ArrayDeque<Node> resolved = new ArrayDeque<>();
		Set<String> resolvedIds = new HashSet<>();
		
		for(ModContainer container : FabricLoader.getInstance().getAllMods()) {
			Node node = new Node(container);
			if (!resolve(node, resolved, resolvedIds)) unresolved.add(node);
		}
		
		for(int i=0; i<1000; i++) {
			if (unresolved.isEmpty()) break;
			
			if (!resolveOne(unresolved, resolved, resolvedIds)) break;
			unresolved.removeIf(resolved::contains);
		}
		
		//
		if (!unresolved.isEmpty()) {
			Minesweeper.LOGGER.info("Unresolved Mods: "+unresolved);
		}
		Minesweeper.LOGGER.info("Resolved Mods: "+resolved);
		
		nodes.addAll(resolved);
	}
	
	private static boolean resolveOne(List<Node> unresolved, ArrayDeque<Node> resolved, Set<String> resolvedIds) {
		for(Node n : unresolved) {
			if (resolve(n, resolved, resolvedIds)) {
				return true;
			}
		}
		
		return false;
	}
	
	private static boolean resolve(Node node, ArrayDeque<Node> resolved, Set<String> resolvedIds) {
		if (node.dependencies().isEmpty() || resolvedIds.containsAll(node.dependencies())) {
			resolved.addFirst(node);
			resolvedIds.add(node.id());
			return true;
		} else {
			return false;
		}
	}
	
	public static List<Resource> get(Identifier id) {
		List<Resource> result = new ArrayList<>();
		for(Node node : nodes) {
			for(Path p : node.rootPaths()) {
				Path filePath = p.resolve("data").resolve(id.namespace()).resolve(id.path());
				if (Files.exists(filePath)) {
					result.add(new Resource(new Identifier(node.id(), "", ""), id, filePath));
				}
			}
		}
		
		return result;
	}
	
	public static List<Resource> find(Identifier basePath, boolean recursive) {
		List<Resource> result = new ArrayList<>();
		
		for(Node node : nodes) {
			for(Path p : node.rootPaths()) {
				
			}
		}
		
		return result;
	}
	
	private static void get(Path p, Identifier basePath, List<Resource> results) {
		
	}
	
	private static void find(Path p, Identifier basePath, List<Resource> results, boolean recursive) {
		
	}
	
	/**
	 * Performs a depth-first search of all directories visible from base, including base, and returns a list of Paths
	 * to all files encountered along the way.
	 * @param base The folder to find all files in
	 * @param recursive if false, only the base directory will be searched. If true, all subdirectories will be searched.
	 * @return A list of all files in the directory
	 */
	public static List<Path> listFiles(Path base, boolean recursive) {
		ArrayList<Path> result = new ArrayList<>();
		
		if (Files.isDirectory(base)) {
			ArrayDeque<Path> stack = new ArrayDeque<>();
			stack.push(base);
			
			while(!stack.isEmpty()) {
				Path curDir = stack.pop();
				try (DirectoryStream<Path> ls = Files.newDirectoryStream(curDir)) {
					for(Path entry : ls) {
						if (Files.isDirectory(entry)) {
							if (recursive) stack.push(entry);
						} else if (Files.isRegularFile(entry)) {
							result.add(entry);
						}
					}
				} catch (IOException e) {
					//We can't list the directory - either it doesn't exist or we don't have permission.
				}
			}
		} else {
			result.add(base);
		}
		return result;
	}
	
	/**
	 * Memory-conserving variant of {@link #listFiles(Path, boolean)} - forwards paths found to a consumer for further
	 * processing.
	 * @param base the base Path to start searching from
	 * @param pathConsumer the Consumer that will receive paths that are found
	 * @param recursive 
	 */
	public static void listFiles(Path base, Consumer<Path> pathConsumer, boolean recursive) {
		
		if (Files.isDirectory(base)) {
			ArrayDeque<Path> stack = new ArrayDeque<>();
			stack.push(base);
			
			while(!stack.isEmpty()) {
				Path curDir = stack.pop();
				try (DirectoryStream<Path> ls = Files.newDirectoryStream(curDir)) {
					for(Path entry : ls) {
						if (Files.isDirectory(entry)) {
							if (recursive) stack.push(entry);
						} else if (Files.isRegularFile(entry)) {
							pathConsumer.accept(entry);
						}
					}
				} catch (IOException e) {
					//We can't list the directory - either it doesn't exist or we don't have permission.
				}
			}
		} else {
			pathConsumer.accept(base);
		}
	}
	
	
	private static class Node {
		private final ModContainer container;
		private ArrayList<Path> rootPaths = new ArrayList<>();
		private final Set<String> dependencies;
		
		public Node(ModContainer container) {
			this.container = container;
			dependencies = Set.copyOf(container.getMetadata().getDependencies().stream().map(it -> it.getModId()).toList());
			rootPaths.addAll(container.getRootPaths());
		}
		
		public Set<String> dependencies() {
			return dependencies;
		}
		
		public String id() {
			return container.getMetadata().getId();
		}
		
		public List<Path> rootPaths() {
			return rootPaths;
		}
		
		@Override
		public String toString() {
			return id();
		}
	}
	
	public static record Resource(Identifier dataSource, Identifier id, Path path) {
	}
}
