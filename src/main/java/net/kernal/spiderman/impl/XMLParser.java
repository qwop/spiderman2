package net.kernal.spiderman.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import net.kernal.spiderman.Downloader;
import net.kernal.spiderman.K;
import net.kernal.spiderman.parser.FieldParser;
import net.kernal.spiderman.parser.ModelParser;
import net.kernal.spiderman.parser.XPathFieldParser;
import net.kernal.spiderman.parser.XPathModelParser;

public class XMLParser extends XPathModelParser {

	private ParsedResult parsedResult;
	public ParsedResult getParsedResult() {
		return this.parsedResult;
	}
	
	private Document doc = null; 
    private XPath xpathObject = null;
	
	public XMLParser(String xpath) {
		super(null, xpath);
	}
	public XMLParser(String xpath, File xml) {
		super(null, xpath);
		try {
			this.init(new FileInputStream(xml));
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
	public XMLParser(Downloader.Response response, String xpath) {
		super(response, xpath);
		this.init(new ByteArrayInputStream(super.response.getBody()));
	}
	
	private void init(InputStream inputStream) {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	        DocumentBuilder dbd = dbf.newDocumentBuilder();
			this.doc = dbd.parse(inputStream);
			XPathFactory factory = XPathFactory.newInstance();
		    this.xpathObject = factory.newXPath();
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}
	
	public ParsedResult parse() {
        if (K.isBlank(xpath)) {
        	this.parsedResult = new ParsedResult(doc);
        	return this.parsedResult;
        }
        
		Object nodes = null;
		try {
			nodes = (NodeList)xpathObject.compile(xpath).evaluate(doc, XPathConstants.NODESET);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		if (nodes == null) return null;
		
		NodeList nodeList = (NodeList)nodes;
		if (nodeList.getLength() == 0) return null;
		List<Object> list = new ArrayList<Object>();
		for (int i = 0; i < nodeList.getLength(); i++){
			Node node = nodeList.item(i);
			list.add(node);
		}
		this.parsedResult = new ParsedResult(list.toArray());
        
        return this.parsedResult;
	}

	public static class FieldPaser extends XPathFieldParser {
		
		private ParsedResult parsedResult;
		public ParsedResult getParsedResult() {
			return this.parsedResult;
		}
		
		public FieldPaser(String xpath) {
			super(null, null, xpath, null);
		}
		public FieldPaser(String xpath, String attr) {
			super(null, null, xpath, attr);
		}
		
		public ParsedResult parse() {
			XMLParser mp = (XMLParser)super.modelParser;
			XPath xpathObj = mp.xpathObject;
			Object item = super.prevParsedResult.first();
			
			if (xpath.endsWith("/text()")) {
				xpath = xpath.replace("/text()", "");
				Object nodes = null;
				try {
					nodes = (NodeList)xpathObj.compile(xpath).evaluate(item, XPathConstants.NODESET);
				} catch (Throwable e) {
					e.printStackTrace();
				}
				if (nodes == null) return null;
				
				NodeList nodeList = (NodeList)nodes;
				if (nodeList.getLength() == 0) return null;
				
				List<String> tmpList = new ArrayList<String>();
				for (int i = 0; i < nodeList.getLength(); i++){
					Object node = nodeList.item(i);
					String nodeValue = node.toString();
					tmpList.add(nodeValue);
				}
				this.parsedResult = new ParsedResult(tmpList.toArray(new Object[]{}));
			} else {
				Object nodes = null;
				try {
					nodes = (NodeList)xpathObj.compile(xpath).evaluate(item, XPathConstants.NODESET);
				} catch (Throwable e) {
					e.printStackTrace();
				}
				if (nodes == null) return null;
				
				NodeList nodeList = (NodeList)nodes;
				if (nodeList.getLength() == 0) return null;
				List<Object> list = new ArrayList<Object>();
				for (int j = 0; j < nodeList.getLength(); j++){
					Node node = nodeList.item(j);
					if (K.isNotBlank(attr)){
						Element e = (Element)node;
						String attrVal = e.getAttribute(attr);
						list.add(attrVal);
					} else {
						list.add(node);
					}
				}
				
				this.parsedResult = new ParsedResult(list.toArray());
			}
			
			return this.parsedResult;
		}
	}
	
	
	public static void main(String[] args) {
		File file = new File("src/main/resources/baidu-web.xml");
		ModelParser p = new XMLParser("//targets//target[@name]", file);
		ParsedResult r = p.parse();
		for (Object obj : r.all()) {
			ParsedResult prev = new ParsedResult(obj);
			
			FieldParser p2 = new XMLParser.FieldPaser(".", "name");
			p2.setModelParser(p);
			p2.setPrevParserResult(prev);
			ParsedResult r2 = p2.parse();
			
			System.out.println("r2---->"+r2.all());
			
			FieldParser p3 = new XMLParser.FieldPaser(".//rules//rule[@value]", "value");
			p3.setModelParser(p);
			p3.setPrevParserResult(prev);
			ParsedResult r3 = p3.parse();
			
			System.out.println("r3---->"+r3.all());
		}
	}
	
}
