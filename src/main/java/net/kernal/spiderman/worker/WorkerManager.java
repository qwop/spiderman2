package net.kernal.spiderman.worker;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Logger;

import net.kernal.spiderman.Spiderman.Counter;
import net.kernal.spiderman.conf.Conf;
import net.kernal.spiderman.queue.TaskQueue;
import net.kernal.spiderman.task.DownloadTask;
import net.kernal.spiderman.task.ParseTask;
import net.kernal.spiderman.task.ResultTask;
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
	private ThreadPoolExecutor workers;
	
	public WorkerManager(String name, TaskQueue taskQueue, ThreadPoolExecutor workers) {
		this.name = name;
		this.taskQueue = taskQueue;
		this.workers = workers;
	}
	
	public void shutdown() {
		this.workers.shutdownNow();
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
	
	public void run() {
		while (true) {
			while(true) {
				int coreSize = workers.getCorePoolSize();
				long completedTaskCount = workers.getCompletedTaskCount();
				long taskCount = workers.getTaskCount();
				long runningCount = taskCount - completedTaskCount;
				if (runningCount < coreSize) {
					break;
				}
				try {
					logger.info(name+"线程池负载已满，将等待"+waitSeconds+"秒再尝试");
					Thread.sleep(waitSeconds*1000L);
				} catch (InterruptedException e) {}
			}
			while (true) {
				Task task = taskQueue.poll();
				if (task == null) {
					try {
						logger.info(name+"队列已无任务可分配，将等待"+waitSeconds+"秒再尝试");
						Thread.sleep(waitSeconds*1000L);
					} catch (InterruptedException e) {}
					continue;
				}
				
				try {
					if (task instanceof DownloadTask) {
						workers.execute(new DownloadWorker((DownloadTask)task, conf, counter));
						break;
					} else if (task instanceof ParseTask) {
						workers.execute(new ParseWorker((ParseTask)task, conf, counter));
						break;
					} else if (task instanceof ResultTask) {
						workers.execute(new ResultWorker((ResultTask)task, conf, counter));
						break;
					}
				} catch (java.util.concurrent.RejectedExecutionException e) {}
			}
		}
	}
}