package net.kernal.spiderman;

import net.kernal.spiderman.parser.Parser;

public interface Reporting {

	public void reportStart();
	
	public void reportDownload(final Downloader.Response response);
	
	public void reportNewTask(final Task newTask);
	
	public void reportParsedResult(final Parser.ParsedResult parsedResult);
	
	public void reportStop(Spiderman.Counter counter, int poolSize, int activeCount, long completedTaskCount);
	
}
