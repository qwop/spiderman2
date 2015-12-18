package net.kernal.spiderman.impl;

import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;

import net.kernal.spiderman.AbstractParser;
import net.kernal.spiderman.K;

/**
 * HtmlCleaner解析器
 * @author 赖伟威 l.weiwei@163.com 2015-12-01
 *
 */
public class HtmlCleanerParser extends AbstractParser {

	private String xpath;
	public HtmlCleanerParser(){}
	public HtmlCleanerParser(String xpath) {
		this.xpath = xpath;
	}
	
	/**
	 * 解析网页内容
	 */
	public void parse() {
		// 使用HtmlCleaner组件
		HtmlCleaner cleaner = new HtmlCleaner();
		cleaner.getProperties().setTreatDeprecatedTagsAsContent(true);
		TagNode rootNode = cleaner.clean(context.getResponse().getHtml());
		// FIXME
		context.setModelParsed(rootNode);
		if (K.isNotBlank(xpath)){
			Object[] nodes = null;
	        try {
	        	nodes = rootNode.evaluateXPath(xpath);
			} catch (Throwable e) {
				throw new RuntimeException("Target["+context.getTarget().getName()+"].model.xpath["+xpath+"] eval failed!", e);
			}
	        context.setModelParsed(nodes);
		}
	}

//	private Parser.Model parseModel(final Target.Model.Field.Parser.ParserContext context) {
//		Parser.Model parsedModel = new Parser.Model();
//		final Target target = context.getTarget();
//		for (Target.Model.Field field : target.getModel().getFields()){
//			final String fieldName = field.getName();
//			
//			K.foreach(field.getParsers(), new K.ForeachCallback<Target.Model.Field.Parser>() {
//				public void each(int i, net.kernal.spiderman.Target.Model.Field.Parser parser) {
//					new SmartParser(parser).parse(context);;
//				}
//			});
//			
//			parsedModel.put(fieldName, context.getParsed());
//		}
//		
//		return parsedModel;
//	}
//	
//	private static class SmartParser implements Target.Model.Field.Parser {
//		private Target.Model.Field.Parser parser;
//		public SmartParser(Target.Model.Field.Parser parser) {
//			this.parser = parser;
//		}
//		public void parse(ParserContext context) {
//			if (parser instanceof Target.Model.Field.XpathParser) {
//				try {
//					new XpathParser(parser).parse(context);
//				} catch (XPatherException e) {
//					e.printStackTrace();
//				}
//			}
//			if (parser instanceof Target.Model.Field.RegexParser) {
//				try {
//					new RegexParser(parser).parse(context);
//				} catch (Exception e) {
//				}
//			}
//			try {
//				parser.parse(context);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//	}
//	
//	private static class RegexParser implements Target.Model.Field.Parser {
//		private Target.Model.Field.RegexParser p;
//		public RegexParser(Target.Model.Field.Parser parser) {
//			this.p = (Target.Model.Field.RegexParser)parser;
//		}
//		public void parse(ParserContext context) throws Exception {
//			String regex = this.p.getRegex();
//			@SuppressWarnings("unchecked")
//			List<String> tmpList = (List<String>)context.getParsed();
//			List<String> newValues = new ArrayList<String>();
//			for (int j = 0; j < tmpList.size(); j++) {
//				String input = tmpList.get(j);
//				String newValue = K.findOneByRegex(input, regex);
//				if (K.isNotBlank(newValue)){
//					newValues.add(newValue);
//				} else {
//					newValues.add(input);
//				}
//			}
//			context.setParsed(newValues);
//		}
//		
//	}
//	
//	private static class XpathParser implements Target.Model.Field.Parser {
//		private Target.Model.Field.XpathParser p;
//		public XpathParser(Target.Model.Field.Parser parser) {
//			this.p = (Target.Model.Field.XpathParser)parser;
//		}
//		public void parse(ParserContext context) throws XPatherException {
//			TagNode parsedResult = (TagNode)context.getParsed();
//			String xpath = p.getXpath();
//			if (xpath.endsWith("/text()")) {
//				xpath = xpath.replace("/text()", "");
//				Object[] nodes = parsedResult.evaluateXPath(xpath);
//				if (K.isEmpty(nodes)) return;
//				
//				List<String> tmpList = new ArrayList<String>();
//				for (Object node : nodes){
//					String nodeValue = node.toString();
//					tmpList.add(nodeValue);
//				}
//				context.setParsed(tmpList);
//			} else {
//				Object[] nodes = parsedResult.evaluateXPath(xpath);
//				if (K.isNotEmpty(nodes)) {
//					String attr = p.getAttr();
//					if (K.isNotBlank(attr)) {
//						List<String> tmpList = new ArrayList<String>();
//						for (Object node : nodes){
//							TagNode tagNode = (TagNode)node;
//							String attrVal = tagNode.getAttributeByName(attr);
//							tmpList.add(attrVal);
//						}
//						context.setParsed(tmpList);
//					} else {
//						context.setParsed(nodes);
//					}
//				}
//			}
//		}
//		
//	}

}
