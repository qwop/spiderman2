package net.kernal.spiderman.conf;

import java.util.ArrayList;
import java.util.List;

import net.kernal.spiderman.K;
import net.kernal.spiderman.Properties;
import net.kernal.spiderman.downloader.Downloader;
import net.kernal.spiderman.parser.FieldParser;
import net.kernal.spiderman.parser.ModelParser;
import net.kernal.spiderman.parser.Parser;
import net.kernal.spiderman.parser.Parser.ParsedResult;

 /**
  * 目标配置
  * @author 赖伟威 l.weiwei@163.com
  *
  */
public abstract class Target {
	
	/**
	 * 解析目标
	 * @param target 匹配到的目标配置
	 * @return 解析结果
	 */
	public ParsedResult parse() {
		return this.parse(null);
	}
	/**
	 * 解析目标
	 * @param target 匹配到的目标配置
	 * @param response 下载器返回的响应结果
	 * @return 解析结果
	 */
	public ParsedResult parse(final Downloader.Response response) {
		if (model.getParser() == null) {
			throw new RuntimeException("请为Target["+this.getName()+"].Model设置解析器，比如model.addParser");
		}
		model.setResponse(response);
		return model.parse();
	}
	
	public Target(String name) {
		this.name = name;
		this.model = new Model();
		this.rules = new Rules();
	}
	
	private String name;
	private Model model;
	private Rules rules;
	
	public abstract void configRules(Rules rules);
	public abstract void configModel(Model model);
	
	public boolean matches(Downloader.Request request) {
		boolean matched = true;
		// FIXME 暂时使用and逻辑
		for (Rule r : rules.rules) {
			matched = r.matches(request);
			if (!matched) {
				return false;
			}
		}
		return matched;
	}
	
	public List<Model.Field> getTheFieldsWhichForNewTask() {
		final List<Model.Field> list = new ArrayList<Model.Field>();
		K.foreach(this.model.getFields(), new K.ForeachCallback<Model.Field>() {
			public void each(int i, Model.Field field) {
				if (field.isForNewTask) {
					list.add(field);
				}
			}
		});
		return list;
	}
	
	public static class Rules {
		private int priority = 10;
		private List<Rule> rules;
		public Rules() {
			this.rules = new ArrayList<Rule>();
		}
		public void addNotRegexRule(String regex) {
			this.rules.add(new RegexRule(regex, false));
		}
		public void addNotStartsWithRule(String prefix) {
			this.rules.add(new StartsWithRule(prefix, true));
		}
		public void addNotEndsWithRule(String suffix) {
			this.rules.add(new EndsWithRule(suffix, true));
		}
		public void addNotContainsRule(String chars) {
			this.rules.add(new ContainsRule(chars, true));
		}
		public void addRegexRule(String regex) {
			this.rules.add(new RegexRule(regex, false));
		}
		public void addStartsWithRule(String prefix) {
			this.rules.add(new StartsWithRule(prefix, false));
		}
		public void addEndsWithRule(String suffix) {
			this.rules.add(new EndsWithRule(suffix, false));
		}
		public void addContainsRule(String chars) {
			this.rules.add(new ContainsRule(chars, false));
		}
		public void add(Rule rule) {
			this.rules.add(rule);
		}
		public List<Rule> getAll() {
			return this.rules;
		}
		public Rules setPriority(int priority) {
			this.priority = priority;
			return this;
		}
		public int getPriority() {
			return this.priority;
		}
	}
	
	public static interface Rule {
		public boolean matches(Downloader.Request request);
	}
	
	public static class RegexRule implements Rule {
		private boolean isNegative;
		private String regex;
		public RegexRule(String regex, boolean isNegative) {
			this.regex = regex;
			this.isNegative = isNegative;
		}
		public boolean matches(Downloader.Request request) {
			boolean r = request.getUrl().matches(this.regex);
			return isNegative ? !r : r;
		}
	}
	
	public static class StartsWithRule implements Rule {
		private boolean isNegative;
		private String prefix;
		public StartsWithRule(String prefix, boolean isNegative) {
			this.prefix = prefix;
			this.isNegative = isNegative;
		}
		public boolean matches(Downloader.Request request) {
			boolean r = request.getUrl().startsWith(prefix);
			return isNegative ? !r : r;
		}
	}
	
	public static class EndsWithRule implements Rule {
		private boolean isNegative;
		private String suffix;
		public EndsWithRule(String suffix, boolean isNegative) {
			this.suffix = suffix;
			this.isNegative = isNegative;
		}
		public boolean matches(Downloader.Request request) {
			boolean r = request.getUrl().endsWith(suffix);
			return isNegative ? !r : r;
		}
	}
	
	public static class ContainsRule implements Rule {
		private boolean isNegative;
		private String chars;
		public ContainsRule(String chars, boolean isNegative) {
			this.chars = chars;
			this.isNegative = isNegative;
		}
		public boolean matches(Downloader.Request request) {
			boolean r = request.getUrl().contains(chars);
			return isNegative ? !r : r;
		}
	}
	
	public static class Model {
		
		public Parser.ParsedResult parse() {
			if (this.parser == null) {
				throw new RuntimeException("请为Model设置解析器，比如model.addParser");
			}
			
			// 首先执行模型解析器，作为字段解析器的基础
			final ParsedResult modelParsedResult = this.parser.parse();
			// 若模型解析结果为空，停止解析
			if (modelParsedResult == null || K.isEmpty(modelParsedResult.all())) {
				return null;
			}
			// 处理字段解析器
			final List<Target.Model.Field> fields = this.getFields();
			// 若没有配置任何字段解析器，只需要报告模型解析器结果即可
			if (K.isEmpty(fields)) {
				return modelParsedResult;
			}
			// 默认用模型解析器的结果值作为最终解析结果
			ParsedResult finalParsedResult = modelParsedResult;
			final List<String[]> urlsForNewTask = new ArrayList<String[]>();
			// 否则需要逐个模型，逐个字段的去解析
			final List<Parser.Model> parsedModelsWithFields = new ArrayList<Parser.Model>();
			modelParsedResult.all().forEach(item -> {
				Parser.Model modelWithFields = new Parser.Model();
				fields.forEach(field -> {
					// 执行字段解析器
					ParsedResult fieldParsedResult = new ParsedResult(item);
					for (FieldParser fieldParser : field.getParsers()) {
						fieldParser.setIsSerialize(field.isSerialize());
						fieldParser.setModelParser(parser);
						fieldParser.setPrevParserResult(fieldParsedResult);
						fieldParsedResult = fieldParser.parse();
					}
					// 若字段解析结果为空，不做后面处理
					if ( K.isEmpty(fieldParsedResult.all())) {
						return;
					}
					// 将字段解析结果存入模型对象中
					modelWithFields.put(field.getName(), fieldParsedResult.all().toArray(new Object[]{}));
					if (field.isForNewTask()) {
						final String httpMethod = field.getProperties().getString("httpMethod", K.HTTP_GET);
						fieldParsedResult.all().forEach(val -> {
							final String newUrl = (String)val;
							urlsForNewTask.add(new String[]{httpMethod, newUrl});
						});
					}
				});
				// 将单个解析结果存入列表中
				parsedModelsWithFields.add(modelWithFields);
			});
			// 若字段解析结果不为空，将其最为最终解析结果返回
			if (K.isNotEmpty(parsedModelsWithFields)) {
				finalParsedResult = ParsedResult.fromList(parsedModelsWithFields);
			}
			finalParsedResult.getUrlsForNewTask().addAll(urlsForNewTask);
			return finalParsedResult;
		}
		private ModelParser parser;
		private List<Field> fields;
		
		public Model() {
			this.fields = new ArrayList<Field>();
		}
		public Model addParser(ModelParser parser) {
			this.parser = parser;
			return this;
		}
		public ModelParser getParser() {
			return this.parser;
		}
		public List<Field> getFields() {
			return this.fields;
		}
		
		public Field addField(String name) {
			Field field = new Field(name);
			this.fields.add(field);
			return field;
		}
		
		public void setResponse(Downloader.Response response) {
			this.parser.setResponse(response);
		}
		
		public static class Field {
			private String name;
			private boolean isArray;
			private boolean isForNewTask;
			private boolean isSerialize;
			private Properties properties;
			private List<FieldParser> parsers;
			public Field(String name) {
				this.name = name;
				this.parsers = new ArrayList<FieldParser>();
				this.properties = new Properties();
			}
			public boolean isArray() {
				return this.isArray;
			}
			
			public boolean isForNewTask() {
				return this.isForNewTask;
			}
			
			public boolean isSerialize() {
				return this.isSerialize;
			}
			
			public Field setIsArray(boolean isArray){
				this.isArray = isArray;
				return this;
			}
			public Field asNewTask(){
				return setIsForNewTask(true);
			}
			public Field setIsForNewTask(boolean isForNewTask) {
				this.isForNewTask = isForNewTask;
				return this;
			}
			public Field serialize() {
				return this.setIsSerialize(true);
			}
			public Field setIsSerialize(boolean isSerialize) {
				this.isSerialize = isSerialize;
				return this;
			}
			public Field addProperty(String name) {
				this.properties.put(name, null);
				return this;
			}
			
			public Field addProperty(String name, Object value) {
				this.properties.put(name, value);
				return this;
			}
			
			public Properties getProperties() {
				return this.properties;
			}
			
			public Field addParser(FieldParser parser) {
				this.parsers.add(parser);
				return this;
			}
			
			public String getName() {
				return name;
			}
			public List<FieldParser> getParsers() {
				return this.parsers;
			}

		}
	}
	

	public Model getModel() {
		return model;
	}

	public String getName() {
		return name;
	}

	public Rules getRules() {
		return this.rules;
	}
	
}
