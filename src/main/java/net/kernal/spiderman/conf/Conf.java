package net.kernal.spiderman.conf;

import net.kernal.spiderman.Properties;
import net.kernal.spiderman.downloader.Downloader;
import net.kernal.spiderman.parser.TransformParser;
import net.kernal.spiderman.reporting.Reporting;
import net.kernal.spiderman.reporting.Reportings;

public class Conf {
	
	public Conf() {
		seeds = new Seeds();
		targets = new Targets();
		properties = new Properties();
		reportings = new Reportings();
		functions = new Functions();
	}
	
	private Seeds seeds;
	private Targets targets;
	private Properties properties;
	private Reportings reportings;
	private Functions functions;
	
	public static interface Builder {
		public Conf build() throws Exception;
	}
	
	public Conf addSeed(String url) {
		seeds.add(new Downloader.Request(url));
		return this;
	}
	public Conf addSeed(String url, String httpMethod) {
		seeds.add(new Downloader.Request(url, httpMethod));
		return this;
	}
	public Conf addSeed(Downloader.Request request) {
		seeds.add(request);
		return this;
	}
	public Conf addTarget(Target target) {
		targets.add(target);
		return this;
	}
	public Conf set(String property, Object value) {
		this.properties.put(property, value);
		return this;
	}
	public Conf registerFunction(String functionName, TransformParser function) {
		this.functions.register(functionName, function);
		return this;
	}
	public Conf addReporting(Reporting reporting) {
		this.reportings.add(reporting);
		return this;
	}
	public Seeds getSeeds() {
		return seeds;
	}
	public Targets getTargets() {
		return targets;
	}
	public Properties getProperties() {
		return properties;
	}
	public Reportings getReportings() {
		return reportings;
	}
	public Functions getFunctions() {
		return this.functions;
	}
	
}
