package net.kernal.spiderman.reporting;

import net.kernal.spiderman.Spiderman;
import net.kernal.spiderman.Task;
import net.kernal.spiderman.downloader.Downloader;
import net.kernal.spiderman.parser.Parser;

/**
 * 状态报告(类似监听器)
 * @author 赖伟威 l.weiwei@163.com 2015-12-10
 *
 */
public interface Reporting {

	public void reportStart();
	
	public void reportDownload(final Downloader.Response response);
	
	public void reportNewTask(final Task newTask);
	
	public void reportParsedResult(final Parser.ParsedResult parsedResult);
	
	public void reportStop(Spiderman.Counter counter, int poolSize, int activeCount, long completedTaskCount);
	
}
