package net.kernal.spiderman.worker.extract;

import java.util.List;

import javax.script.ScriptEngine;

import net.kernal.spiderman.Counter;
import net.kernal.spiderman.K;
import net.kernal.spiderman.Spiderman;
import net.kernal.spiderman.logger.Logger;
import net.kernal.spiderman.queue.QueueManager;
import net.kernal.spiderman.worker.Task;
import net.kernal.spiderman.worker.Worker;
import net.kernal.spiderman.worker.WorkerManager;
import net.kernal.spiderman.worker.WorkerResult;
import net.kernal.spiderman.worker.download.DownloadTask;
import net.kernal.spiderman.worker.download.Downloader;
import net.kernal.spiderman.worker.extract.conf.Page;
import net.kernal.spiderman.worker.result.ResultTask;

public class ExtractManager extends WorkerManager {
	
	private List<Page> pages;
	private ScriptEngine scriptEngine;
	
	public ExtractManager(int nWorkers, QueueManager queueManager, Counter counter, Logger logger, List<Page> pages) {
		super(nWorkers, queueManager, counter, logger);
		this.pages = pages;
		if (K.isEmpty(pages)) {
			throw new Spiderman.Exception("缺少页面抽取配置");
		}
	}

	public ExtractManager setScriptEngine(ScriptEngine scriptEngine) {
		this.scriptEngine = scriptEngine;
		return this;
	}
	
	public List<Page> getPages() {
		return this.pages;
	}
	
	public ScriptEngine getScriptEngine() {
		return this.scriptEngine;
	}
	
	protected void handleResult(Task task, WorkerResult result) {
		if (result instanceof ExtractResult) {
			// 计数器加1
			long count = getCounter().plus();
//			int limit = getCounter().getLimit();
//			if (limit > 0 && count > limit) {
//				// 通知该结束了
//				return;
//			}
			getLogger().info("解析了第"+count+"个模型");
			// 将成果放入结果处理队列
			final ExtractResult extractResult = (ExtractResult)result;
			getQueueManager().append(new ResultTask(task.getSeed(), extractResult));
		} else if (result instanceof Downloader.Request) {
			Downloader.Request request = (Downloader.Request)result;
			getQueueManager().append(new DownloadTask(task.getSeed(), request));
		}
	}

	protected Task takeTask() {
		return getQueueManager().getExtractQueue().take();
	}

	protected Worker buildWorker() {
		return new ExtractWorker(this);
	}
	
	public static interface ResultHandler {
		public void handle(ExtractResult result, long count);
	}

	protected void clear() {
	}

}
