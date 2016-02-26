package net.kernal.spiderman.worker;

import java.util.concurrent.CountDownLatch;

import net.kernal.spiderman.logger.ConsoleLogger;
import net.kernal.spiderman.logger.Logger;

/**
 * 我们可爱的工人们.
 * 1. 从任务队列管理器里申领任务
 * 2. 调用子类实现的work方法完成具体工作
 * 3. 完成后继续申领任务，直到经理喊收工啦!
 * @author 赖伟威 l.weiwei@163.com 2016-01-16
 *
 */
public abstract class Worker extends Thread {

	private WorkerManager manager;
	private WorkerResult result;
	private CountDownLatch countDown;
	private boolean stop;
	
	public Worker(WorkerManager manager) {
		this.manager = manager;
		this.countDown = new CountDownLatch(1);
	}
	
	protected Logger getLogger() {
		return manager != null ? manager.getLogger() : new ConsoleLogger(getClass(), Logger.LEVEL_DEBUG);
	}
	
	protected WorkerManager getManager() {
		return this.manager;
	}
	
	protected void setResult(WorkerResult result) {
		this.result = result;
	}
	
	public WorkerResult getResult() {
		return this.result;
	}
	
	public abstract void work(Task task);
	
	public void run() {
		while (true) {
			if (this.stop || this.isInterrupted()) {
				break;
			}
			final Task task;
			try {
				task = manager.takeTask();
			} catch (InterruptedException e) {
				break;
			}
			if (task == null) {
				continue;
			}
			getLogger().info(this.getName() + " 获取任务: " + task.getKey());
			try {
				this.work(task);
			} catch (Throwable e) {
				getLogger().error(this.getName() + " 任务失败: " + task.getKey(), e);
				continue;
			}
			getLogger().info(this.getName() + " 完成任务: " + task.getKey());
		}
		countDown.countDown();
		getLogger().warn("工人["+this.getName() + "]已收工");
	}
	
	/**
	 * 提供给经理调用，收工啦！！！
	 */
	public void await() {
		this.stop = true;
		this.interrupt();
		try {
			this.countDown.await();
		} catch (InterruptedException e) {
		}
	}
	
}
