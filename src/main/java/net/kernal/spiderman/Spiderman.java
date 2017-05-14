package net.kernal.spiderman;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import net.kernal.spiderman.kit.Context;
import net.kernal.spiderman.kit.Counter;
import net.kernal.spiderman.kit.K;
import net.kernal.spiderman.kit.Properties;
import net.kernal.spiderman.logger.Logger;
import net.kernal.spiderman.logger.Loggers;
import net.kernal.spiderman.worker.WorkerManager;
import net.kernal.spiderman.worker.download.DownloadTask;

/**
 * 客户端类 
 */
public class Spiderman {
    /**
     * 日志工具
     */
	private final static Logger logger = Loggers.getLogger(Spiderman.class);
	/**
	 * 定时周期执行指定的任务服务
	 */
	private ScheduledExecutorService scheduler;
	/**
	 * 执行上下文
	 */
	private Context context;
    /**
     * 工人经理、包工头集合
     */
	private Collection<WorkerManager> managers;
    /**
     * 异步执行的机制,让任务在后台执行
     */
	private ExecutorService threads;
    /**
     * 计数器
     */
	private Counter counter;
    /**
     * 执行时长
     */
	private long duration;
	
	/**
	 * 通过加载配置，构造客户端类
	 */
	public Spiderman(Config config) {
		this.context = new Context(config);
		final Properties params = context.getParams();
		this.scheduler = Executors.newSingleThreadScheduledExecutor();
		this.managers = context.getManagers();
		this.managers.forEach(m -> {
			m.addListener(() -> { 
				counter.plus(); 
			});
		});
		this.threads = Executors.newFixedThreadPool(managers.size());
		duration = K.convertToMillis(params.getString("duration", "0")).longValue();
		counter = new Counter(managers.size(), duration);
	}
	
	public Context getContext() {
		return this.context;
	}
	
	/**
	 * 开始执行
	 * 1. 启动工头
	 * 2. 调度, 固定一段时间清除种子和一些中间过程任务，重新将种子放入任务队列
	 * 3.
	 * <Short overview of features>  
	 * <Feature details> 
	 * @return 
	 * @author	qwop
	 * @date	May 13, 2017
	 * @see [Class、Class#Method、Class#Field]
	 */
	public Spiderman go() {
		logger.debug("开始行动...");
		// 启动各个工头
		this.managers.forEach(m -> threads.execute(m));
		// 调度, 固定一段时间清除种子和一些中间过程任务，重新将种子放入任务队列
		final InitialSeeds initSeeds = new InitialSeeds();
		final long period = K.convertToMillis(context.getParams().getString("scheduler.period", "0")).longValue();
		if (period > 0) {
			this.scheduler.scheduleAtFixedRate(initSeeds, 5000, period, TimeUnit.MILLISECONDS);
		} else {
			initSeeds.execute();
		}
		
		// 启动一个线程、控制控制超时；
		Thread thread = new Thread(() -> {
			// 阻塞等待计数器归0
			try {
				this.counter.await();
			} finally {
				if (this.counter.isTimeout()) {
					// 若是超时退出，先关闭manager
					logger.warn("运行时间["+this.counter.getCost()+"]已经达到或超过设置的最大运行时间[duration="+this.duration+"],将强行停止行动");
					this.stop();
				} else {
					logger.warn("当前采集的结果数["+this.counter.get()+"]已经达到或超过设置的最大数量[worker.result.limit="+this.counter.getLimit()+"],将强行停止行动");
				}
				this._stop();
			}
		});
		thread.start();
		
		return this;
	}
	
	public void stop() {
		this.managers.forEach(m -> m.shutdownAndWait());
	}
	
	private void _stop() {
		this.scheduler.shutdownNow();
		logger.debug("调度器关闭...");
		this.threads.shutdownNow();
		logger.debug("工头线程池关闭...");
		this.context.shutdown();
	}
	
	// 初始化种子
	private class InitialSeeds implements Runnable {
		public void run() {
			logger.debug("调度...");
			// 清除掉一些消息
			try {
				logger.warn("开始清除Keys");
				context.getTaskManager().removeKeys("seeds");
				logger.warn("清除Keys成功[group=seeds]");
				context.getConf().getPages().all().parallelStream()
					.filter(p -> !p.isPersisted())
					.map(p -> p.getName())
					.forEach(group -> {
						context.getTaskManager().removeKeys(group);
						logger.warn("清除Keys成功[group="+group+"]");
					});
			} catch (Throwable e) {
				logger.error("清除Keys失败", e);
			}
			this.execute();
		}
		public void execute() {
			logger.debug("准备初始化种子...");
			try {
				// 往队列里添加种子
				context.getSeeds().all().parallelStream()
					.map(seed -> new DownloadTask(seed, "seeds"))
					.forEach(task -> context.getTaskManager().append(task));
				logger.debug("初始化种子成功...");
			} catch (Throwable e) {
				logger.error("初始化种子失败", e);
			}
		}
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
