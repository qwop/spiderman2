package net.kernal.spiderman.parser;

public abstract class XPathFieldParser extends FieldParser {
	
	protected String xpath;
	protected String attr;
	
	public XPathFieldParser(ModelParser modelParser, ParsedResult prevParserResult, String xpath, String attr) {
		super(modelParser, prevParserResult);
		this.xpath = xpath;
		this.attr = attr;
	}
}
