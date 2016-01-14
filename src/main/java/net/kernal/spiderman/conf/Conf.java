package net.kernal.spiderman.conf;

import java.util.ArrayList;
import java.util.List;

import net.kernal.spiderman.Properties;
import net.kernal.spiderman.Seed;
import net.kernal.spiderman.worker.extract.conf.Page;

public class Conf {
	
	public Conf() {
		seeds = new Seeds();
		pages = new Pages();
		params = new Properties();
	}
	
	private Seeds seeds;
	private Pages pages;
	private Properties params;
	
	public static class Seeds {
		private List<Seed> seeds;
		public Seeds() {
			this.seeds = new ArrayList<Seed>();
		}
		public Seeds add(Seed seed) {
			this.seeds.add(seed);
			return this;
		}
		public List<Seed> all() {
			return this.seeds;
		}
	}
	public static class Pages {
		private List<Page> pages;
		public Pages() {
			this.pages = new ArrayList<Page>();
		}
		public Pages add(Page page) {
			this.pages.add(page);
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
	public Pages getPages() {
		return pages;
	}
	public Properties getParams() {
		return params;
	}
}
