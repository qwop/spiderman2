package net.kernal.spiderman.worker.extract.extractor.impl;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.SimpleXmlSerializer;
import org.htmlcleaner.TagNode;

import com.alibaba.fastjson.JSON;

import net.kernal.spiderman.kit.K;
import net.kernal.spiderman.worker.extract.ExtractTask;
import net.kernal.spiderman.worker.extract.extractor.AbstractXPathExtractor;
import net.kernal.spiderman.worker.extract.extractor.Extractor;
import net.kernal.spiderman.worker.extract.schema.Field;
import net.kernal.spiderman.worker.extract.schema.Model;

public class HtmlCleanerExtractor extends AbstractXPathExtractor {

	public static Extractor.Builder builder() {
		return (t, p, ms) -> new HtmlCleanerExtractor(t, p, ms);
	}
	
	private HtmlCleaner htmlCleaner;
	private TagNode doc;
	
	public HtmlCleanerExtractor(String html, Model... models) {
		super(null, null, models);
		// 使用HtmlCleaner组件
		htmlCleaner = new HtmlCleaner();
		htmlCleaner.getProperties().setTreatDeprecatedTagsAsContent(true);
		this.doc = htmlCleaner.clean(html);
	}
	
	public HtmlCleanerExtractor(ExtractTask task, String page, Model... models) {
		super(task, page, models);
		final String html = task.getResponse().getBodyStr();
		// 使用HtmlCleaner组件
		htmlCleaner = new HtmlCleaner();
		htmlCleaner.getProperties().setTreatDeprecatedTagsAsContent(true);
		this.doc = htmlCleaner.clean(html);
	}
	
	protected Object getDoc() {
		return this.doc;
	}

	protected List<Object> extractModel(Object aDoc, String modelXpath) {
		TagNode doc = (TagNode)aDoc;
		Object[] nodeList = new TagNode[]{doc};
		if (K.isNotBlank(modelXpath)) {
			try {
				nodeList = doc.evaluateXPath(modelXpath);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (nodeList == null || nodeList.length == 0) {
			return null;
		}
		
		return Arrays.asList(nodeList);
	}
	
	protected List<Object> extractField(Object model, Field field, String aXpath, String attr, boolean isSerialize) {
		final List<Object> values = new ArrayList<Object>();
		TagNode mNode = (TagNode)model;
		Object[] nodeList = null;
		String xpath = aXpath.replace("/text()", "");
		try {
			nodeList = mNode.evaluateXPath(xpath.replace("/text()", ""));
		} catch (Throwable e) {
			e.printStackTrace();
		}
		if (nodeList == null || nodeList.length == 0) {
			return null;
		}
		
		for (int i = 0; i < nodeList.length; i++){
			TagNode tagNode = (TagNode)nodeList[i];
			if (aXpath.endsWith("/text()")) {
				values.add(tagNode.getText().toString());
				continue;
			} 
			
			Object value = null;
			if (K.isNotBlank(attr)) {
				value = tagNode.getAttributeByName(attr);
			} else if (isSerialize) {
				StringWriter sw = new StringWriter();
				CleanerProperties prop = htmlCleaner.getProperties();
				prop.setOmitXmlDeclaration(true);
				SimpleXmlSerializer ser = new SimpleXmlSerializer(prop);
				try {
					ser.write(tagNode, sw, getTask().getResponse().getCharset(), true);
				} catch (IOException e) {
					e.printStackTrace();
				}
		    	value = sw.getBuffer().toString();
			} else {
				value = tagNode;
			}
			values.add(value);
		}
		
		return values;
	}

	protected Map<String, String> extractAttributes(Object node) {
		Map<String, String> r = new HashMap<String, String>();
		TagNode tagNode = (TagNode)node;
//		final String text = tagNode.getText().toString();
//		if (K.isNotBlank(text)) {
//			r.put("text", text.trim());
//		}
		r.putAll(tagNode.getAttributes());
		return r;
	}
	
	public static void main(String[] args) {
		String html = "<html><title>Hello</title><targets><target name='vivi' /><target name='linda' /></targets></html>";
		Extractor extractor = new HtmlCleanerExtractor(html);
		Model page = new Model("page");
		page.addField("title").set("xpath", "//title/text()");
		page.addField("target").set("xpath", "//target").set("isAutoExtractAttrs", true).set("isArray", true);
		extractor.addModel(page);
		extractor.extract(new Callback(){
			public void onModelExtracted(ModelEntry entry) {
				System.out.println(entry.getModel().getName()+"->\r\n"+JSON.toJSONString(entry.getFields(), true)+"\r\n\r\n");
			}
			public void onFieldExtracted(FieldEntry entry) {
			}
		});
	}

}
