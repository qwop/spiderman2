package net.kernal.spiderman.impl;

import java.util.ArrayList;
import java.util.List;

import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;

import net.kernal.spiderman.Downloader;
import net.kernal.spiderman.K;
import net.kernal.spiderman.parser.FieldParser;
import net.kernal.spiderman.parser.Parser;
import net.kernal.spiderman.parser.XPathFieldParser;
import net.kernal.spiderman.parser.XPathModelParser;

public class HtmlCleanerParser extends XPathModelParser {

	private ParsedResult parsedResult;
	
	private TagNode doc;
	public HtmlCleanerParser() {
		super(null, null);
	}
	public HtmlCleanerParser(String xpath) {
		super(null, xpath);
	}
	public HtmlCleanerParser(String html, String xpath) {
		super(null, xpath);
		this.init(html);
	}
	public void setResponse(Downloader.Response response) {
		this.init(response.getHtml());
	}
	
	private void init(final String html) {
		// 使用HtmlCleaner组件
		HtmlCleaner cleaner = new HtmlCleaner();
		cleaner.getProperties().setTreatDeprecatedTagsAsContent(true);
		this.doc = cleaner.clean(html);
	}

	public ParsedResult parse() {
		if (K.isBlank(xpath)) {
			return new ParsedResult(this.doc);
		}
		try {
			Object[] nodes = this.doc.evaluateXPath(xpath);
			if (K.isNotEmpty(nodes)) {
				return new ParsedResult(nodes);
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}

	public static class FieldPaser extends XPathFieldParser  {

		private ParsedResult parsedResult;
		
		public FieldPaser(String xpath) {
			super(null, null, xpath, null);
		}
		public FieldPaser(String xpath, String attr) {
			super(null, null, xpath, attr);
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
	
	public static void main(String[] args) {
		final String html = "<html><title>Hello</title><targets><target name='vivi' /><target name='linda' /></targets></html>";
		Parser p1 = new HtmlCleanerParser(html, "//target");
		final ParsedResult r = p1.parse();
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
