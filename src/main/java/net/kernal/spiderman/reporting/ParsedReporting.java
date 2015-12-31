package net.kernal.spiderman.reporting;

import net.kernal.spiderman.Spiderman.Counter;
import net.kernal.spiderman.downloader.Downloader.Response;
import net.kernal.spiderman.parser.Parser.ParsedResult;
import net.kernal.spiderman.task.Task;

public abstract class ParsedReporting implements Reporting {

	public void reportStart() {
	}

	public void reportDownload(Response response) {
	}

	public void reportNewTask(Task newTask) {
	}

	public abstract void reportParsedResult(ParsedResult parsedResult);

	public void reportStop(Counter counter) {
	}

}
