package net.kernal.spiderman.conf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import net.kernal.spiderman.Properties;
import net.kernal.spiderman.Seed;
import net.kernal.spiderman.worker.extract.Extractor;
import net.kernal.spiderman.worker.extract.conf.Field;
import net.kernal.spiderman.worker.extract.conf.Page;

public class Conf {
	
	private static final Logger logger = Logger.getLogger(Conf.class.getName());
	
	public Conf() {
		seeds = new Seeds();
		extractors = new Extractors();
		filters = new Filters();
		pages = new Pages();
		params = new Properties();
	}
	
	private Seeds seeds;
	private Extractors extractors;
	private Filters filters;
	private Pages pages;
	private Properties params;
	
	public static class Seeds {
		private List<Seed> seeds;
		public Seeds() {
			this.seeds = new ArrayList<Seed>();
		}
		public Seeds add(Seed seed) {
			this.seeds.add(seed);
			logger.info("添加种子: " + seed);
			return this;
		}
		public List<Seed> all() {
			return this.seeds;
		}
	}
	public static class Extractors {
		private Map<String, Class<Extractor>> extractors;
		public Extractors() {
			extractors = new HashMap<String, Class<Extractor>>();
		}
		public Extractors register(String alias, Class<Extractor> extractor) {
			this.extractors.put(alias, extractor);
			logger.info("注册解析器类: " + alias + ", " + extractor.getName());
			return this;
		}
		public Map<String, Class<Extractor>> all() {
			return this.extractors;
		}
	}
	public static class Filters {
		private Map<String, Field.ValueFilter> filters;
		public Filters() {
			filters = new HashMap<String, Field.ValueFilter>();
		}
		public Filters register(String alias, Field.ValueFilter ft) {
			this.filters.put(alias, ft);
			logger.info("注册过滤器: " + alias + ", " + ft);
			return this;
		}
		public Map<String, Field.ValueFilter> all() {
			return this.filters;
		}
	}
	public static class Pages {
		private List<Page> pages;
		public Pages() {
			this.pages = new ArrayList<Page>();
		}
		public Pages add(Page page) {
			this.pages.add(page);
			logger.info("添加页面配置: " + page);
			return this;
		}
		public List<Page> all() {
			return this.pages;
		}
	}
	
	public static interface Builder {
		public Conf build() throws Exception;
	}
	public Conf addSeed(String url) {
		return this.addSeed(new Seed(url));
	}
	public Conf addSeed(String name, String url) {
		return this.addSeed(new Seed(name, url));
	}
	public Conf addSeed(Seed seed) {
		seeds.add(seed);
		return this;
	}
	public Conf registerExtractor(String alias, Class<Extractor> extractor) {
		extractors.register(alias, extractor);
		return this;
	}
	public Conf registerFilter(String alias, Field.ValueFilter filter) {
		filters.register(alias, filter);
		return this;
	}
	public Conf addPage(Page page) {
		pages.add(page);
		return this;
	}
	public Conf set(String paramName, Object value) {
		this.params.put(paramName, value);
		return this;
	}
	public Seeds getSeeds() {
		return seeds;
	}
	public Extractors getExtractors() {
		return extractors;
	}
	public Filters getFilters() {
		return filters;
	}
	public Pages getPages() {
		return pages;
	}
	public Properties getParams() {
		return params;
	}
}
