package net.kernal.spiderman.queue;

import net.kernal.spiderman.Task;

/**
 * 任务队列接口.
 * @author 赖伟威 l.weiwei@163.com 2015-12-17
 *
 */
public interface TaskQueue {

	/**
	 * 从队列获取任务
	 */
	public Task poll();
	
	/**
	 * 放入任务到队列中
	 * @param task
	 */
	public void put(Task task);
	
	/**
	 * 获取任务队列当前大小(当前待分配任务数量)
	 */
	public long size();
	
}
