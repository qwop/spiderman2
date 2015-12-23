package net.kernal.spiderman.parser;

import java.util.Arrays;
import java.util.List;

import net.kernal.spiderman.Properties;

public interface Parser {

	public ParsedResult parse();
	public ParsedResult getParsedResult();
	
	public static class ParsedResult {
		private List<Object> objects;
		public static ParsedResult fromList(List<?> args) {
			return new ParsedResult(args.toArray());
		}
		public ParsedResult(Object... args) {
			this.objects = Arrays.asList(args);
		}
		public List<Object> all() {
			return objects;
		}
		public Object first() {
			return objects == null ? null : objects.isEmpty() ? null : objects.get(0);
		}
	}
	
	/**
	 * 模型
	 */
	public static class Model extends Properties {
		private static final long serialVersionUID = 1L;
	}
	
}
