package net.kernal.spiderman.worker.extract.schema.rule;

import net.kernal.spiderman.worker.download.Downloader;

public class ContainsRule extends UrlMatchRule {
	
	private String chars;

	public ContainsRule(String chars) {
		this.chars = chars;
	}

	public boolean doMatches(Downloader.Request request) {
		return request.getUrl().contains(chars);
	}
	
}