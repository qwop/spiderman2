package net.kernal.spiderman.parser;

import net.kernal.spiderman.conf.Functions;

public class JavaInvoker {

	private FieldParser parser;
	private Functions functions;
	public JavaInvoker(Functions functions) {
		this.functions = functions;
	}
	public void setParser(FieldParser parser) {
		this.parser = parser;
	}
	
	public Object invoke(String funcName, Object arg) {
		if (functions == null || functions.all() == null) {
			return null;
		}
		
		TransformParser parser = functions.all().get(funcName);
		if (parser == null) {
			return null;
		}
		parser.setModelParser(this.parser.modelParser);
		parser.setPrevParserResult(this.parser.getParsedResult());
		parser.setScriptEngine(this.parser.scriptEngine);
		
		return parser.transform(arg);
	}
	
}
