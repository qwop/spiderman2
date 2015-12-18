package net.kernal.spiderman;

import net.kernal.spiderman.Parser.ParsedResult;

public interface Reporting {

	public void reportStart();
	
	public void reportDownload(final Downloader.Response response);
	
	public void reportNewTask(final Task newTask);
	
	public void reportParsedResult(final ParsedResult parsedResult);
	
	public void reportStop();
	
}
