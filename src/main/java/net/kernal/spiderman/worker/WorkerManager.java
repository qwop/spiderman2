package net.kernal.spiderman.worker;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Logger;

import net.kernal.spiderman.Context;
import net.kernal.spiderman.Counter;
import net.kernal.spiderman.K;
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
	
	private Context context;
	
	private String name;
	private long waitSeconds;
	private TaskQueue taskQueue;
	private ThreadPoolExecutor threads;
	private Worker.Builder workerBuilder;
	
	public WorkerManager(String name, TaskQueue taskQueue, ThreadPoolExecutor threads, Worker.Builder workerBuilder, Context context) {
		this.name = name;
		this.taskQueue = taskQueue;
		this.threads = threads;
		this.workerBuilder = workerBuilder;
		this.context = context;
		this.waitSeconds = K.convertToSeconds(context.getConf().getProperties().getString("waitSeconds", "1s")).longValue();
		context.getCounter().setPrimaryDownloadPool(new Counter.Threads(threads));
	}
	
	public void run() {
		boolean debug = this.context.getConf().getProperties().getBoolean("debug", true);
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
			
			if (debug) {
				logger.info("从"+name+"队列获取任务...");
			}
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
				Worker worker = this.workerBuilder.build(task, context);
				threads.execute(worker);
				// 每使用一个线程，下次就要重新检查一下线程池
				checkEnabled = true;
			} catch (java.util.concurrent.RejectedExecutionException e) {}
		}
	}
	
	public void shutdown() {
		this.threads.shutdownNow();
	}
	
	public static class Builder {
		private Context context;
		public Builder(Context context) {
			this.context = context;
		}
		
		private WorkerManager build(String name, int threadSize, TaskQueue queue, Worker.Builder workerBuilder) {
			WorkerManager mgr = null;
			if (threadSize > 0) {
				final ThreadPoolExecutor threads = (ThreadPoolExecutor) Executors.newFixedThreadPool(threadSize);
				mgr = new WorkerManager(name, queue, threads, workerBuilder, context);
			}
			return mgr;
		}
		
		// 构建负责主下载的包工头
		public WorkerManager buildPrimaryDownloadWorkerManager(int threadSize) {
			final TaskQueue queue = context.getQueueManager().getPrimaryDownloadTaskQueue();
			return this.build("下载(主)", threadSize, queue, new Worker.Builder(){
				public Worker build(Task task, Context context) {
					return new DownloadWorker((DownloadTask)task, context);
				}
			});
		}
		
		// 构建负责次下载的包工头
		public WorkerManager buildSecondaryDownloadWorkerManager(int threadSize) {
			final TaskQueue queue = context.getQueueManager().getSecondaryDownloadTaskQueue();
			return this.build("下载(次)", threadSize, queue, new Worker.Builder(){
				public Worker build(Task task, Context context) {
					return new DownloadWorker((DownloadTask)task, context);
				}
			});
		}
		
		// 负责主解析的包工头
		public WorkerManager buildPrimaryParseWorkerManager(int threadSize) {
			final TaskQueue queue = context.getQueueManager().getPrimaryParseTaskQueue();
			return this.build("解析(主)", threadSize, queue, new Worker.Builder(){
				public Worker build(Task task, Context context) {
					return new ParseWorker((ParseTask)task, context);
				}
			});
		}
		
		// 负责次解析的包工头
		public WorkerManager buildSecondaryParseWorkerManager(int threadSize) {
			final TaskQueue queue = context.getQueueManager().getSecondaryParseTaskQueue();
			return this.build("解析(次)", threadSize, queue, new Worker.Builder(){
				public Worker build(Task task, Context context) {
					return new ParseWorker((ParseTask)task, context);
				}
			});
		}
		
		// 负责结果处理的包工头
		public WorkerManager buildResultWorkerManager(int threadSize) {
			final TaskQueue queue = context.getQueueManager().getResultTaskQueue();
			return this.build("结果", threadSize, queue, new Worker.Builder(){
				public Worker build(Task task, Context context) {
					return new ResultWorker((ResultTask)task, context);
				}
			});
		}
	}
	
}