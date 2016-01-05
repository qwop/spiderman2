package net.kernal.spiderman.parser;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.SimpleXmlSerializer;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import net.kernal.spiderman.K;

/**
 * 模型解析器，基于HtmlCleaner实现, 且具备XPath解析能力
 * @author 赖伟威 l.weiwei@163.com 2015-12-10
 *
 */
public class HtmlCleanerParser extends ModelParser {

	private String xpath;
	private ParsedResult parsedResult;
	private HtmlCleaner htmlCleaner;
	
	private TagNode rootNode;
	public HtmlCleanerParser() {
		super(null);
	}
	public HtmlCleanerParser(String xpath) {
		super(null);
		this.xpath = xpath;
	}
	public HtmlCleanerParser(String html, String xpath) {
		super(null);
		this.xpath = xpath;
		this.init(html);
	}
	public ModelParser afterSetTask() {
		this.init(task.getResponse().getBodyStr());
		return this;
	}
	public String getXPath() {
		return this.xpath;
	}
	
	private void init(final String html) {
		// 使用HtmlCleaner组件
		this.htmlCleaner = new HtmlCleaner();
		this.htmlCleaner.getProperties().setTreatDeprecatedTagsAsContent(true);
		this.rootNode = this.htmlCleaner.clean(html);
	}

	public ParsedResult parse() {
		if (K.isBlank(xpath)) {
			return new ParsedResult(this.rootNode);
		}
		try {
			Object[] nodes = this.rootNode.evaluateXPath(xpath);
			if (K.isNotEmpty(nodes)) {
				return new ParsedResult(nodes);
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}

	public static class FieldPaser extends FieldParser  {
		private String xpath;
		private String attr;
		private ParsedResult parsedResult;
		public FieldPaser(String xpath) {
			super();
			this.xpath = xpath;
		}
		public FieldPaser(String xpath, String attr) {
			super();
			this.xpath = xpath;
			this.attr = attr;
		}
		
		public ParsedResult parse() {
			TagNode prevParsedResult = (TagNode)this.prevParsedResult.first();
			if (xpath.endsWith("/text()")) {
				xpath = xpath.replace("/text()", "");
				Object[] nodes = null;
				try {
					nodes = prevParsedResult.evaluateXPath(xpath);
				} catch (Throwable e) {
					e.printStackTrace();
				}
				if (K.isEmpty(nodes)) return null;
				
				List<String> tmpList = new ArrayList<String>();
				for (Object node : nodes){
					String nodeValue = node.toString();
					tmpList.add(nodeValue);
				}
				this.parsedResult = new ParsedResult(tmpList.toArray(new Object[]{}));
			} else {
				Object[] nodes = null;
				try {
					nodes = prevParsedResult.evaluateXPath(xpath);
				} catch (Throwable e) {
					e.printStackTrace();
				}
				if (K.isNotEmpty(nodes)) {
					if (K.isNotBlank(attr)) {
						List<String> tmpList = new ArrayList<String>();
						for (Object node : nodes){
							TagNode tagNode = (TagNode)node;
							String attrVal = tagNode.getAttributeByName(attr);
							tmpList.add(attrVal);
						}
						this.parsedResult =  new ParsedResult(tmpList.toArray(new Object[]{}));
					} else if (super.isSerialize) {
						final String charset = super.modelParser.getTask().getResponse().getCharset();
						HtmlCleaner cleaner = ((HtmlCleanerParser)super.modelParser).htmlCleaner;
						StringWriter sw = new StringWriter();
						CleanerProperties prop = cleaner.getProperties();
						SimpleXmlSerializer ser = new SimpleXmlSerializer(prop);
						List<String> tmpList = new ArrayList<String>();
						for (Object node : nodes){
							TagNode tagNode = (TagNode)node;
							try {
								ser.write(tagNode, sw, charset, true);
							} catch (IOException e) {
								e.printStackTrace();
							}
					    	String out = sw.getBuffer().toString();
							tmpList.add(out);
						}
						this.parsedResult =  new ParsedResult(tmpList.toArray(new Object[]{}));
					} else {
						this.parsedResult =  new ParsedResult(nodes);
					}
				}
			}
			return this.parsedResult;
		}
		public ParsedResult getParsedResult() {
			return this.parsedResult;
		}
	}
	
	public ParsedResult getParsedResult() {
		return this.parsedResult;
	}
	
	public static void main(String[] args) throws XPatherException {
		String html = "<html><title>Hello</title><targets><target name='vivi' /><target name='linda' /></targets></html>";
		String xpath = "//target";
		Parser p1 = new HtmlCleanerParser(html, xpath);
		final ParsedResult r = p1.parse();
		System.out.println(r.all());
		K.foreach(r.all(), new K.ForeachCallback<Object>(){
			public void each(int i, Object item) {
				FieldParser p2 = new HtmlCleanerParser.FieldPaser(".", "name");
				p2.setPrevParserResult(new ParsedResult(item));
				
				ParsedResult r2 = p2.parse();
				System.out.println(r2.first());
			}
		});
	}
	
}
