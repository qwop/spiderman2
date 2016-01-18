package net.kernal.spiderman;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.kernal.spiderman.logger.ConsoleLogger;
import net.kernal.spiderman.logger.Logger;
import net.kernal.spiderman.worker.WorkerManager;
import net.kernal.spiderman.worker.download.DownloadTask;
import net.kernal.spiderman.worker.download.Downloader;

/**
 * 客户端类 
 */
public class Spiderman {

	private Logger logger;
	
	private Context context;
	private Collection<WorkerManager> managers;
	private ExecutorService threads;
	private Counter counter;
	
	public Spiderman(Context context) {
		this.context = context;
		final Properties params = context.getParams();
		final byte level = params.getByte("logger.level", Logger.LEVEL_INFO);
		this.logger = new ConsoleLogger(Spiderman.class, level);
		this.threads = Executors.newCachedThreadPool();
		this.managers = context.getManagers();
		this.managers.forEach(m -> m.addListener(() -> counter.plus()));
		final long duration = K.convertToMillis(params.getString("duration", "0")).longValue();
		counter = new Counter(managers.size(), duration);
	}
	
	public void go() {
		logger.debug("开始行动...");
		managers.forEach(m -> threads.execute(m));
		// 往队列里添加种子
		final boolean isSeedUnique = context.getParams().getBoolean("seed.unique", false);
		context.getSeeds().all().parallelStream()
			.map(seed -> new DownloadTask(seed, isSeedUnique, new Downloader.Request(seed.getUrl())))
			.forEach(task -> context.getQueueManager().append(task));
		counter.await();
		_stop();
	}
	
	public void stop() {
		this.managers.forEach(m -> m.getCounter().stop());
	}
	
	private void _stop() {
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
