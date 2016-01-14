package net.kernal.spiderman.worker.extract;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import net.kernal.spiderman.worker.extract.conf.Field;
import net.kernal.spiderman.worker.extract.conf.Page;

public class ExtractManager extends WorkerManager {
	
	private List<Page> pages;
	private ScriptEngine scriptEngine;
	private Map<String, Field.ValueFilter> filters;
	private ResultHandler resultHandler;
	
	public ExtractManager(int nWorkers, QueueManager queueManager, Counter counter, Logger logger, List<Page> pages, ResultHandler resultHandler) {
		super(nWorkers, queueManager, counter, logger);
		this.pages = pages;
		if (K.isEmpty(pages)) {
			throw new Spiderman.Exception("缺少页面抽取配置");
		}
		this.filters = new HashMap<String, Field.ValueFilter>();
		this.resultHandler = resultHandler;
	}

	public ExtractManager setScriptEngine(ScriptEngine scriptEngine) {
		this.scriptEngine = scriptEngine;
		return this;
	}
	
	public ExtractManager registerFilter(String name, Field.ValueFilter filter) {
		this.filters.put(name, filter);
		return this;
	}
	
	public List<Page> getPages() {
		return this.pages;
	}
	
	public ScriptEngine getScriptEngine() {
		return this.scriptEngine;
	}
	
	public Field.ValueFilter getFilter(String name) {
		return this.filters.get(name);
	}

	protected void handleResult(Task task, WorkerResult result) {
		if (result instanceof ExtractResult) {
			ExtractResult extractResult = (ExtractResult)result;
			// 计数器加1
			long count = getCounter().plus();
			getLogger().info("解析了第"+count+"个模型: "+extractResult);
			this.resultHandler.handle(extractResult, count);
		} else if (result instanceof Downloader.Request) {
			Downloader.Request request = (Downloader.Request)result;
			getQueueManager().append(new DownloadTask(task.getSeed(), request));
		}
	}

	protected Task takeTask() {
		return getQueueManager().getExtractQueue().take();
	}

	protected Worker buildWorker(Task task) {
		return new ExtractWorker(this, (ExtractTask)task);
	}
	
	public static interface ResultHandler {
		public void handle(ExtractResult result, long count);
	}

	protected void clear() {
	}

}
