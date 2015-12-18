package net.kernal.spiderman;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 蜘蛛侠，根据预言之子设定的目标引领蜘蛛大军开展网络世界采集行动。
 * @author 赖伟威 l.weiwei@163.com 2015-12-01
 *
 */
public class Spiderman {

	public Spiderman(Conf conf) {
		if (conf.seeds.isEmpty()) 
			throw new RuntimeException("少年,请添加一个种子来让蜘蛛侠行动起来!参考：conf.addSeed");
		
		if (conf.targets.isEmpty()) 
			throw new RuntimeException("少年,请添加一个目标来让蜘蛛侠行动起来!参考：conf.addTarget");
		
		this.conf = conf;
		int downloadLimit = conf.getProperties().getInt("downloader.limit", 0);
		if (downloadLimit > 0) {
			this.counter = new CountDownLatch(downloadLimit);
		}
		this.singlePool = Executors.newSingleThreadExecutor();
		int threadSize = this.conf.properties.getInt("threadSize", 1);
		this.threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(threadSize);
	}
	
	/**
	 * 开展行动
	 * @return
	 */
	public Spiderman go() {
		this.conf.reportings.reportStart();
		this.singlePool.execute(new Runnable() {
			public void run() {
				// 将种子添加到任务队列里
				for (Downloader.Request seed : conf.seeds.getAll()) {
					Task newTask = new Task(seed, 0);
					conf.taskQueue.put(newTask);
				}
				
				while (true) {
					while(true) {
						int coreSize = threadPool.getCorePoolSize();
						long completedTaskCount = threadPool.getCompletedTaskCount();
						long taskCount = threadPool.getTaskCount();
						long runningCount = taskCount - completedTaskCount;
						if (runningCount < coreSize) {
							break;
						}
						try {
							Thread.sleep(conf.getProperties().getLong("waitThread", 1000));
						} catch (InterruptedException e) {
						}
					}
					while (true) {
						Task task = conf.taskQueue.poll();
						if (task == null) {
							try {
								Thread.sleep(conf.getProperties().getLong("waitQueue", 1000));
							} catch (InterruptedException e) {
							}
							continue;
						}
						
//						System.out.println("execute spider for task->" + pTask.getRequest().getUrl());
						threadPool.execute(new Spider(conf, task, counter));
						
						break;
					}
				}
			}
		});
		
		this._holding();
		return this;
	}
	
	public Spiderman stop() {
		this.singlePool.shutdownNow();
		this.threadPool.shutdownNow();
		this.conf.reportings.reportStop();
		return this;
	}
	
	private ExecutorService singlePool;
	private ThreadPoolExecutor threadPool;
	private Conf conf;
	private CountDownLatch counter;
	
	private void _holding() {
		if (this.counter != null) {
			try {
				this.counter.await();
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
		private TaskQueue taskQueue;
		
		public static interface Builder {
			public Conf build();
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
		public Conf setTaskQueue(TaskQueue taskQueue) {
			this.taskQueue = taskQueue;
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
		public TaskQueue getTaskQueue() {
			return taskQueue;
		}
	}

	public static class Seeds {
		private List<Downloader.Request> requests;
		public Seeds() {
			this.requests = new ArrayList<Downloader.Request>();
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
		
		public List<Target> getAll() {
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
	
}
