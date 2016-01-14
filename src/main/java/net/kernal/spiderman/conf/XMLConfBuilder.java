package net.kernal.spiderman.conf;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import net.kernal.spiderman.K;
import net.kernal.spiderman.Properties;
import net.kernal.spiderman.Spiderman;
import net.kernal.spiderman.conf.Conf.Pages;
import net.kernal.spiderman.conf.Conf.Seeds;
import net.kernal.spiderman.worker.extract.Extractor;
import net.kernal.spiderman.worker.extract.HtmlCleanerExtractor;
import net.kernal.spiderman.worker.extract.XMLExtractor;
import net.kernal.spiderman.worker.extract.Extractor.Callback;
import net.kernal.spiderman.worker.extract.conf.Field;
import net.kernal.spiderman.worker.extract.conf.Model;
import net.kernal.spiderman.worker.extract.conf.Page;
import net.kernal.spiderman.worker.extract.conf.filter.ScriptableFilter;
import net.kernal.spiderman.worker.extract.conf.rule.ContainsRule;
import net.kernal.spiderman.worker.extract.conf.rule.EndsWithRule;
import net.kernal.spiderman.worker.extract.conf.rule.RegexRule;
import net.kernal.spiderman.worker.extract.conf.rule.StartsWithRule;

/**
 * 此类解析规则比较复杂，建议暂时别太深入去看 :)
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
		// ExtractPage模型
		Model page = new Model("extract-page")
				.set("xpath", "//extract-page")
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
		Field filter = field.addField("filter")
			.set("xpath", ".//filter")
			.set("isAutoExtractAttrs", true)
			.set("isArray", true);
		filter.addField("text").set("xpath", "./text()");
		// 抽取器
		extractor.addModel(property);
		extractor.addModel(seed);
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
	
	public XMLConfBuilder set(String key, Object value){
		this.conf.getParams().put(key, value);
		return this;
	}
	
	public Conf build() {
		super.build();
		// 开始解析
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
			    case "extract-page":
			    	final String pageName = values.getString("name");
			    	final String extractorName = values.getString("extractor", "HtmlCleaner");
			    	Page page = new Page(pageName) {
						public void config(UrlMatchRules rules, Models models) {}
					};
					page.setExtractorBuilder("XML".equals(extractorName) ? XMLExtractor.builder() : HtmlCleanerExtractor.builder());
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
							model.remove("field");
							final List<Properties> fields = mdl.getListProperties("field");
							if (K.isNotEmpty(fields)) {
								fields.forEach(f -> {
									final String fieldName = f.getString("name");
									final Field field = model.addField(fieldName);
									field.putAll(f);
									field.remove("filter");
									final List<Properties> filters = f.getListProperties("filter");
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

	public void configParams(Properties params) { }
	public void configSeeds(Seeds seeds) { }
	public void configPages(Pages pages) { }
	
	public static void main(String[] args) {
		new XMLConfBuilder(new File("src/main/resources/baidu-search.xml")).build();
	}

}
