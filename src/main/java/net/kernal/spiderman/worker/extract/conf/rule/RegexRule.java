package net.kernal.spiderman.worker.extract.conf.rule;

import net.kernal.spiderman.worker.download.Downloader;

public class RegexRule extends UrlMatchRule {
	
	private String regex;

	public RegexRule(String regex) {
		this.regex = regex;
	}

	protected boolean doMatches(Downloader.Request request) {
		return request.getUrl().matches(this.regex);
	}
	
}