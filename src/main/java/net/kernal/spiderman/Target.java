package net.kernal.spiderman;

import java.util.ArrayList;
import java.util.List;

public abstract class Target {

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
		public void addRegexRule(String regex) {
			this.rules.add(new RegexRule(regex));
		}
		public void addStartsWithRule(String prefix) {
			this.rules.add(new StartsWithRule(prefix));
		}
		public void addEndsWithRule(String suffix) {
			this.rules.add(new EndsWithRule(suffix));
		}
		public void addContainsRule(String chars) {
			this.rules.add(new ContainsRule(chars));
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
		private String regex;
		public RegexRule(String regex) {
			this.regex = regex;
		}
		public boolean matches(Downloader.Request request) {
			return request.getUrl().matches(this.regex);
		}
	}
	
	public static class StartsWithRule implements Rule {
		private String prefix;
		public StartsWithRule(String prefix) {
			this.prefix = prefix;
		}
		public boolean matches(Downloader.Request request) {
			return request.getUrl().startsWith(prefix);
		}
	}
	
	public static class EndsWithRule implements Rule {
		private String suffix;
		public EndsWithRule(String suffix) {
			this.suffix = suffix;
		}
		public boolean matches(Downloader.Request request) {
			return request.getUrl().endsWith(suffix);
		}
	}
	
	public static class ContainsRule implements Rule {
		private String chars;
		public ContainsRule(String chars) {
			this.chars = chars;
		}
		public boolean matches(Downloader.Request request) {
			return request.getUrl().contains(chars);
		}
	}
	
	public static class Model {
		private boolean isArray;
		private Parser parser;
		private List<Field> fields;
		
		public Model() {
			this.fields = new ArrayList<Field>();
		}
		public Model addParser(Parser parser) {
			this.parser = parser;
			return this;
		}
		public Parser getParser() {
			return this.parser;
		}
		public List<Field> getFields() {
			return this.fields;
		}
		
		public Model(boolean isArray) {
			this.isArray = isArray;
			this.fields = new ArrayList<Field>();
		}
		
		public Field addField(String name) {
			Field field = new Field(name);
			this.fields.add(field);
			return field;
		}
		
//		public String getXpath() {
//			return xpath;
//		}
//		public boolean isTextExtractEnabled() {
//			return this.textExtractEnabled;
//		}
//		public Model xpath(String xpath) {
//			this.xpath = xpath;
//			return this;
//		}
//		public Model setTextExtractEnabled(boolean b) {
//			textExtractEnabled = b;
//			return this;
//		}
		public boolean isArray() {
			return isArray;
		}
		public Model setIsArray(boolean isArray) {
			this.isArray = isArray;
			return this;
		}

		public static class Field {
			private String name;
			private boolean isArray;
			private boolean isForNewTask;
			private Properties properties;
			private Parser firstParser;
			private Parser tailParser;
			public Field(String name) {
				this.name = name;
				this.firstParser = new AbstractParser() { public void parse() {} };
				this.tailParser = this.firstParser;
				this.properties = new Properties();
			}
			public boolean isArray() {
				return this.isArray;
			}
			
			public boolean isForNewTask() {
				return this.isForNewTask;
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
			
//			public Field xpath(String xpath) {
//				this.parsers.add(new XpathParser(xpath, null));
//				return this;
//			}
//			public Field xpath(String xpath, String attr) {
//				this.parsers.add(new XpathParser(xpath, attr));
//				return this;
//			}
//			public Field selector(String selector) {
//				this.parsers.add(new SelectorParser(selector));
//				return this;
//			}
//			public Field regex(String regex) {
//				this.parsers.add(new RegexParser(regex));
//				return this;
//			}
			public Field addParser(Parser parser) {
				this.tailParser.setNextParser(parser);
				this.tailParser = parser;
				return this;
			}
			
			public String getName() {
				return name;
			}
			public Parser getParser() {
				return firstParser;
			}

//			public static interface Parser {
//				
//				public void parse(ParserContext context) throws Exception;
//				
//				public static class ParserContext extends Properties {
//					private static final long serialVersionUID = 1L;
//					
//					public ParserContext(Target target, Downloader.Response response) {
//						this.target = target;
//						this.response = response;
//					}
//					private Downloader.Response response;
//					private Target target;
//					private Object parsed;
//					public void setParsed(Object parsed) {
//						this.parsed = parsed;
//					}
//					public Object getParsed(){
//						return this.parsed;
//					}
//					public Target getTarget() {
//						return this.target;
//					}
//					public Downloader.Response getResponse() {
//						return this.response;
//					}
//				}
//				
//			}
//			
//			public static class XpathParser implements Parser {
//				private String xpath;
//				private String attr;
//				public XpathParser(String xpath, String attr){
//					this.xpath = xpath;
//					this.attr = attr;
//				}
//				public String getXpath() {
//					return this.xpath;
//				}
//				public String getAttr() {
//					return this.attr;
//				}
//				@Deprecated
//				public void parse(ParserContext context) {}
//			}
//			public static class SelectorParser implements Parser {
//				private String selector;
//				public SelectorParser(String selector) {
//					this.selector = selector;
//				}
//				public String getSelector(){
//					return this.selector;
//				}
//				@Deprecated
//				public void parse(ParserContext context) throws Exception {}
//				
//			}
//			public static class RegexParser implements Parser {
//				private String regex;
//				public RegexParser(String regex) {
//					this.regex = regex;
//				}
//				public String getRegex() {
//					return this.regex;
//				}
//				@Deprecated
//				public void parse(ParserContext context) {}
//			}
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
