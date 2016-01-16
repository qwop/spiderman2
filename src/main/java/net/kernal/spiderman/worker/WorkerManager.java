package net.kernal.spiderman.worker;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import net.kernal.spiderman.Counter;
import net.kernal.spiderman.Spiderman;
import net.kernal.spiderman.logger.Logger;
import net.kernal.spiderman.queue.QueueManager;

/**
 * 工人经理，俗称包工头。
 * 1. 安排工人们开工
 * 2. 接收工人工作结果
 * 3. 结束的时候显示统计结果
 * @author 赖伟威 l.weiwei@163.com 2016-01-16
 *
 */
public abstract class WorkerManager implements Runnable {

	private Logger logger;
	protected Logger getLogger() {
		return this.logger;
	}
	
	private ThreadPoolExecutor threads;
	private List<Worker> workers;
	private QueueManager queueManager;
	protected QueueManager getQueueManager() {
		return queueManager;
	}
	
	private Counter counter;
	public Counter getCounter() {
		return this.counter;
	}
	
	private List<Listener> listeners;
	public static interface Listener {
		public void shouldShutdown();
	}
	public WorkerManager addListener(Listener listener) {
		this.listeners.add(listener);
		return this;
	}
	
	/**
	 * 构造器
	 * @param nWorkers
	 * @param taskQueue
	 */
	public WorkerManager(int nWorkers, QueueManager queueManager, Counter counter, Logger logger) {
		final int n = nWorkers > 0 ? nWorkers : 1;
		this.threads = (ThreadPoolExecutor)Executors.newFixedThreadPool(n);
		this.queueManager = queueManager;
		this.counter = counter;
		this.listeners = new ArrayList<Listener>();
		this.logger = logger;
	}
	
	/**
	 * 处理工人的工作结果, 子类实现
	 * @param task
	 * @param result
	 */
	protected abstract void handleResult(Task task, WorkerResult result);
	/**
	 * 获取任务，子类实现
	 */
	protected abstract Task takeTask();
	/**
	 * 获取工人实例，子类实现
	 * @param task
	 */
	protected abstract Worker buildWorker();
	protected abstract void clear();
	
	/**
	 * 接收工人完成工作的通知，然后调用子类去处理结果
	 */
	public void done(Task task, WorkerResult result) {
		this.handleResult(task, result);
	}
	
	/**
	 * 工作
	 */
	public void run() {
		if (this.queueManager == null) {
			throw new Spiderman.Exception(getClass().getSimpleName()+" 缺少队列管理器");
		}
		final int nWorkers = threads.getCorePoolSize();
		workers = new ArrayList<Worker>(nWorkers);
		for (int i = 0; i < nWorkers; i++) {
			final Worker worker = this.buildWorker();
			workers.add(worker);
		}
		
		this.workers.forEach(w -> threads.execute(w));
		this.counter.await();
		this.shutdown();
	}
	
	/**
	 * 停工
	 */
	private void shutdown() {
		try {
			this.workers.forEach(w -> w.stop());
			this.threads.shutdownNow();
		} catch(Throwable e) {
		} finally {
			this.clear();
			logger.debug("退出管理器...");
			// 统计结果
			final String fmt = "统计结果 \r\n 耗时:%sms \r\n 计数:%s \r\n 线程池:总数(%s) 运行中(%s) 已完成(%s) \r\n";
			final String msg = String.format(fmt, counter.getCost(), counter.get(), threads.getCorePoolSize(), threads.getActiveCount(), threads.getCompletedTaskCount());
			logger.debug(msg);
			listeners.forEach(l -> l.shouldShutdown());
			this.listeners.clear();
		}
	}
	
}
