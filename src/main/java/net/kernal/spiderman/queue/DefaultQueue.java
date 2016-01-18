package net.kernal.spiderman.queue;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedTransferQueue;

import net.kernal.spiderman.logger.Logger;

/**
 * 默认的任务队列实现
 * @author 赖伟威 l.weiwei@163.com 2015-12-10
 *
 */
public class DefaultQueue<T> implements Queue<T> {
	
	private Logger logger;
	private BlockingQueue<T> queue;
	
	public DefaultQueue(int capacity, Logger logger) {
		this.logger = logger;
		if (capacity <= 0) {
			queue = new LinkedTransferQueue<T>();
			logger.debug(getClass().getName()+" 使用无边界LinkedTransferQueue");
		} else {
			queue = new ArrayBlockingQueue<T>(capacity);
			logger.debug(getClass().getName()+" 使用有边界ArrayBlockingQueue");
		}
	}
	
	public T take() {
		try {
			return this.queue.take();
		} catch (InterruptedException e) {
		}	
		return null;
	}
	
	public void append(T t) {
		try {
			this.queue.put(t);
		} catch (InterruptedException e) {
		}
	}

	public void clear() {
		logger.debug(getClass().getName()+" 队列元素剩余数:"+queue.size());
		queue.clear();
	}
	
}