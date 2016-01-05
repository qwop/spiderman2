package net.kernal.spiderman.reporting;

import net.kernal.spiderman.Counter;
import net.kernal.spiderman.downloader.Downloader;
import net.kernal.spiderman.downloader.Downloader.Request;
import net.kernal.spiderman.downloader.Downloader.Response;
import net.kernal.spiderman.parser.Parser.ParsedResult;
import net.kernal.spiderman.task.Task;

public abstract class ParsedReporting implements Reporting {

	public void reportStart() {
	}
	
	public void reportDuplicateCheck(final String key, final boolean checkResult, final Downloader.Request request){
	}

	public void reportDownload(Response response) {
	}

	public void reportNewTask(Task newTask) {
	}

	public abstract void reportParsedResult(Task task, ParsedResult parsedResult);

	public void reportStop(Counter counter) {
	}
	
	public void reportDuplicate(String key, Request req) {
	}
}
