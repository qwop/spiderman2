package net.kernal.spiderman.worker;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Logger;

import net.kernal.spiderman.Spiderman.Counter;
import net.kernal.spiderman.conf.Conf;
import net.kernal.spiderman.queue.TaskQueue;
import net.kernal.spiderman.task.Task;

/**
 * 包工头
 * @author 赖伟威 l.weiwei@163.com 2015-12-31
 *
 */
public class WorkerManager implements Runnable {
	
	public final static Logger logger = Logger.getLogger(WorkerManager.class.getName());
	
	private Conf conf;
	private Counter counter;
	private String name;
	private long waitSeconds;
	private TaskQueue taskQueue;
	private ThreadPoolExecutor threads;
	private Worker.Builder workerBuilder;
	
	public WorkerManager(String name, TaskQueue taskQueue, ThreadPoolExecutor threads, Worker.Builder workerBuilder) {
		this.name = name;
		this.taskQueue = taskQueue;
		this.threads = threads;
		this.workerBuilder = workerBuilder;
	}
	
	public void run() {
		boolean debug = this.conf.getProperties().getBoolean("debug", true);
		boolean checkEnabled = true;
		while (true) {
			if (checkEnabled) {
				// 先检查线程池是否还有线程可用
				boolean isAvailable = (threads.getTaskCount() - threads.getCompletedTaskCount()) < threads.getCorePoolSize();
				if (isAvailable) {
					// 若可用，则下次不需要再检查了
					checkEnabled = false;
				} else {
					// 若不可用，则暂时睡眠一段时间，让出CPU，再重试 (后面看看是否有其他更好的做法)
					try {
						if (debug) {
							logger.info(name+"线程池负载已满，将等待"+waitSeconds+"秒再尝试");
						}
						Thread.sleep(waitSeconds*1000L);
					} catch (InterruptedException e) {}
					continue;
				}
			}
			
			logger.info("从"+name+"队列获取任务...");
			final Task task = taskQueue.poll();
			if (task == null) {
				try {
					if (debug) {
						logger.info("获取"+name+"队列任务超时，将等待"+waitSeconds+"秒再尝试");
					}
					Thread.sleep(waitSeconds*1000L);
				} catch (InterruptedException e) {}
				continue;
			}
			try {
				Worker worker = this.workerBuilder.build(task, conf, counter);
				threads.execute(worker);
				// 每使用一个线程，下次就要重新检查一下线程池
				checkEnabled = true;
			} catch (java.util.concurrent.RejectedExecutionException e) {}
		}
	}
	
	public void shutdown() {
		this.threads.shutdownNow();
	}
	public void setConf(Conf conf) {
		this.conf = conf;
	}
	public void setCounter(Counter counter) {
		this.counter = counter;
	}
	public void setWaitSeconds(long waitSeconds) {
		this.waitSeconds = waitSeconds;
	}
	
}