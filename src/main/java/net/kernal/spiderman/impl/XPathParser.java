package net.kernal.spiderman.impl;

import java.util.ArrayList;
import java.util.List;

import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import net.kernal.spiderman.AbstractParser;
import net.kernal.spiderman.K;

public class XPathParser extends AbstractParser {

	private String xpath;
	private String attr;
	public XPathParser(String xpath, String attr) {
		this.xpath = xpath;
		this.attr = attr;
	}
	
	public void parse() {
		TagNode parsedResult = (TagNode)context.getModelParsed();
		if (xpath.endsWith("/text()")) {
			xpath = xpath.replace("/text()", "");
			Object[] nodes = null;
			try {
				nodes = parsedResult.evaluateXPath(xpath);
			} catch (XPatherException e) {
				e.printStackTrace();
			}
			if (K.isEmpty(nodes)) return;
			
			List<String> tmpList = new ArrayList<String>();
			for (Object node : nodes){
				String nodeValue = node.toString();
				tmpList.add(nodeValue);
			}
			context.setFieldParsed(tmpList);
		} else {
			Object[] nodes = null;
			try {
				nodes = parsedResult.evaluateXPath(xpath);
			} catch (XPatherException e) {
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
					context.setFieldParsed(tmpList);
				} else {
					context.setFieldParsed(nodes);
				}
			}
		}
	}

}
