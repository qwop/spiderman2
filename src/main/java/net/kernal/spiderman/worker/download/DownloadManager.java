package net.kernal.spiderman.worker.download;

import net.kernal.spiderman.Counter;
import net.kernal.spiderman.Spiderman;
import net.kernal.spiderman.logger.Logger;
import net.kernal.spiderman.queue.QueueManager;
import net.kernal.spiderman.worker.Task;
import net.kernal.spiderman.worker.Worker;
import net.kernal.spiderman.worker.WorkerManager;
import net.kernal.spiderman.worker.WorkerResult;
import net.kernal.spiderman.worker.extract.ExtractTask;

/**
 * 下载工人驱动器 
 */
public class DownloadManager extends WorkerManager {
	
	private Downloader downloader;
	private long delay;
	
	public DownloadManager(int nWorkers, QueueManager queueManager, Counter counter, Logger logger, Downloader downloader, long delay) {
		super(nWorkers, queueManager, counter, logger);
		this.downloader = downloader;
		this.delay = delay;
	}
	
	/**
	 * 从队列里获取任务
	 */
	protected Task takeTask() throws InterruptedException {
		return (Task)getQueueManager().getDownloadQueue().take();
	}

	/**
	 * 构建工人对象
	 */
	protected Worker buildWorker() {
		return new DownloadWorker(this, downloader, delay);
	}
	
	/**
	 * 处理结果
	 */
	protected void handleResult(WorkerResult wr) {
		final Object result = wr.getResult();
		final Task task = wr.getTask();
		if (!(result instanceof Downloader.Response)) {
			throw new Spiderman.Exception("只接受Downloader.Response类型的结果");
		}
		// 计数器加1
		long count = getCounter().plus();
		Downloader.Response response = (Downloader.Response)result;
		// 放入解析队列
		getQueueManager().append(new ExtractTask((DownloadTask)task, response));
		getLogger().info("下载了第"+count+"个网页: "+response.getBodyStr());
	}

	protected void clear() {
	}

}
