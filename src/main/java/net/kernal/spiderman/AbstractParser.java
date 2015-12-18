package net.kernal.spiderman;

public abstract class AbstractParser implements Parser {
	
	protected ParserContext context;
	protected Parser prevParser;
	protected Parser nextParser;
	
	public void setNextParser(Parser nextParser) {
		this.nextParser = nextParser;
		nextParser.setPrevParser(this);
	}
	public void setPrevParser(Parser parser) {
		this.prevParser = parser;
	}
	public Parser getPrevParser() {
		return this.prevParser;
	}
	
	/**
	 * 根据目标规则去解析下载下来的网页内容，并将解析结果返回
	 * @param response 下载下来的网页内容
	 * @param target 包含解析规则的目标对象
	 * @return 返回解析后的结果
	 */
	public void parse(ParserContext context) {
		this.context = context;
		this.parse();
		if (this.nextParser != null) {
			this.nextParser.setPrevParser(this);
			this.nextParser.parse(context);
		}
	}
	
	public abstract void parse();
	
}
