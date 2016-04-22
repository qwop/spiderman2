package net.kernal.spiderman.worker.extract.schema.rule;

import net.kernal.spiderman.worker.download.Downloader;

public class StartsWithRule extends UrlMatchRule {
	
	private String prefix;

	public StartsWithRule(String prefix) {
		this.prefix = prefix;
	}

	protected boolean doMatches(Downloader.Request request) {
		return request.getUrl().startsWith(prefix);
	}
	
}