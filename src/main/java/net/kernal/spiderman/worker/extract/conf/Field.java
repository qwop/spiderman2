package net.kernal.spiderman.worker.extract.conf;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import net.kernal.spiderman.Properties;
import net.kernal.spiderman.worker.extract.Extractor;

public class Field extends Properties {

	private static final Logger logger = Logger.getLogger(Field.class.getName());
	
	private static final long serialVersionUID = 7148432045433277575L;
	
	private String page;
	private String model;
	private String name;
	private List<ValueFilter> filters;
	
	private List<Field> fields;
	
	public Field(String page, String model, String name) {
		this.page = page;
		this.model = model;
		this.name = name;
		this.filters = new ArrayList<ValueFilter>();
		this.fields = new ArrayList<Field>();
	}
	
	public String getPage() {
		return this.page;
	}
	
	public String getModel() {
		return this.model;
	}
	
	public List<Field> getFields() {
		return this.fields;
	}
	
	public Field addField(String name)  {
		Field field = new Field(page, this.name, name);
		this.fields.add(field);
		return field;
	}
	
	public Model toModel() {
		return new Model(page, name, fields).set("isAutoExtractAttrs", this.getBoolean("isAutoExtractAttrs"));
	}
	
	public String getName() {
		return this.name;
	}
	
	public Field set(String k, Object v) {
		this.put(k, v);
		return this;
	}
	
	public Field addFilter(ValueFilter ft) {
		this.filters.add(ft);
		logger.info("添加字段值过滤器: " + ft);
		return this;
	}
	
	public List<ValueFilter> getFilters() {
		return this.filters;
	}
	
	public static interface ValueFilter {
		public String filter(Extractor e, String v);
	}

	public String toString() {
		return "Field [page=" + page + ", model=" + model + ", name=" + name + "]";
	}
	
}
