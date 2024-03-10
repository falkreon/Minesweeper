package blue.endless.minesweeper;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class FileIterator implements Iterator<Path> {
	private Deque<Path> queue = new ArrayDeque<Path>();
	private Deque<Path> outputBuffer = new ArrayDeque<Path>();
	private Path next = null;
	private final boolean recursive;
	
	public FileIterator(Path basePath, boolean recursive) {
		this.recursive = recursive;
		queue.add(basePath);
		lookahead();
	}
	
	@Override
	public boolean hasNext() {
		return next != null;
	}

	@Override
	public Path next() {
		
		Path result = next;
		
		lookahead();
		
		return result;
	}
	
	/**
	 * Loads a new value into {@link #next}, buffering a new directory listing if needed, or sets it to null if there is
	 * no more data available.
	 */
	private void lookahead() {
		while(outputBuffer.isEmpty() && !queue.isEmpty()) populateBuffer();
		next = outputBuffer.pollLast();
	}
	
	private void populateBuffer() {
		if (queue.isEmpty()) return;
		Path cur = queue.removeLast();
		try (DirectoryStream<Path> ls = Files.newDirectoryStream(cur)) {
			for(Path entry : ls) {
				if (Files.isDirectory(entry)) {
					if (recursive) queue.addFirst(entry);
				} else if (Files.isRegularFile(entry)) {
					outputBuffer.add(entry);
				}
			}
		} catch (IOException e) {
			//We can't list the directory - either it doesn't exist or we don't have permission.
			//Either way just omit it from the results.
		}
	}
	
	public static Stream<Path> findFiles(Path basePath, boolean recursive) {
		return StreamSupport.stream(
			Spliterators.spliteratorUnknownSize(
				new FileIterator(basePath, recursive),
				Spliterator.CONCURRENT | Spliterator.NONNULL
			),
			false
		);
	}
}
