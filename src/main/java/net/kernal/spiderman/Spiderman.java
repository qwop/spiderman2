package net.kernal.spiderman;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

import javax.script.ScriptEngine;

import net.kernal.spiderman.downloader.Downloader;
import net.kernal.spiderman.queue.TaskQueue;
import net.kernal.spiderman.reporting.Reporting;
import net.kernal.spiderman.reporting.Reportings;
import net.kernal.spiderman.task.DownloadTask;
import net.kernal.spiderman.task.ParseTask;
import net.kernal.spiderman.task.Task;
import net.kernal.spiderman.worker.DownloadSpider;
import net.kernal.spiderman.worker.ParseSpider;

/**
 * 蜘蛛侠，根据预言之子设定的目标引领蜘蛛大军开展网络世界采集行动。
 * @author 赖伟威 l.weiwei@163.com 2015-12-01
 * @author 赖伟威 l.weiwei@163.com 2015-12-30
 */
public class Spiderman {

	public static final Logger logger = Logger.getLogger(Spiderman.class.getName());
	
	public Spiderman(Conf conf) {
		if (conf.seeds.isEmpty()) 
			throw new RuntimeException("少年,请添加一个种子来让蜘蛛侠行动起来!参考：conf.addSeed");
		
		if (conf.targets.isEmpty()) 
			throw new RuntimeException("少年,请添加一个目标来让蜘蛛侠行动起来!参考：conf.addTarget");
		
		this.conf = conf;
		
		this.threadsForGo = Executors.newFixedThreadPool(2);
		
		final int threadSizeForDownload = this.conf.properties.getInt("downloader.threadSize", 1);
		this.threadsForDownload = (ThreadPoolExecutor) Executors.newFixedThreadPool(threadSizeForDownload);
		
		final int threadSizeForParse = this.conf.properties.getInt("parser.threadSize", 1);
		if (threadSizeForParse > 0) {
			this.threadsForParse = (ThreadPoolExecutor) Executors.newFixedThreadPool(threadSizeForParse);
		}
		
		this.counter = new Counter();
		int parsedLimit = conf.properties.getInt("parsedLimit", 0);
		if (parsedLimit > 0) {
			this.counter.setCountDown(new CountDownLatch(parsedLimit));
		} 
		counter.setDownloadPool(new Counter.Threads(threadsForDownload));
		counter.setParsePool(new Counter.Threads(threadsForParse));
		
		// JAVA8爽!
		if (conf.scriptEngine != null) {
			this.conf.targets.all().forEach((target) -> {
				target.getModel().getFields().forEach((field) -> {
					field.getParsers().forEach((parser) -> {
						parser.setScriptEngine(conf.scriptEngine);
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
		this.threadsForGo.execute(new Runnable() {
			public void run() {
				// 将种子添加到任务队列里
				conf.seeds.all().forEach((seed) -> {
					DownloadTask newTask = new DownloadTask(seed, 0);
					conf.getDownloadTaskQueue().put(newTask);
					// 队列计数+1
					counter.downloadQueuePlus();
				});
				// 状态报告: 行动开始了
				conf.reportings.reportStart();
				while (true) {
					while(true) {
						int coreSize = threadsForDownload.getCorePoolSize();
						long completedTaskCount = threadsForDownload.getCompletedTaskCount();
						long taskCount = threadsForDownload.getTaskCount();
						long runningCount = taskCount - completedTaskCount;
						if (runningCount < coreSize) {
							break;
						}
						try {
							long seconds = K.convertToSeconds(conf.getProperties().getString("downloader.waitThread", "1s")).longValue();
							logger.info("下载线程池负载已满，将等待"+seconds+"秒再尝试申请线程资源");
							Thread.sleep(seconds*1000L);
						} catch (InterruptedException e) {}
					}
					while (true) {
						Task task = conf.getDownloadTaskQueue().poll();
						if (task == null) {
							try {
								long seconds = K.convertToSeconds(conf.getProperties().getString("waitQueue", "1s")).longValue();
								logger.info("待下载队列已无任务可分配，将等待"+seconds+"秒再尝试申领任务");
								Thread.sleep(seconds*1000L);
							} catch (InterruptedException e) {}
							continue;
						}
						
						try {
							if (task instanceof DownloadTask) {
								threadsForDownload.execute(new DownloadSpider((DownloadTask)task, conf, counter));
								
							}
						} catch (java.util.concurrent.RejectedExecutionException e) {}
						
						break;
					}
				}
			}
		});
		if (threadsForParse != null) {
			this.threadsForGo.execute(new Runnable() {
				public void run() {
					while (true) {
						while(true) {
							int coreSize = threadsForParse.getCorePoolSize();
							long completedTaskCount = threadsForParse.getCompletedTaskCount();
							long taskCount = threadsForParse.getTaskCount();
							long runningCount = taskCount - completedTaskCount;
							if (runningCount < coreSize) {
								break;
							}
							try {
								long seconds = K.convertToSeconds(conf.getProperties().getString("parser.waitThread", "1s")).longValue();
								logger.info("解析线程池负载已满，将等待"+seconds+"秒再尝试申请线程资源");
								Thread.sleep(seconds*1000L);
							} catch (InterruptedException e) {}
						}
						while (true) {
							Task task = conf.getParseTaskQueue().poll();
							if (task == null) {
								try {
									long seconds = K.convertToSeconds(conf.getProperties().getString("waitQueue", "1s")).longValue();
									logger.info("待解析队列已无任务可分配，将等待"+seconds+"秒再尝试申领任务");
									Thread.sleep(seconds*1000L);
								} catch (InterruptedException e) {}
								continue;
							}
							
							try {
								if (task instanceof ParseTask) {
									threadsForParse.execute(new ParseSpider((ParseTask)task, conf, counter));
								}
							} catch (java.util.concurrent.RejectedExecutionException e) {}
							
							break;
						}
					}
				}
			});
		}
		
		this._holding();
		return this;
	}
	
	public Spiderman stop() {
		this.threadsForGo.shutdownNow();
		this.threadsForDownload.shutdownNow();
		this.threadsForParse.shutdownNow();
		
		this.conf.reportings.reportStop(counter);
		
		return this;
	}
	
	private ExecutorService threadsForGo;
	private ThreadPoolExecutor threadsForDownload;
	private ThreadPoolExecutor threadsForParse;
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
	
	public static class Conf {
		
		public Conf() {
			seeds = new Seeds();
			targets = new Targets();
			properties = new Properties();
			reportings = new Reportings();
		}
		
		private Seeds seeds;
		private Targets targets;
		private Properties properties;
		private Downloader downloader;
		private Reportings reportings;
		private TaskQueue downloadTaskQueue;
		private TaskQueue parseTaskQueue;
		private ScriptEngine scriptEngine;
		
		public static interface Builder {
			public Conf build() throws Exception;
		}
		
		public Conf addSeed(String url) {
			seeds.add(new Downloader.Request(url));
			return this;
		}
		public Conf addSeed(String url, String httpMethod) {
			seeds.add(new Downloader.Request(url, httpMethod));
			return this;
		}
		public Conf addSeed(Downloader.Request request) {
			seeds.add(request);
			return this;
		}
		public Conf addTarget(Target target) {
			targets.add(target);
			return this;
		}
		public Conf set(String property, Object value) {
			this.properties.put(property, value);
			return this;
		}
		public Conf setDownloadTaskQueue(TaskQueue taskQueue) {
			this.downloadTaskQueue = taskQueue;
			return this;
		}
		public Conf setParseTaskQueue(TaskQueue taskQueue) {
			this.parseTaskQueue = taskQueue;
			return this;
		}
		public Conf setDownloader(Downloader downloader) {
			this.downloader = downloader;
			return this;
		}
		public Conf addReporting(Reporting reporting) {
			this.reportings.add(reporting);
			return this;
		}
		public Conf setScriptEngine(ScriptEngine scriptEngine) {
			this.scriptEngine = scriptEngine;
			return this;
		}
		public Seeds getSeeds() {
			return seeds;
		}
		public Targets getTargets() {
			return targets;
		}
		public Properties getProperties() {
			return properties;
		}
		public Downloader getDownloader() {
			return downloader;
		}
		public Reportings getReportings() {
			return reportings;
		}
		public TaskQueue getDownloadTaskQueue() {
			return downloadTaskQueue;
		}
		public TaskQueue getParseTaskQueue() {
			return parseTaskQueue;
		}
	}

	public static class Seeds {
		private List<Downloader.Request> requests;
		public Seeds() {
			this.requests = new ArrayList<Downloader.Request>();
		}
		public List<Downloader.Request> all() {
			return this.getAll();
		}
		public List<Downloader.Request> getAll(){
			return this.requests;
		}
		public boolean isEmpty() {
			return this.requests.isEmpty();
		}
		public Seeds add(Downloader.Request request) {
			this.requests.add(request);
			return this;
		}
		public Seeds add(String url) {
			return this.add(new Downloader.Request(url));
		}
	}
	
	public static class Targets {
		private List<Target> list;
		
		public Targets() {
			this.list = new ArrayList<Target>();
		}
		
		public List<Target> all() {
			return this.list;
		}
		
		public Targets add(Target... target) {
			this.list.addAll(Arrays.asList(target));
			return this;
		}
		
		public Targets addAll(List<Target> targets) {
			this.list.addAll(targets);
			return this;
		}
		
		public boolean isEmpty() {
			return this.list.isEmpty();
		}
	}
	
	public static class Counter {
		private CountDownLatch countDown;
		private AtomicLong countDownload;
		private AtomicLong countDownloadQueue;
		private AtomicLong countParseQueue;
		private AtomicLong countTarget;
		private AtomicLong countParsed;
		private Threads downloadPool;
		private Threads parsePool;
		
		public Counter() {
			this.countDownload = new AtomicLong(0);
			this.countDownloadQueue = new AtomicLong(0);
			this.countParseQueue = new AtomicLong(0);
			this.countTarget = new AtomicLong(0);
			this.countParsed = new AtomicLong(0);
		}
		public Long downloadPlus() {
			return this.countDownload.addAndGet(1);
		}
		public Long downloadQueuePlus() {
			return this.countDownloadQueue.addAndGet(1);
		}
		public Long parseQueuePlus() {
			return this.countParseQueue.addAndGet(1);
		}
		public Long targetPlus() {
			return this.countTarget.addAndGet(1);
		}
		public Long parsedPlus() {
			if (this.countDown != null) {
				this.countDown.countDown();
			}
			return this.countParsed.addAndGet(1);
		}
		public CountDownLatch getCountDown() {
			return countDown;
		}
		public void setCountDown(CountDownLatch countDown) {
			this.countDown = countDown;
		}
		public AtomicLong getCountDownload() {
			return countDownload;
		}
		public AtomicLong getCountDownloadQueue() {
			return this.countDownloadQueue;
		}
		public AtomicLong getCountParseQueue() {
			return this.countParseQueue;
		}
		public AtomicLong getCountTarget() {
			return this.countTarget;
		}
		public AtomicLong getCountParsed() {
			return this.countParsed;
		}
		
		public Threads getDownloadPool() {
			return downloadPool;
		}
		public void setDownloadPool(Threads downloadPool) {
			this.downloadPool = downloadPool;
		}
		public Threads getParsePool() {
			return parsePool;
		}
		public void setParsePool(Threads parsePool) {
			this.parsePool = parsePool;
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
