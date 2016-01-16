package net.kernal.spiderman.worker;

/**
 * 我们可爱的工人们.
 * 1. 从任务队列管理器里申领任务
 * 2. 调用子类实现的work方法完成具体工作
 * 3. 完成后继续申领任务，直到经理喊收工啦!
 * @author 赖伟威 l.weiwei@163.com 2016-01-16
 *
 */
public abstract class Worker implements Runnable {

	private WorkerManager manager;
	private WorkerResult result;
	private boolean stop;
	
	public Worker(WorkerManager manager) {
		this.manager = manager;
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
		final Thread thread = Thread.currentThread();
		while (true) {
			if (stop || thread.isInterrupted()) {
				manager.getLogger().info(thread.getName() + " 退出获取任务的循环");
				break;
			}
			final Task task = manager.takeTask();
			if (task == null) {
				continue;
			}
			manager.getLogger().info(thread.getName() + " 获取任务: " + task);
			this.work(task);
			manager.getLogger().info(thread.getName() + " 结束任务: " + task);
		}
	}
	
	/**
	 * 提供给经理调用，收工啦！！！
	 */
	public void stop() {
		this.stop = true;
		manager.getLogger().info(Thread.currentThread().getName() + " 收工");
	}
	
}
