package net.kernal.spiderman;

import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import net.kernal.spiderman.conf.Conf;
import net.kernal.spiderman.logger.ConsoleLogger;
import net.kernal.spiderman.logger.Logger;
import net.kernal.spiderman.queue.QueueManager;
import net.kernal.spiderman.worker.download.DownloadManager;
import net.kernal.spiderman.worker.download.DownloadTask;
import net.kernal.spiderman.worker.download.Downloader;
import net.kernal.spiderman.worker.download.HttpClientDownloader;
import net.kernal.spiderman.worker.extract.ExtractManager;
import net.kernal.spiderman.worker.extract.ExtractManager.ResultHandler;
import net.kernal.spiderman.worker.extract.conf.Page;

public class Context {
	
	private Logger logger = null;
	
	private Conf conf;
	private QueueManager queueManager;
	private DownloadManager downloadManager;
	private ExtractManager extractManager;
	
	public Context(Conf conf, ResultHandler resultHandler) {
		final List<Seed> seeds = conf.getSeeds().all();
		final List<Page> pages = conf.getPages().all();
		final Properties params = conf.getParams();
		
		if (seeds.isEmpty()) 
			throw new Spiderman.Exception("少年,请添加一个种子来让蜘蛛侠行动起来!参考：conf.addSeed");
		if (pages.isEmpty()) 
			throw new Spiderman.Exception("少年,请添加一个页面来让蜘蛛侠行动起来!参考：conf.addPage");
		
		final byte level = params.getByte("logger.level", Logger.LEVEL_INFO);
		this.logger = new ConsoleLogger(Context.class, level);
		
		this.conf = conf;
		
		// 构建队列管理器
		queueManager = new QueueManager(params, new ConsoleLogger(QueueManager.class, level));
				
		// 构建下载管理器
		final long duration = K.convertToMillis(params.getString("duration", "0")).longValue();
		final int size1 = params.getInt("worker.downloader.size", 1);
		if (size1 > 0) {
			final Downloader downloader = new HttpClientDownloader(params);
			final int limit = params.getInt("worker.downloader.result.limit", 0);
			final Counter counter = new Counter(limit, duration);
			downloadManager = new DownloadManager(size1, queueManager, counter, new ConsoleLogger(DownloadManager.class, level), downloader);
			// 添加种子
			seeds.parallelStream()
				.map(seed -> new DownloadTask(seed, new Downloader.Request(seed.getUrl())))
				.forEach(task -> queueManager.append(task));
		}
		
		// 构建解析管理器
		final int size2 = params.getInt("worker.extractor.size", 1);
		if (size2 > 0) {
			final int limit = params.getInt("worker.extractor.result.limit", 0);
			final Counter counter = new Counter(limit, duration);
			extractManager = new ExtractManager(size2, queueManager, counter, new ConsoleLogger(ExtractManager.class, level), pages, resultHandler);
			final String engineName = params.getString("scriptEngine", "nashorn");
			final ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName(engineName);
			extractManager.setScriptEngine(scriptEngine);
		}
	}
	
	public DownloadManager getDownloadManager() {
		return this.downloadManager;
	}
	
	public ExtractManager getExtractManager() {
		return this.extractManager;
	}
	
	public Properties getParams() {
		return this.conf.getParams();
	}
	
	public void shutdown() {
		if (this.downloadManager != null) {
			this.downloadManager.shutdown();
		}
		if (this.extractManager != null) {
			this.extractManager.shutdown();
		}
		if (this.queueManager != null) {
			this.queueManager.shutdown();
		}
		logger.debug("退出...");
	}
	
}
