package net.kernal.spiderman.worker.extract.schema.rule;

import net.kernal.spiderman.worker.download.Downloader;

public class EndsWithRule extends UrlMatchRule {
	
	private String suffix;

	public EndsWithRule(String suffix) {
		this.suffix = suffix;
	}

	protected boolean doMatches(Downloader.Request request) {
		return request.getUrl().endsWith(suffix);
	}
	
}