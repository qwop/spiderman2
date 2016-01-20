package net.kernal.spiderman.conf;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import net.kernal.spiderman.K;
import net.kernal.spiderman.Properties;
import net.kernal.spiderman.Spiderman;
import net.kernal.spiderman.conf.Conf.Pages;
import net.kernal.spiderman.conf.Conf.Seeds;
import net.kernal.spiderman.worker.extract.ExtractTask;
import net.kernal.spiderman.worker.extract.Extractor;
import net.kernal.spiderman.worker.extract.Extractor.Callback;
import net.kernal.spiderman.worker.extract.XMLExtractor;
import net.kernal.spiderman.worker.extract.conf.Field;
import net.kernal.spiderman.worker.extract.conf.Model;
import net.kernal.spiderman.worker.extract.conf.Page;
import net.kernal.spiderman.worker.extract.conf.filter.ScriptableFilter;
import net.kernal.spiderman.worker.extract.conf.rule.ContainsRule;
import net.kernal.spiderman.worker.extract.conf.rule.EndsWithRule;
import net.kernal.spiderman.worker.extract.conf.rule.RegexRule;
import net.kernal.spiderman.worker.extract.conf.rule.StartsWithRule;

/**
 * 建议深入看看哦～
 * @author 赖伟威 l.weiwei@163.com 2015-12-28
 *
 */
public class XMLConfBuilder extends DefaultConfBuilder {
	
	private Extractor extractor;
	public XMLConfBuilder(File file) {
		super();
		try {
			this.extractor = new XMLExtractor(file);
		} catch (FileNotFoundException e) {
			throw new Spiderman.Exception("初始化XMLConfBuilder失败", e);
		}
		
		// Property模型
		Model property = new Model("property")
			.set("xpath", "//property")
			.set("isAutoExtractAttrs", true);
		// Seed模型
		Model seed = new Model("seed")
			.set("xpath", "//seed")
			.set("isAutoExtractAttrs", true);
		seed.addField("text").set("xpath", "./text()");
		// Script模型
		Model script = new Model("script")
			.set("xpath", "//script")
			.set("isAutoExtractAttrs", true);
		script.addField("text").set("xpath", "./text()");
		// Extractor定义
		Model extractors = new Model("extractor")
			.set("xpath", "//extractor[@name]")
			.set("isArray", true)
			.set("isAutoExtractAttrs", true);
		// Filter定义
		Model filters = new Model("filter")
			.set("xpath", "//filter[@name]")
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
		// models
		Field model = page.addField("model")
			.set("xpath", ".//model")
			.set("isAutoExtractAttrs", true)
			.set("isArray", true);
		// fields
		Field field = model.addField("field")
			.set("xpath", ".//field")
			.set("isAutoExtractAttrs", true)
			.set("isArray", true);
		// filters
		Field filter = field.addField("filters")
			.set("xpath", ".//filter[@type]")
			.set("isAutoExtractAttrs", true)
			.set("isArray", true);
		filter.addField("text").set("xpath", "./text()");
		// 抽取器
		extractor.addModel(property);
		extractor.addModel(seed);
		extractor.addModel(script);
		extractor.addModel(extractors);
		extractor.addModel(filters);
		extractor.addModel(page);
	}
	
	public XMLConfBuilder addSeed(String url) {
		this.conf.addSeed(url);
		return this;
	}
	
	public XMLConfBuilder addSeed(String name, String url) {
		this.conf.addSeed(name, url);
		return this;
	}
	
	public XMLConfBuilder addPage(Page page) {
		page.config(page.getRules(), page.getModels());
		this.conf.getPages().add(page);
		return this;
	}
	
	public XMLConfBuilder bindObjectForScript(String name, Object obj) {
		this.conf.bindObjectForScript(name, obj);
		return this;
	}
	
	public XMLConfBuilder set(String key, Object value){
		this.conf.getParams().put(key, value);
		return this;
	}
	
	public Conf build() {
		super.build();
		// 开始解析
		final AtomicReference<String> defaultExtractorNames = new AtomicReference<String>();
		extractor.extract(new Callback() {
			public void onModelExtracted(ModelEntry entry) {
				final Properties values = entry.getProperties();
				switch(entry.getModel().getName()) {
				case "property":
			    	conf.set(values.getString("key"), values.getString("value", values.getString("text")));
				    break;
			    case "seed":
			    	conf.addSeed(values.getString("name"), values.getString("url", values.getString("text")));
				    break;
			    case "script":
			    	final String bindingsClassName = values.getString("bindings");
			    	if (K.isNotBlank(bindingsClassName)) {
			    		Class<Conf.Bindings> bindingsClass = K.loadClass(bindingsClassName);
			    		if (bindingsClass != null) {
			    			Conf.Bindings bindings;
							try {
								bindings = bindingsClass.newInstance();
							} catch (InstantiationException | IllegalAccessException e) {
								throw new Spiderman.Exception("实例化"+bindingsClassName+"失败", e);
							}
			    			bindings.config(conf.getBindings(), conf);
			    		}
			    	}
			    	conf.setScript(values.getString("value", values.getString("text")));
			    	break;
			    case "extractor":
			    	final String name = values.getString("name");
			    	final String className = values.getString("class");
			    	final boolean isDefault = values.getBoolean("isDefault", false);
			    	if (K.isBlank(name) || K.isBlank(className)) {
			    		break;
			    	}
			    	if (K.isBlank(defaultExtractorNames.get()) && isDefault) {
			    		defaultExtractorNames.set(name);
			    	}
					final Class<Extractor> cls = K.loadClass(className);
			    	conf.registerExtractor(name, cls);
			    	break;
			    case "filter":
			    	final String name2 = values.getString("name");
			    	final String className2 = values.getString("class");
			    	if (K.isBlank(name2) || K.isBlank(className2)) {
			    		break;
			    	}
					final Class<Field.ValueFilter> ftCls = K.loadClass(className2);
			    	Field.ValueFilter filter;
					try {
						filter = ftCls.newInstance();
					} catch (Exception e) {
						throw new Spiderman.Exception("过滤器[class="+ftCls.getName()+"]实例化失败", e); 
					}
			    	conf.registerFilter(name2, filter);
			    	break;
			    case "extract-page":
			    	final String pageName = values.getString("name");
			    	final Page page = new Page(pageName) {
						public void config(UrlMatchRules rules, Models models) {}
					};
					final boolean isUnique = values.getBoolean("isUnique", false);
					page.setTaskDuplicateCheckEnabled(isUnique);
					// 处理extractor
					final String extractorName = values.getString("extractor", defaultExtractorNames.get());
			    	handleExtractor(page, extractorName);
					
					// handle url match rule
					final Properties rule = values.getProperties("url-match-rule");
					if (rule == null) {
						throw new Spiderman.Exception("页面[name="+pageName+"]缺少URL匹配规则的配置");
					}
					String type = rule.getString("type", "");
					final boolean isNegativeEnabled = type.startsWith("!");
					if (isNegativeEnabled) {
						type = type.substring(1);
					}
					
					final String value = rule.getString("value", rule.getString("text"));
					switch(type) {
					case "regex":
						page.getRules().add(new RegexRule(value).setNegativeEnabled(isNegativeEnabled));
						break;
					case "startsWith":
						page.getRules().add(new StartsWithRule(value).setNegativeEnabled(isNegativeEnabled));
						break;
					case "endsWith":
						page.getRules().add(new EndsWithRule(value).setNegativeEnabled(isNegativeEnabled));
						break;
					case "contains":
						page.getRules().add(new ContainsRule(value).setNegativeEnabled(isNegativeEnabled));
						break;
					}
					// handle model
					List<Properties> models = values.getListProperties("model");
					if (K.isNotEmpty(models)) {
						models.forEach(mdl -> { 
							final String modelName = mdl.getString("name");
							final Model model = page.getModels().addModel(modelName);
							model.putAll(mdl);
							final List<Properties> fields = mdl.getListProperties("field");
							if (K.isNotEmpty(fields)) {
								fields.forEach(f -> {
									final String fieldName = f.getString("name");
									final Field field = model.addField(fieldName);
									field.putAll(f);
									// 处理Filters
									String ftName = f.getString("filter");
									if (K.isNotBlank(ftName)) {
										Field.ValueFilter rft = conf.getFilters().all().get(ftName);
										if (rft == null) {
											throw new Spiderman.Exception("页面[name="+pageName+"].模型[name="+modelName+"].Field[name="+fieldName+"]配置的属性[filter="+ftName+"]不存在");
										}
										field.addFilter(rft);
									}
									final List<Properties> filters = f.getListProperties("filters");
									if (K.isNotEmpty(filters)) {
										filters.forEach(ft -> {
											switch(ft.getString("type")) {
											case "script":
												field.addFilter(new ScriptableFilter(ft.getString("value", ft.getString("text"))));
											break;
											}
										});
									}
								});
							}
						});
					}
					// add page
			    	conf.addPage(page);
				    break;
				}
			}
			public void onFieldExtracted(FieldEntry entry) {
			}
		});
		return conf;
	}

	private void handleExtractor(Page page, String extractorName) {
		final String pageName = page.getName();
    	if (K.isBlank(extractorName)) {
    		throw new Spiderman.Exception("页面[name="+pageName+"]必须指定解析器");
    	}
    	final Map<String, Class<Extractor>> extractors = conf.getExtractors().all();
    	final Class<? extends Extractor> extractorClass = extractors.get(extractorName);
    	if (extractorClass == null) {
    		throw new Spiderman.Exception("页面[name="+pageName+"]指定的extractor[name="+extractorName+"]不存在");
    	}
    	final Constructor<? extends Extractor> ct;
		try {
			ct = extractorClass.getConstructor(ExtractTask.class, String.class, Model[].class);
		} catch (Exception e) {
			throw new Spiderman.Exception("页面[name="+pageName+"]指定的extractor[class="+extractorClass.getName()+"]的构造器不满足要求", e);
		}
    	if (ct == null) {
    		throw new Spiderman.Exception("页面[name="+pageName+"]指定的extractor[class="+extractorClass.getName()+"]的构造器不满足要求");
    	}
		page.setExtractorBuilder((t, p, mds) -> {
			try {
				return ct.newInstance(t, p, mds);
			} catch (Exception e) {
				throw new Spiderman.Exception("页面[name="+pageName+"]指定的extractor[class="+extractorClass.getName()+"]实例化失败", e);
			}
		});
	}
	
	public void configParams(Properties params) { }
	public void configSeeds(Seeds seeds) { }
	public void configPages(Pages pages) { }
	public void configBindings(Map<String, Object> bindings) {}
	
}
