package net.kernal.spiderman.worker.extract.conf;

import java.util.ArrayList;
import java.util.List;

import org.zbus.kit.log.Logger;

import net.kernal.spiderman.worker.download.Downloader;
import net.kernal.spiderman.worker.extract.Extractor;
import net.kernal.spiderman.worker.extract.conf.rule.ContainsRule;
import net.kernal.spiderman.worker.extract.conf.rule.EndsWithRule;
import net.kernal.spiderman.worker.extract.conf.rule.RegexRule;
import net.kernal.spiderman.worker.extract.conf.rule.StartsWithRule;
import net.kernal.spiderman.worker.extract.conf.rule.UrlMatchRule;

public abstract class Page {

	private static final Logger logger = Logger.getLogger(Page.class.getName());
	
	private String name;
	private Extractor.Builder extractorBuilder;
	private UrlMatchRules rules;
	private Models models;
	private boolean isTaskDuplicateCheckEnabled;
	private Field.ValueFilter filter;//全局Filter，所有Model的所有Field都需要执行
	
	public Page(String name) {
		this.name = name;
		this.rules = new UrlMatchRules();
		this.models = new Models(this.name);
	}
	
	public Page setFilter(Field.ValueFilter filter) {
		this.filter = filter;
		return this;
	}
	public Field.ValueFilter getFilter() {
		return this.filter;
	}
	
	public Page setTaskDuplicateCheckEnabled(boolean bool) {
		this.isTaskDuplicateCheckEnabled = bool;
		return this;
	}
	
	public boolean isTaskDuplicateCheckEnabled() {
		return this.isTaskDuplicateCheckEnabled;
	}
	
	public Page setExtractorBuilder(Extractor.Builder builder) {
		this.extractorBuilder = builder;
		return this;
	}
	
	public boolean matches(Downloader.Request request) {
		if ("or".equalsIgnoreCase(rules.getPolicy())) {
			for (UrlMatchRule r : rules.all()) {
				if (r.matches(request)) {
					return true;
				}
			}
		} else {
			boolean matched = false;
			for (UrlMatchRule r : rules.all()) {
				matched = r.matches(request);
			}
			return matched;
		}
		
		return false;
	}
	
	public String getName() {
		return this.name;
	}
	public UrlMatchRules getRules() {
		return this.rules;
	}
	public Models getModels() {
		return this.models;
	}
	
	public Extractor.Builder getExtractorBuilder() {
		return this.extractorBuilder;
	}
	
	public abstract void config(UrlMatchRules rules, Models models);
	
	public static class Builder {
		private Page page;
		public Builder() {
			this.page = new Page("") {
				public void config(UrlMatchRules rules, Models models) {}
			};
		}
		
		public Builder setName(String name) {
			this.page.name = name;
			return this;
		}
		
		public Builder setExtractorBuilder(Extractor.Builder builder) {
			this.page.extractorBuilder = builder;
			return this;
		}
		
		public Builder addRule(UrlMatchRule rule) {
			this.page.rules.add(rule);
			return this;
		}
		
		public Page build() {
			return page;
		}
	}
	
	public static class UrlMatchRules {
		
		private String policy;
		private List<UrlMatchRule> rules;
		
		public UrlMatchRules() {
			this.rules = new ArrayList<UrlMatchRule>();
		}
		
		public void addNegativeRegexRule(String regex) {
			this.rules.add(new RegexRule(regex).setNegativeEnabled(true));
		}
		public void addNegativeStartsWithRule(String prefix) {
			this.rules.add(new StartsWithRule(prefix).setNegativeEnabled(true));
		}
		public void addNegativeEndsWithRule(String suffix) {
			this.rules.add(new EndsWithRule(suffix).setNegativeEnabled(true));
		}
		public void addNegativeContainsRule(String chars) {
			this.rules.add(new ContainsRule(chars).setNegativeEnabled(true));
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
		public void add(UrlMatchRule rule) {
			this.rules.add(rule);
		}
		public List<UrlMatchRule> all() {
			return this.rules;
		}
		
		public String getPolicy() {
			return policy;
		}
		public void setPolicy(String policy) {
			this.policy = policy;
		}

		public String toString() {
			return "UrlMatchRules [policy=" + policy + ", rules=" + rules + "]";
		}
		
	}
	
	public static class Models {
		private String page;
		private List<Model> models;
		public Models(String page) {
			this.page = page;
			this.models = new ArrayList<Model>();
		}
		public Model addModel(String name) {
			Model model = new Model(page, name);
			this.models.add(model);
			logger.info("添加Model配置: " + page);
			return model;
		}
		public List<Model> all() {
			return this.models;
		}
	}

	public String toString() {
		return "Page [name=" + name + "]";
	}

}