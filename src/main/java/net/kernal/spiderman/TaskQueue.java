package net.kernal.spiderman;

/**
 * 任务队列接口.
 * @author 赖伟威 l.weiwei@163.com 2015-12-17
 *
 */
public interface TaskQueue {

	/**
	 * 从队列获取任务
	 * @return
	 */
	public Task poll();
	
	/**
	 * 放入任务到队列中
	 * @param task
	 * @return
	 */
	public void put(Task task);
	
}
