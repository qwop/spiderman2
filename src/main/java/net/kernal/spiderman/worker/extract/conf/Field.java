package net.kernal.spiderman.worker.extract.conf;

import java.util.ArrayList;
import java.util.List;

import net.kernal.spiderman.Properties;
import net.kernal.spiderman.worker.extract.Extractor;

public class Field extends Properties {

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
	
	public Field set(String key, Object value) {
		this.put(key, value);
		return this;
	}
	
	public Field addFilter(ValueFilter transformer) {
		this.filters.add(transformer);
		return this;
	}
	
	public List<ValueFilter> getFilters() {
		return this.filters;
	}
	
	public static interface ValueFilter {
		public String filter(Extractor extractor, String value);
	}
}
