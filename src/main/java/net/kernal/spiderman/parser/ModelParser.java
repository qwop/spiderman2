package net.kernal.spiderman.parser;

import net.kernal.spiderman.Downloader;

public abstract class ModelParser implements Parser {

	protected Downloader.Response response;
	public ModelParser(Downloader.Response response) {
		this.response = response;
	}
	public void setResponse(Downloader.Response response) {
		this.response = response;
	}
	
}
