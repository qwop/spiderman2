package net.kernal.spiderman;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.kernal.spiderman.logger.ConsoleLogger;
import net.kernal.spiderman.logger.Logger;
import net.kernal.spiderman.worker.WorkerManager;
import net.kernal.spiderman.worker.extract.ExtractManager;

/**
 * 客户端类 
 */
public class Spiderman {

	private Logger logger;
	
	private Context context;
	private List<WorkerManager> managers;
	private ExecutorService threads;
	private Counter counter;
	
	public Spiderman(Context context) {
		this.context = context;
		final byte level = context.getParams().getByte("logger.level", Logger.LEVEL_INFO);
		this.logger = new ConsoleLogger(ExtractManager.class, level);
		this.threads = Executors.newCachedThreadPool();
		managers = new ArrayList<WorkerManager>(2);
		final WorkerManager downloadManager = context.getDownloadManager();
		if (downloadManager != null) {
			downloadManager.addListener(() -> counter.plus());
			managers.add(downloadManager);
		}
		final WorkerManager extractManager = context.getExtractManager();
		if (extractManager != null) {
			downloadManager.addListener(() -> counter.plus());
			managers.add(extractManager);
		}
		counter = new Counter(managers.size(), 0);
	}
	
	public void go() {
		logger.debug("开始行动...");
		this.managers.forEach(m -> threads.execute(m));
		this.counter.await();
		stop();
	}
	
	public void stop() {
		this.threads.shutdownNow();
		this.context.shutdown();
		logger.debug("停止行动...");
	}
	
	public static class Exception extends RuntimeException {
		private static final long serialVersionUID = 2703000025276351774L;
		public Exception(String msg) {
			super(msg);
		}
		public Exception(String msg, Throwable cause) {
			super(msg, cause);
		}
	}
	
}
