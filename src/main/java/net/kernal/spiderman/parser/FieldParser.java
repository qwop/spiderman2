package net.kernal.spiderman.parser;

public abstract class FieldParser implements Parser {

	protected ModelParser modelParser;
	protected ParsedResult prevParsedResult;
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
	
}
