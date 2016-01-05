package net.kernal.spiderman.task;

import net.kernal.spiderman.conf.Seed;
import net.kernal.spiderman.downloader.Downloader.Request;
import net.kernal.spiderman.parser.Parser.ParsedResult;

public class ResultTask extends Task {

	private static final long serialVersionUID = -2038408835292733528L;
	
	private String target;
	private ParsedResult parsedResult;

	public ResultTask(String target, Seed seed, ParsedResult parsedResult, Request request) {
		super(seed, request, 0);
		this.target = target;
		this.parsedResult = parsedResult;
	}
	
	public ParsedResult getParsedResult() {
		return this.parsedResult;
	}

	public String getType() {
		return "reporting";
	}

	public String getTarget() {
		return this.target;
	}
	
}
