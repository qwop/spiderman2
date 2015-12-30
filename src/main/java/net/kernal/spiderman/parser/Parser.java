package net.kernal.spiderman.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.script.ScriptEngine;

import net.kernal.spiderman.Properties;

public interface Parser {

	public ParsedResult parse();
	public ParsedResult getParsedResult();
	public void setScriptEngine(ScriptEngine scriptEngine);
	
	public static class ParsedResult {
		private List<String[]> urlsForNewTask;//["HttpMethod", "URL"]
		private List<Object> objects;
		public static ParsedResult fromList(List<?> args) {
			return new ParsedResult(args.toArray());
		}
		public ParsedResult(Object... args) {
			this.objects = Arrays.asList(args);
			this.urlsForNewTask = new ArrayList<String[]>();
		}
		public List<String[]> getUrlsForNewTask() {
			return this.urlsForNewTask;
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
