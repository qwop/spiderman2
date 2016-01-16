package net.kernal.spiderman;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import net.kernal.spiderman.conf.Conf;
import net.kernal.spiderman.logger.ConsoleLogger;
import net.kernal.spiderman.logger.Logger;
import net.kernal.spiderman.queue.QueueManager;
import net.kernal.spiderman.worker.WorkerManager;
import net.kernal.spiderman.worker.download.DownloadManager;
import net.kernal.spiderman.worker.download.DownloadTask;
import net.kernal.spiderman.worker.download.Downloader;
import net.kernal.spiderman.worker.download.HttpClientDownloader;
import net.kernal.spiderman.worker.extract.ExtractManager;
import net.kernal.spiderman.worker.extract.ExtractManager.ResultHandler;
import net.kernal.spiderman.worker.extract.conf.Page;
import net.kernal.spiderman.worker.result.ResultManager;

public class Context {
	
	private Logger logger = null;
	
	private Conf conf;
	private QueueManager queueManager;
	
	private List<WorkerManager> managers;
	
	public Context(Conf conf, ResultHandler resultHandler) {
		final List<Seed> seeds = conf.getSeeds().all();
		final List<Page> pages = conf.getPages().all();
		final Properties params = conf.getParams();
		if (seeds.isEmpty()) 
			throw new Spiderman.Exception("少年,请添加一个种子来让蜘蛛侠行动起来!参考：conf.addSeed");
		if (pages.isEmpty()) 
			throw new Spiderman.Exception("少年,请添加一个页面来让蜘蛛侠行动起来!参考：conf.addPage");
		
		this.conf = conf;
		this.managers = new ArrayList<WorkerManager>();
		final byte level = params.getByte("logger.level", Logger.LEVEL_INFO);
		this.logger = new ConsoleLogger(Context.class, level);
		
		// 构建队列管理器
		queueManager = new QueueManager(params, new ConsoleLogger(QueueManager.class, level));
		// 往队列里添加种子
		seeds.parallelStream()
			.map(seed -> new DownloadTask(seed, new Downloader.Request(seed.getUrl())))
			.forEach(task -> queueManager.append(task));
		// 构建下载管理器
		final int size1 = params.getInt("worker.download.size", 1);
		if (size1 > 0) {
			final Downloader downloader = new HttpClientDownloader(params);
			final int limit = params.getInt("worker.download.result.limit", 0);
			final Counter counter = new Counter(limit, 0);
			final DownloadManager downloadManager = new DownloadManager(size1, queueManager, counter, new ConsoleLogger(DownloadManager.class, level), downloader);
			this.addManager(downloadManager);
		}
		
		// 构建解析管理器
		final int size2 = params.getInt("worker.extract.size", 1);
		if (size2 > 0) {
			final int limit = params.getInt("worker.extract.result.limit", 0);
			final Counter counter = new Counter(limit, 0);
			final ExtractManager extractManager = new ExtractManager(size2, queueManager, counter, new ConsoleLogger(ExtractManager.class, level), pages);
			final String engineName = params.getString("scriptEngine", "nashorn");
			final ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName(engineName);
			extractManager.setScriptEngine(scriptEngine);
			this.addManager(extractManager);
		}
		
		// 构建结果处理管理器
		final int size3 = params.getInt("worker.result.size", 1);
		if (size3 > 0) {
			final int limit = params.getInt("worker.result.limit", 0);
			final Counter counter = new Counter(limit, 0);
			final ResultManager resultManager = new ResultManager(size3, queueManager, counter, new ConsoleLogger(ResultManager.class, level), resultHandler);
			this.addManager(resultManager);
		}
	}
	
	public Collection<WorkerManager> getManagers() {
		return Collections.unmodifiableCollection(this.managers);
	}
	
	public Context addManager(WorkerManager manager) {
		this.managers.add(manager);
		return this;
	}
	
	public Properties getParams() {
		return this.conf.getParams();
	}
	
	public void shutdown() {
		this.queueManager.shutdown();
		this.managers.clear();
		logger.debug("退出...");
	}
	
}
