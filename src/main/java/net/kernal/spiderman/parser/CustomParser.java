package net.kernal.spiderman.parser;

public abstract class CustomParser extends FieldParser {

	private ParsedResult parsedResult;
	
	public CustomParser() {}

	public ParsedResult parse() {
		return this.parse(this.prevParsedResult);
	}
	
	public abstract ParsedResult parse(ParsedResult prevResult);
	
	public ParsedResult getParsedResult() {
		return this.parsedResult;
	}
	
	
}
