package net.kernal.spiderman;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

import net.kernal.spiderman.conf.Conf;
import net.kernal.spiderman.task.DownloadTask;
import net.kernal.spiderman.task.ParseTask;
import net.kernal.spiderman.task.ResultTask;
import net.kernal.spiderman.task.Task;
import net.kernal.spiderman.worker.DownloadWorker;
import net.kernal.spiderman.worker.ParseWorker;
import net.kernal.spiderman.worker.ResultWorker;
import net.kernal.spiderman.worker.Worker;
import net.kernal.spiderman.worker.WorkerManager;

/**
 * 蜘蛛侠，根据预言之子设定的目标引领蜘蛛大军开展网络世界采集行动。
 * @author 赖伟威 l.weiwei@163.com 2015-12-01
 * @author 赖伟威 l.weiwei@163.com 2015-12-30
 */
public class Spiderman {

	public static final Logger logger = Logger.getLogger(Spiderman.class.getName());
	
	public Spiderman(Conf conf) {
		if (conf.getSeeds().isEmpty()) 
			throw new RuntimeException("少年,请添加一个种子来让蜘蛛侠行动起来!参考：conf.addSeed");
		if (conf.getTargets().isEmpty()) 
			throw new RuntimeException("少年,请添加一个目标来让蜘蛛侠行动起来!参考：conf.addTarget");
		
		this.conf = conf;
		
		this.counter = new Counter();
		int parsedLimit = conf.getProperties().getInt("parsedLimit", 0);
		if (parsedLimit > 0) {
			this.counter.setCountDown(new CountDownLatch(parsedLimit));
		} 
		
		// 5个包工头
		this.workerManagers = new ArrayList<WorkerManager>(5);
		this.threads = Executors.newFixedThreadPool(5);
		
		// 下载工人包工头
		Worker.Builder downloadBuilder = new Worker.Builder(){
			public Worker build(Task task, Conf conf, Counter counter) {
				return new DownloadWorker((DownloadTask)task, conf, counter);
			}
		};
		final int dpts1 = this.conf.getProperties().getInt("downloader.primary.threadSize", 1);
		if (dpts1 > 0) {
			ThreadPoolExecutor threadsForPrimaryDownload = (ThreadPoolExecutor) Executors.newFixedThreadPool(dpts1);
			WorkerManager mgr1 = new WorkerManager("下载(主)", conf.getPrimaryDownloadTaskQueue(), threadsForPrimaryDownload, downloadBuilder);
			this.workerManagers.add(mgr1);
			counter.setPrimaryDownloadPool(new Counter.Threads(threadsForPrimaryDownload));
		}
		
		final int dpts2 = this.conf.getProperties().getInt("downloader.secondary.threadSize", 1);
		if (dpts2 > 0) {
			ThreadPoolExecutor threadsForSecondaryDownload = (ThreadPoolExecutor) Executors.newFixedThreadPool(dpts2);
			WorkerManager mgr2 = new WorkerManager("下载(次)", conf.getSecondaryDownloadTaskQueue(), threadsForSecondaryDownload, downloadBuilder);
			this.workerManagers.add(mgr2);
			counter.setSecondaryDownloadPool(new Counter.Threads(threadsForSecondaryDownload));
		}
		
		// 解析工人包工头
		Worker.Builder parseBuilder = new Worker.Builder() {
			public Worker build(Task task, Conf conf, Counter counter) {
				return new ParseWorker((ParseTask)task, conf, counter);
			}
		};
		final int ppts1 = this.conf.getProperties().getInt("parser.primary.threadSize", 1);
		if (ppts1 > 0) {
			ThreadPoolExecutor threadsForPrimaryParse = (ThreadPoolExecutor) Executors.newFixedThreadPool(ppts1);
			WorkerManager mgr3 = new WorkerManager("解析(主)", conf.getPrimaryParseTaskQueue(), threadsForPrimaryParse, parseBuilder);
			this.workerManagers.add(mgr3);
			counter.setPrimaryParsePool(new Counter.Threads(threadsForPrimaryParse));
		}
		
		final int ppts2 = this.conf.getProperties().getInt("parser.secondary.threadSize", 1);
		if (ppts2 > 0) {
			ThreadPoolExecutor threadsForSecondaryParse = (ThreadPoolExecutor) Executors.newFixedThreadPool(ppts2);
			WorkerManager mgr4 = new WorkerManager("解析(次)", conf.getSecondaryParseTaskQueue(), threadsForSecondaryParse, parseBuilder);
			this.workerManagers.add(mgr4);
			counter.setSecondaryParsePool(new Counter.Threads(threadsForSecondaryParse));
		}
		
		// 结果处理工人包工头
		Worker.Builder resultBuilder = new Worker.Builder() {
			public Worker build(Task task, Conf conf, Counter counter) {
				return new ResultWorker((ResultTask)task, conf, counter);
			}
		};
		
		final int rts = this.conf.getProperties().getInt("result.threadSize", 1);
		if (rts > 0) {
			ThreadPoolExecutor threadsForResult = (ThreadPoolExecutor) Executors.newFixedThreadPool(rts);
			WorkerManager mgr5 = new WorkerManager("结果", conf.getResultTaskQueue(), threadsForResult, resultBuilder);
			this.workerManagers.add(mgr5);
		}
		
		long waitSeconds = K.convertToSeconds(conf.getProperties().getString("waitSeconds", "1s")).longValue();
		this.workerManagers.forEach(wm -> {
			wm.setConf(conf);
			wm.setCounter(counter);
			wm.setWaitSeconds(waitSeconds);
		});
		
		// JAVA8爽!
		if (conf.getScriptEngine() != null) {
			this.conf.getTargets().all().forEach((target) -> {
				target.getModel().getFields().forEach((field) -> {
					field.getParsers().forEach((parser) -> {
						parser.setScriptEngine(conf.getScriptEngine());
					});
				});
			});
		}
	}
	
	/**
	 * 开展行动
	 * @return
	 */
	public Spiderman go() {
		// 将种子添加到主任务队列里
		conf.getSeeds().all().forEach((seed) -> {
			DownloadTask newTask = new DownloadTask(seed, 0);
			conf.getPrimaryDownloadTaskQueue().put(newTask);
			// 队列计数+1
			counter.primaryDownloadQueuePlus();
		});
		// 状态报告: 行动开始了
		conf.getReportings().reportStart();
		
		this.workerManagers.forEach(manager -> {
			this.threads.execute(manager);
		});
		
		this._holding();
		return this;
	}
	
	public Spiderman stop() {
		this.threads.shutdownNow();
		this.workerManagers.forEach(w -> {
			w.shutdown();
		});
		try {
			if (this.conf.getZbusBroker() != null) {
				this.conf.getZbusBroker().close();
			}
		} catch (IOException e) {}
		this.conf.getReportings().reportStop(counter);
		
		return this;
	}
	
	private ExecutorService threads;
	private List<WorkerManager> workerManagers;
	private Conf conf;
	private Counter counter;
	
	private void _holding() {
		if (this.counter.getCountDown() != null) {
			try {
				this.counter.getCountDown().await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			stop();
			System.exit(-1);
			return;
		} else {
			Long l = null;
			String duration = this.conf.getProperties().getString("duration");
			if (K.isNotBlank(duration)) {
				try {
					long t = K.convertToSeconds(duration).longValue()*1000L;
					l = new Long(t);
				} catch (Throwable e){}
			}
			
			try {
				if (l == null) {
					Thread.currentThread().join();
				} else {
					Thread.currentThread().join(l);
					System.err.println("[Spiderman][由于配置了duration="+this.conf.getProperties().get("duration")+",现在到时间了需要强制退出,若出现异常请以平常心对待]"+K.formatNow());
				}
				stop();
				System.exit(-1);
			} catch (InterruptedException e) {
				stop();
				System.exit(-1);
			}
		}
	}
	
	public static class Counter {
		
		private CountDownLatch countDown;
		
		private AtomicLong countPrimaryDownload;
		private AtomicLong countSecondaryDownload;
		
		private AtomicLong countPrimaryDownloadQueue;
		private AtomicLong countSecondaryDownloadQueue;
		
		private AtomicLong countPrimaryParseQueue;
		private AtomicLong countSecondaryParseQueue;
		
		private AtomicLong countPrimaryParsed;
		private AtomicLong countSecondaryParsed;
		
		private Threads primaryDownloadPool;
		private Threads secondaryDownloadPool;
		private Threads primaryParsePool;
		private Threads secondaryParsePool;
		
		public Counter() {
			this.countPrimaryDownload = new AtomicLong(0);
			this.countSecondaryDownload = new AtomicLong(0);
			
			this.countPrimaryDownloadQueue = new AtomicLong(0);
			this.countSecondaryDownloadQueue = new AtomicLong(0);
			
			this.countPrimaryParseQueue = new AtomicLong(0);
			this.countSecondaryParseQueue = new AtomicLong(0);
			
			this.countPrimaryParsed = new AtomicLong(0);
			this.countSecondaryParsed = new AtomicLong(0);
			
			this.primaryDownloadPool = new Threads(null);
			this.secondaryDownloadPool = new Threads(null);
			this.primaryParsePool = new Threads(null);
			this.secondaryParsePool = new Threads(null);
		}
		public Long primaryDownloadPlus() {
			return this.countPrimaryDownload.addAndGet(1);
		}
		public Long secondaryDownloadPlus() {
			return this.countSecondaryDownload.addAndGet(1);
		}
		
		public Long primaryDownloadQueuePlus() {
			return this.countPrimaryDownloadQueue.addAndGet(1);
		}
		public Long secondaryDownloadQueuePlus() {
			return this.countSecondaryDownloadQueue.addAndGet(1);
		}
		
		public Long primaryParseQueuePlus() {
			return this.countPrimaryParseQueue.addAndGet(1);
		}
		public Long secondaryParseQueuePlus() {
			return this.countSecondaryParseQueue.addAndGet(1);
		}
		
		public Long primaryParsedPlus() {
			if (this.countDown != null) {
				this.countDown.countDown();
			}
			return this.countPrimaryParsed.addAndGet(1);
		}
		public Long secondaryParsedPlus() {
			return this.countSecondaryParsed.addAndGet(1);
		}
		
		public CountDownLatch getCountDown() {
			return countDown;
		}
		public void setCountDown(CountDownLatch countDown) {
			this.countDown = countDown;
		}
		
		public AtomicLong getCountPrimaryDownload() {
			return countPrimaryDownload;
		}
		public AtomicLong getCountSecondaryDownload() {
			return countSecondaryDownload;
		}
		public AtomicLong getCountPrimaryDownloadQueue() {
			return countPrimaryDownloadQueue;
		}
		public AtomicLong getCountSecondaryDownloadQueue() {
			return countSecondaryDownloadQueue;
		}
		public AtomicLong getCountPrimaryParseQueue() {
			return countPrimaryParseQueue;
		}
		public AtomicLong getCountSecondaryParseQueue() {
			return countSecondaryParseQueue;
		}
		public AtomicLong getCountPrimaryParsed() {
			return this.countPrimaryParsed;
		}
		public AtomicLong getCountSecondaryParsed() {
			return this.countSecondaryParsed;
		}
		public Threads getPrimaryDownloadPool() {
			return primaryDownloadPool;
		}
		public void setPrimaryDownloadPool(Threads primaryDownloadPool) {
			this.primaryDownloadPool = primaryDownloadPool;
		}
		public Threads getSecondaryDownloadPool() {
			return secondaryDownloadPool;
		}
		public void setSecondaryDownloadPool(Threads secondaryDownloadPool) {
			this.secondaryDownloadPool = secondaryDownloadPool;
		}
		public Threads getPrimaryParsePool() {
			return primaryParsePool;
		}
		public void setPrimaryParsePool(Threads primaryParsePool) {
			this.primaryParsePool = primaryParsePool;
		}
		public Threads getSecondaryParsePool() {
			return secondaryParsePool;
		}
		public void setSecondaryParsePool(Threads secondaryParsePool) {
			this.secondaryParsePool = secondaryParsePool;
		}

		public static class Threads {
			private ThreadPoolExecutor pool;
			public Threads(ThreadPoolExecutor pool) {
				this.pool = pool;
			}
			public int getPoolSize() {
				return this.pool != null ? this.pool.getCorePoolSize() : 0;
			}
			public int getActiveCount() {
				return this.pool != null ? this.pool.getActiveCount() : 0;
			}
			public long getCompletedTaskCount() {
				return this.pool != null ? this.pool.getCompletedTaskCount() : 0;
			}
		}
	}
	
}
