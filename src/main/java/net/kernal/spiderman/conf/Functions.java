package net.kernal.spiderman.conf;

import java.util.HashMap;
import java.util.Map;

import net.kernal.spiderman.parser.TransformParser;

public class Functions {

	private Map<String, TransformParser> functions;
	public Functions() {
		this.functions = new HashMap<String, TransformParser>();
	}
	
	public Functions register(String name, TransformParser function) {
		this.functions.put(name, function);
		return this;
	}
	
	public Map<String, TransformParser> all() {
		return this.functions;
	}
	
}
