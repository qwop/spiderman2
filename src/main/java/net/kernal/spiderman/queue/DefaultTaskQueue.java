package net.kernal.spiderman.queue;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import net.kernal.spiderman.worker.Task;

/**
 * 默认的任务队列实现
 * @author 赖伟威 l.weiwei@163.com 2015-12-10
 *
 */
public class DefaultTaskQueue implements TaskQueue {
	
	private BlockingQueue<Task> queue;
	
	public DefaultTaskQueue(int capacity) {
		if (capacity <= 0) {
			queue = new LinkedBlockingQueue<Task>(5000);
		} else {
			queue = new ArrayBlockingQueue<Task>(capacity);
		}
	}
	
	public Task take() {
		try {
			return this.queue.take();
		} catch (InterruptedException e) {
		}	
		return null;
	}
	
	public void append(Task task) {
		try {
			this.queue.add(task);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public void clear() {
		queue.clear();
	}
	
}