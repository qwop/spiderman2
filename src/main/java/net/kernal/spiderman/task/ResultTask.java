package net.kernal.spiderman.task;

import net.kernal.spiderman.downloader.Downloader.Request;
import net.kernal.spiderman.parser.Parser.ParsedResult;

public class ResultTask extends Task {

	private static final long serialVersionUID = -2038408835292733528L;
	
	private ParsedResult parsedResult;

	public ResultTask(ParsedResult parsedResult, Request request) {
		super(request, 0);
		this.parsedResult = parsedResult;
	}
	
	public ParsedResult getParsedResult() {
		return this.parsedResult;
	}

	public String getType() {
		return "reporting";
	}

}
