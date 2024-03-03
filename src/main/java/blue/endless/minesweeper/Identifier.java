package blue.endless.minesweeper;

import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record Identifier(String namespace, String path, String selector) {
	public static final String MINESWEEPER_NAMESPACE = "ms";
	private static final Pattern VALID_NAMESPACE = Pattern.compile("([a-z0-9_]+)");
	private static final Pattern VALID_PATH = Pattern.compile("([a-z0-9_/\\.]+)");
	private static final Pattern VALID_SELECTOR = Pattern.compile("[a-z0-9_/\\.]+");
	
	private static final Predicate<String> IS_VALID_NAMESPACE = VALID_NAMESPACE.asMatchPredicate();
	private static final Predicate<String> IS_VALID_PATH = VALID_PATH.asMatchPredicate();
	private static final Predicate<String> IS_VALID_SELECTOR = VALID_SELECTOR.asMatchPredicate();
	
	private static final Pattern VALID_IDENTIFIER = Pattern.compile("([a-z0-9_]+):([a-z0-9_/\\.]+)(?::([a-z0-9_/\\.]+))?");
	
	public Identifier(String namespace, String path, String selector) {
		this.namespace = namespace;
		this.path = path;
		this.selector = (selector==null) ? "" : selector;
		
		if (!IS_VALID_NAMESPACE.test(namespace)) throw new IllegalArgumentException("Invalid characters in namespace.");
		if (!IS_VALID_PATH.test(namespace)) throw new IllegalArgumentException("Invalid characters in path.");
		if (!selector.isEmpty() && !IS_VALID_SELECTOR.test(namespace)) throw new IllegalArgumentException("Invalid characters in selector.");
	}
	
	public static Identifier of(String s) {
		Matcher m = VALID_IDENTIFIER.matcher(s);
		if (!m.find()) throw new IllegalArgumentException("Invalid Identifier.");
		String namespace = m.group(1);
		String path = m.group(2);
		String selector = m.group(3);
		if (selector == null) selector = "";
		
		return new Identifier(namespace, path, selector);
	}
}
