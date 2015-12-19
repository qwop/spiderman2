package net.kernal.spiderman;

/**
 * 任务管理器
 * @author 赖伟威 l.weiwei@163.com 2015-12-18
 */
public class TaskManager implements TaskQueue {

	/**
	 * 主要任务队列，存放符合目标(Target)的任务
	 */
	private TaskQueue primaryTaskQueue;
	/**
	 * 次要任务队列，存放不符合目标的其他任务
	 */
	private TaskQueue secondaryTaskQueue;
	
	private boolean isPrimary(Task task) {
		return task.getPriority() < 5;
	}
	
	public TaskManager(TaskQueue primaryTaskQueue, TaskQueue secondaryTaskQueue) {
		this.primaryTaskQueue = primaryTaskQueue;
		this.secondaryTaskQueue = secondaryTaskQueue;
	}

	public Task poll() {
		Task task = this.primaryTaskQueue.poll();
		return task != null ? task : this.secondaryTaskQueue.poll();
	}

	public void put(Task task) {
		if (isPrimary(task))
			this.primaryTaskQueue.put(task);
		else 
			this.secondaryTaskQueue.put(task);
	}
	public long size() {
		return this.primaryTaskQueue.size() + this.secondaryTaskQueue.size();
	}
}
