package net.kernal.spiderman.worker.extract.extractor.impl;

import net.kernal.spiderman.kit.K;
import net.kernal.spiderman.worker.extract.ExtractTask;
import net.kernal.spiderman.worker.extract.extractor.AbstractXPathExtractor;
import net.kernal.spiderman.worker.extract.extractor.Extractor;
import net.kernal.spiderman.worker.extract.schema.Field;
import net.kernal.spiderman.worker.extract.schema.Model;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.SimpleXmlSerializer;
import org.htmlcleaner.TagNode;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

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

    @Override
    protected List<Object> extractModel(Object aDoc, String modelXpath) {
        TagNode doc = (TagNode) aDoc;
        Object[] nodeArray = new TagNode[]{doc};
        if (K.isNotBlank(modelXpath)) {
            try {
            	nodeArray = doc.evaluateXPath(modelXpath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (nodeArray == null || nodeArray.length == 0) {
            return null;
        }

        return Arrays.asList(nodeArray);
    }

    protected List<Object> extractField(Object model, Field field, String aXpath, String attr, boolean isSerialize) {
        final List<Object> values = new ArrayList<>();
        TagNode mNode = (TagNode) model;
        Object[] nodeArray = null;
        String xpath = aXpath.replace("/text()", "");
        try {
        	nodeArray = mNode.evaluateXPath(xpath);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        if (nodeArray == null || nodeArray.length == 0) {
            return null;
        }

        for (int i = 0; i < nodeArray.length; i++) {
            TagNode tagNode = (TagNode) nodeArray[i];
            if (aXpath.endsWith("/text()")) {
                values.add(tagNode.getText().toString());
                continue;
            }

            Object value;
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
        Map<String, String> r = new HashMap<>();
        TagNode tagNode = (TagNode) node;
        r.putAll(tagNode.getAttributes());
        return r;
    }

}
