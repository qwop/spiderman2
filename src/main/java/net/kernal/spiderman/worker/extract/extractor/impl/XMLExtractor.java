package net.kernal.spiderman.worker.extract.extractor.impl;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.alibaba.fastjson.JSON;

import net.kernal.spiderman.Spiderman;
import net.kernal.spiderman.kit.K;
import net.kernal.spiderman.worker.extract.ExtractTask;
import net.kernal.spiderman.worker.extract.extractor.AbstractXPathExtractor;
import net.kernal.spiderman.worker.extract.extractor.Extractor;
import net.kernal.spiderman.worker.extract.schema.Field;
import net.kernal.spiderman.worker.extract.schema.Model;

public class XMLExtractor extends AbstractXPathExtractor {
	
	public static Extractor.Builder builder() {
		return (t, p, ms) -> new XMLExtractor(t, p, ms);
	}
	
	private XPath xpath;
	private Document doc;
	private Transformer transformer;
	
	public XMLExtractor(ExtractTask task, Model... models) {
		this(new ByteArrayInputStream(task.getResponse().getBody()), task, null, models);
	}
	
	public XMLExtractor(String file, Model... models) {
		this(K.asStream(file), null, null, models);
	}
	
	public XMLExtractor(ExtractTask task, String page, Model... models) {
		this(new ByteArrayInputStream(task.getResponse().getBody()), task, page, models);
	}
	
	public XMLExtractor(String file, String page, Model... models) {
		this(K.asStream(file), null, page, models);
	}
	
	public XMLExtractor(InputStream is, ExtractTask task, String page, Model... models) {
		super(task, page, models);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			this.doc = db.parse(is);
			XPathFactory xf = XPathFactory.newInstance();
			this.xpath = xf.newXPath();
			this.transformer = TransformerFactory.newInstance().newTransformer();
		} catch (Throwable e) {
			throw new Spiderman.Exception("初始化XML解析器失败", e);
		}
	}
	
	protected Object getDoc() {
		return this.doc;
	}
	
	protected List<Object> extractModel(Object doc, String modelXpath) {
		NodeList nodeList = null;
		try {
			nodeList = (NodeList)this.xpath.compile(modelXpath).evaluate(doc, XPathConstants.NODESET);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		if (nodeList == null || nodeList.getLength() == 0) {
			return null;
		}
		
		List<Object> mNodes = new ArrayList<Object>();
		for (int i = 0; i < nodeList.getLength(); i++){
			Node node = nodeList.item(i);
			mNodes.add(node);
		}
		return mNodes;
	}
	
	protected List<Object> extractField(Object model, Field field, String aXpath, String attr, boolean isSerialize) {
		final List<Object> values = new ArrayList<Object>();
		NodeList nodeList = null;
		String xpath = aXpath.replace("/text()", "");
		try {
			nodeList = (NodeList)this.xpath.compile(xpath).evaluate(model, XPathConstants.NODESET);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		if (nodeList == null || nodeList.getLength() == 0) {
			return null;
		}
		
		for (int j = 0; j < nodeList.getLength(); j++){
			Node node = nodeList.item(j);
			if (aXpath.endsWith("/text()")) {
				values.add(node.getTextContent());
				continue;
			} 
			
			Object value = null;
			if (K.isNotBlank(attr)) {
				Element e = (Element)node;
				value = e.getAttribute(attr);
			} else if (isSerialize) {
				StringWriter writer = new StringWriter();
				try {
					transformer.transform(new DOMSource(node), new StreamResult(writer));
					String str = writer.getBuffer().toString();
					value = new String(str.substring(str.indexOf("?>") + 2));
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			} else {
				value = node;
			}
			values.add(value);
		}
		
		return values;
	}
	
	protected Map<String, String> extractAttributes(Object node) {
		Map<String, String> r = new HashMap<String, String>();
		Element e = (Element)node;
		NamedNodeMap attrs = e.getAttributes();
		if (attrs.getLength() > 0) {
			for (int i = 0; i < attrs.getLength(); i++) {
				Node n = attrs.item(i);
				if (Node.ATTRIBUTE_NODE == n.getNodeType()) {
					r.put(n.getNodeName(), n.getNodeValue());
				}
			}
		}
		return r;
	}

	// test configure XML
	public static void main(String[] args) throws FileNotFoundException {
		// Property模型
		Model property = new Model("property")
				.set("xpath", "//property")
				.set("isAutoExtractAttrs", true);
		// Seed模型
		Model seed = new Model("seed")
				.set("xpath", "//seed")
				.set("isAutoExtractAttrs", true);
		seed.addField("text").set("xpath", "./text()");
		// Extractor定义
		Model extractors = new Model("extractor")
			.set("xpath", "//extractor[@alias]")
			.set("isArray", true)
			.set("isAutoExtractAttrs", true);
		// Filter定义
		Model filters = new Model("filter")
			.set("xpath", "//filter[@alias]")
			.set("isArray", true)
			.set("isAutoExtractAttrs", true);
		// Page模型
		Model page = new Model("extract-page")
				.set("xpath", "//page")
				.set("isArray", true)
				.set("isAutoExtractAttrs", true);
		// URL match rule
		Field urlMatchRule = page.addField("url-match-rule")
			.set("xpath", ".//url-match-rule")
			.set("isAutoExtractAttrs", true);
		urlMatchRule.addField("text").set("xpath", "./text()");
		// Models
		Field model = page.addField("model")
				.set("xpath", ".//model")
				.set("isAutoExtractAttrs", true)
				.set("isArray", true);
		// Fields
		Field field = model.addField("field")
			.set("xpath", ".//field")
			.set("isAutoExtractAttrs", true)
			.set("isArray", true);
		// Field's filters
		Field filter = field.addField("filters")
			.set("xpath", ".//filter[@type]")
			.set("isAutoExtractAttrs", true)
			.set("isArray", true);
		filter.addField("text").set("xpath", "./text()");
		// 抽取器
		final Extractor extractor = new XMLExtractor("spiderman.bootstrap.xml");
		extractor.addModel(property);
		extractor.addModel(seed);
		extractor.addModel(extractors);
		extractor.addModel(filters);
		extractor.addModel(page);
		extractor.extract(new Callback() {
			public void onModelExtracted(ModelEntry entry) {
				System.out.println(entry.getModel().getName()+"->\r\n"+JSON.toJSONString(entry.getFields(), true)+"\r\n\r\n");
			}
			public void onFieldExtracted(FieldEntry entry) {
			}
		});
	}

}
