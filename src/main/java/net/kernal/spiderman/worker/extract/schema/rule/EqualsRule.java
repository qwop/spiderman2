package net.kernal.spiderman.worker.extract.schema.rule;

import net.kernal.spiderman.worker.download.Downloader.Request;

public class EqualsRule extends UrlMatchRule {

	private String url;
	
	public EqualsRule(String url) {
		this.url = url;
	}
	
	protected boolean doMatches(Request request) {
		return url.equals(request.getUrl());
	}

}
