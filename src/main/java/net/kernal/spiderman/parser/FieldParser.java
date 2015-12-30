package net.kernal.spiderman.parser;

import javax.script.ScriptEngine;

public abstract class FieldParser implements Parser {

	protected ScriptEngine scriptEngine;
	protected ModelParser modelParser;
	protected ParsedResult prevParsedResult;
	protected boolean isSerialize;
	public FieldParser() {}
	public FieldParser(ModelParser modelParser, ParsedResult prevParsedResult) {
		this.modelParser = modelParser;
		this.prevParsedResult = prevParsedResult;
	}
	public void setPrevParserResult(ParsedResult prevParsedResult) {
		this.prevParsedResult = prevParsedResult;
	}
	public void setModelParser(ModelParser modelParser) {
		this.modelParser = modelParser;
	}
	public void setIsSerialize(boolean isSerialize) {
		this.isSerialize = isSerialize;
	}
	public void setScriptEngine(ScriptEngine scriptEngine) {
		this.scriptEngine = scriptEngine;
	}
}
