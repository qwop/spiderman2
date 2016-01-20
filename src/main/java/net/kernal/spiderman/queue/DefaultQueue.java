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
public class DefaultQueue extends CheckableQueue {
	
	private Logger logger;
	private BlockingQueue<Element> queue;
	
	public DefaultQueue(Checker checker, int capacity, Logger logger) {
		super(checker);
		this.logger = logger;
		if (capacity <= 0) {
			queue = new LinkedTransferQueue<Element>();
			logger.debug(getClass().getName()+" 使用无边界LinkedEransferQueue");
		} else {
			queue = new ArrayBlockingQueue<Element>(capacity);
			logger.debug(getClass().getName()+" 使用有边界ArrayBlockingQueue");
		}
	}
	
	public Element take() {
		try {
			return this.queue.take();
		} catch (InterruptedException e) {
		}	
		return null;
	}
	
	public void appendChecked(Element t) {
		try {
			this.queue.put(t);
		} catch (InterruptedException e) {
		}
	}

	public void clear() {
		logger.debug(getClass().getName()+" 队列元素剩余数:"+queue.size());
		queue.clear();
		Checker checker = getChecker();
		if (checker != null && checker instanceof RepeatableChecker) {
			((RepeatableChecker)checker).clear();
		}
	}
	
}