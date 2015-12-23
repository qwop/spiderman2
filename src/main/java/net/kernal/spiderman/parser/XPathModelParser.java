package net.kernal.spiderman.parser;

import net.kernal.spiderman.Downloader;

public abstract class XPathModelParser extends ModelParser {

	protected String xpath;
	public XPathModelParser(Downloader.Response response, String xpath) {
		super(response);
		this.xpath = xpath;
	}
	
}
