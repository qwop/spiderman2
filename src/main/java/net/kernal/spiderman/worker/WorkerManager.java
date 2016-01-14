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
 * 1. 从队列里获取任务
 * 2. 将任务分配给工人
 * 3. 接收工人工作结果
 * @author laiweiwei
 *
 */
public abstract class WorkerManager implements Runnable {

	private Logger logger;
	protected Logger getLogger() {
		return this.logger;
	}
	
	private ThreadPoolExecutor threads;
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
	 * 停工
	 */
	public void shutdown() {
		try {
			this.threads.shutdownNow();
		} catch(Throwable e) {
		} finally {
			this.clear();
			logger.debug("退出...");
		}
	}
	
	/**
	 * 处理工人的工作结果, 子类实现
	 * @param task
	 * @param result
	 */
	protected abstract void handleResult(Task task, WorkerResult result);
	
	/**
	 * 接收工人完成工作的通知
	 * @param worker
	 */
	public void done(Task task, WorkerResult result) {
		this.handleResult(task, result);
	}
	
	protected abstract Task takeTask();
	protected abstract Worker buildWorker(Task task);
	protected abstract void clear();
	
	public void run() {
		if (this.queueManager == null) {
			throw new Spiderman.Exception(getClass().getSimpleName()+" 缺少队列管理器");
		}
		// 要有地方触发这个计时器
		new Thread(() -> this.counter.await()).start();
		
		// 进入正题
		boolean needCheck = true;
		while (this.counter.isWorking()) {
			if (needCheck) {
				boolean isAvai = (threads.getTaskCount() - threads.getCompletedTaskCount()) < threads.getCorePoolSize();
				if (!isAvai) {
					wait(1);
					continue;
				}
				needCheck = false;
			}
			
			final Task task = this.takeTask();
			if (task == null) {
				wait(1);
				continue;
			}
			logger.info("获得任务: "+task);
			final Worker worker = this.buildWorker(task);
			threads.execute(worker);
			needCheck = true;
		}
		logger.debug("退出获取任务的循环...");
		listeners.forEach(l -> l.shouldShutdown());
	}
	
	private void wait(int waitSeconds) {
		try {
			Thread.sleep(waitSeconds*1000L);
		} catch (InterruptedException e) {}
	}
	
}
