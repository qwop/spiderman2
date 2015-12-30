package net.kernal.spiderman.parser;

import javax.script.ScriptEngine;

import net.kernal.spiderman.downloader.Downloader;

public abstract class ModelParser implements Parser {

	protected ScriptEngine scriptEngine;
	protected Parser.ParsedResult prevParsedResult;
	protected ModelParser prevParser;
	protected Downloader.Response response;
	public ModelParser(Downloader.Response response) {
		this.response = response;
	}
	public ModelParser(Downloader.Response response, ModelParser prevParser) {
		this.response = response;
		this.prevParser = prevParser;
	}
	public ModelParser setResponse(Downloader.Response response) {
		this.response = response;
		return this;
	}
	public ModelParser setPrevParser(ModelParser prevParser) {
		this.prevParser = prevParser;
		return this;
	}
	public ModelParser setPrevParsedResult(Parser.ParsedResult prevParsedResult) {
		this.prevParsedResult = prevParsedResult;
		return this;
	}
	public Downloader.Response getResponse() {
		return this.response;
	}
	public void setScriptEngine(ScriptEngine scriptEngine) {
		this.scriptEngine = scriptEngine;
	}
}
