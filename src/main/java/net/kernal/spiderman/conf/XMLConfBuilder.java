package net.kernal.spiderman.conf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import net.kernal.spiderman.K;
import net.kernal.spiderman.Properties;
import net.kernal.spiderman.parser.HtmlCleanerParser;
import net.kernal.spiderman.parser.ModelParser;
import net.kernal.spiderman.parser.Parser;
import net.kernal.spiderman.parser.ScriptTransformParser;
import net.kernal.spiderman.parser.TransformParser;
import net.kernal.spiderman.parser.XMLParser;
import net.kernal.spiderman.reporting.Reporting;

/**
 * 此类解析规则比较复杂，建议暂时别太深入去看 :)
 * @author 赖伟威 l.weiwei@163.com 2015-12-28
 *
 */
public class XMLConfBuilder extends DefaultConfBuilder {

	private FileInputStream is;
	public XMLConfBuilder(File file) {
		super();
		try {
			this.is = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
	public XMLConfBuilder registerFunction(String funcName, TransformParser function) {
		this.conf.registerFunction(funcName, function);
		return this;
	}
	
	public XMLConfBuilder addReporting(Reporting reporting) {
		this.conf.addReporting(reporting);
		return this;
	}
	
	public XMLConfBuilder addSeed(String url) {
		this.conf.getSeeds().add(url);
		return this;
	}
	
	public XMLConfBuilder addTarget(Target target) {
		target.configRules(target.getRules());
		target.configModel(target.getModel());
		this.conf.getTargets().add(target);
		return this;
	}
	
	public XMLConfBuilder set(String key, Object value){
		this.conf.getProperties().put(key, value);
		return this;
	}
	
	public Conf build() {
		super.build();
		
		// TODO 搞个Factory，Builder之类的
		@SuppressWarnings("unchecked")
		final Class<? extends Parser> provider = (Class<? extends Parser>) conf.getProperties().getClass("parser.provider", HtmlCleanerParser.class);
				
		Target.Model rootModel = new Target.Model();
		ModelParser rootParser = new XMLParser("//spiderman", this.is);
		rootModel.addParser(rootParser);
		Parser.ParsedResult rootPr = new Parser.ParsedResult(rootModel.parse().first());
		
		final List<File> scriptFiles = new ArrayList<File>();
		Target.Model sc = new Target.Model();
		sc.addParser(new XMLParser(".//script", rootParser).setPrevParsedResult(rootPr));
		sc.parse().all().forEach((s) -> {
			Parser.ParsedResult pr = new Parser.ParsedResult(s);
			Target.Model m = new Target.Model();
			m.addParser(new XMLParser(rootParser).setPrevParsedResult(pr));
			m.addField("src").addParser(new XMLParser.FieldPaser(".", "src"));
			Properties script = (Properties)m.parse().first();
			String scriptSrc = script.getString("src");
			//System.out.println("scriptSrc: "+scriptSrc);
			scriptFiles.add(new File(scriptSrc));
		});
		
		Target.Model m0 = new Target.Model();
		m0.addParser(new XMLParser(".//seed", rootParser).setPrevParsedResult(rootPr));
		Parser.ParsedResult seeds = m0.parse();
		if (seeds != null && K.isNotEmpty(seeds.all())) {
			seeds.all().forEach((sd) -> {
				Parser.ParsedResult pr = new Parser.ParsedResult(sd);
				Target.Model m = new Target.Model();
				m.addParser(new XMLParser(rootParser).setPrevParsedResult(pr));
				m.addField("url").addParser(new XMLParser.FieldPaser(".", "url"));
				m.addField("text").addParser(new XMLParser.FieldPaser("./text()"));
				Properties seed = (Properties)m.parse().first();
				String seedUrl = seed.getString("url");
				if (K.isBlank(seedUrl)) {
					seedUrl = seed.getString("text");
				}
				//System.out.println("seed: "+seedUrl);
				conf.addSeed(K.urlEncode(seedUrl));
			});
		}
		
		Target.Model m1 = new Target.Model();
		m1.addParser(new XMLParser(".//target", rootParser).setPrevParsedResult(rootPr));
		Parser.ParsedResult targets = m1.parse();
		for (Object tgt : targets.all()) {
			Parser.ParsedResult pr1 = new Parser.ParsedResult(tgt);
			Target.Model m2 = new Target.Model();
			m2.addParser(new XMLParser(rootParser).setPrevParsedResult(pr1));
			m2.addField("name").addParser(new XMLParser.FieldPaser(".", "name"));
			m2.addField("parser").addParser(new XMLParser.FieldPaser(".", "parser"));
			Properties targetCfg = (Properties)m2.parse().first();
			String targetName = targetCfg.getString("name");
			if (K.isBlank(targetName)) {
				throw new RuntimeException("Target.name required");
			}
			
			//System.out.println("taretName: "+targetName);
			Target target = null;
			for (Target tg : conf.getTargets().all()) {
				if (targetName.equals(tg.getName())) {
					target = tg;
				}
			};
			if (target == null) {
				target = new Target(targetName){
					public void configRules(Rules rules) {}
					public void configModel(Model model) {}
				};
				conf.getTargets().add(target);
			}
			
			Target.Model m3 = new Target.Model();
			m3.addParser(new XMLParser(".//rule", rootParser).setPrevParsedResult(pr1));
			m3.addField("type").addParser(new XMLParser.FieldPaser(".", "type"));
			m3.addField("value").addParser(new XMLParser.FieldPaser("./text()"));
			Parser.ParsedResult rules = m3.parse();
			for (Object r : rules.all()) {
				Properties rule = (Properties)r;
				String type = rule.getString("type");
				String value = rule.getString("value");
				//System.out.println("\t"+type+","+value);
				if ("regex".equalsIgnoreCase(type)) {
					target.getRules().addRegexRule(value);
				} else if ("!regex".equalsIgnoreCase(type)) {
					target.getRules().addNotRegexRule(value);
				} else if ("startsWith".equalsIgnoreCase(type)) {
					target.getRules().addStartsWithRule(value);
				} else if ("!startsWith".equalsIgnoreCase(type)) {
					target.getRules().addNotStartsWithRule(value);
				} else if ("endsWith".equalsIgnoreCase(type)) {
					target.getRules().addEndsWithRule(value);
				} else if ("!endsWith".equalsIgnoreCase(type)) {
					target.getRules().addNotEndsWithRule(value);
				} else if ("contains".equalsIgnoreCase(type)) {
					target.getRules().addContainsRule(value);
				} else if ("!contains".equalsIgnoreCase(type)) {
					target.getRules().addNotContainsRule(value);
				}
			}
			
			Target.Model m4 = new Target.Model();
			XMLParser p4 = new XMLParser(".//model", rootParser);
			m4.addParser(p4.setPrevParsedResult(pr1));
			
			m4.addField("xpath").addParser(new XMLParser.FieldPaser(".", "xpath"));
			Parser.ParsedResult pr2 = m4.parse();
			Properties model = (Properties)pr2.first();
			
			String modelXpath = model.getString("xpath");
			//System.out.println("\t"+modelXpath);
			if (HtmlCleanerParser.class.isAssignableFrom(provider)) {
				target.getModel().addParser(new HtmlCleanerParser(modelXpath));
			} else if (XMLParser.class.isAssignableFrom(provider)) {
				target.getModel().addParser(new XMLParser(modelXpath));
			}
			
			Target.Model m5 = new Target.Model();
			XMLParser p5 = new XMLParser(".//field", rootParser);
			m5.addParser(p5.setPrevParsedResult(new Parser.ParsedResult(p4.getInputSource())));
			Parser.ParsedResult fields = m5.parse();
			for (Object f : fields.all()) {
				Parser.ParsedResult pr3 = new Parser.ParsedResult(f);
				
				Target.Model m6 = new Target.Model();
				m6.addParser(new XMLParser(rootParser).setPrevParsedResult(pr3));
				m6.addField("name").addParser(new XMLParser.FieldPaser(".", "name"));
				m6.addField("isForNewTask").addParser(new XMLParser.FieldPaser(".", "isForNewTask"));
				Properties field = (Properties)m6.parse().first();
				
				String fieldName = field.getString("name");
				int isForNewTask = field.getInt("isForNewTask");
				//System.out.println("\t\t"+fieldName+","+isForNewTask);
				
				Target.Model.Field mField = target.getModel().addField(fieldName).setIsForNewTask(isForNewTask == 1);
				
				Target.Model m7 = new Target.Model();
				XMLParser p7 = new XMLParser(".//parser", rootParser);
				m7.addParser(p7.setPrevParsedResult(pr3));
				Parser.ParsedResult parsers = m7.parse();
				for (Object p : parsers.all()) {
					Parser.ParsedResult pr4 = new Parser.ParsedResult(p);
					Target.Model m8 = new Target.Model();
					m8.addParser(new XMLParser(rootParser).setPrevParsedResult(pr4));
					m8.addField("xpath").addParser(new XMLParser.FieldPaser(".", "xpath"));
					m8.addField("attribute").addParser(new XMLParser.FieldPaser(".", "attribute"));
					m8.addField("script").addParser(new XMLParser.FieldPaser(".", "script"));
					
					Properties parser = (Properties)m8.parse().first();
					String xpath = parser.getString("xpath");
					String attr = parser.getString("attribute");
					final String regex = parser.getString("regex");
					String script = parser.getString("script");
					
					//System.out.println("\t\t\t"+xpath+","+attr+","+regex+","+script);
					
					if (K.isNotBlank(xpath)) {
						if (HtmlCleanerParser.class.isAssignableFrom(provider)) {
							mField.addParser(new HtmlCleanerParser.FieldPaser(xpath, attr));
						} else if (XMLParser.class.isAssignableFrom(provider)) {
							mField.addParser(new XMLParser.FieldPaser(xpath, attr));
						}
					}
					if (K.isNotBlank(regex)) {
						mField.addParser(new TransformParser() {
							public Object transform(Object oldValue) {
								return K.findOneByRegex((String)oldValue, regex);
							}
						});
					}
					if (K.isNotBlank(script)) {
						mField.addParser(new ScriptTransformParser(scriptFiles, script));
					}
				}
			}
			
		}
		
		return conf;
	}

	public void registerFunction(Functions functions){}
	public void addProperty(Properties properties) {}
	public void addSeed(Seeds seeds) {}
	public void addTarget(Targets targets) {}
	
	public static void main(String[] args) {
		new XMLConfBuilder(new File("src/main/resources/baidu-search.xml")).build();
	}

}
