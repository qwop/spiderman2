package net.kernal.spiderman.worker.result;

import net.kernal.spiderman.Counter;
import net.kernal.spiderman.logger.Logger;
import net.kernal.spiderman.queue.QueueManager;
import net.kernal.spiderman.worker.Task;
import net.kernal.spiderman.worker.Worker;
import net.kernal.spiderman.worker.WorkerManager;
import net.kernal.spiderman.worker.WorkerResult;
import net.kernal.spiderman.worker.extract.ExtractManager.ResultHandler;
import net.kernal.spiderman.worker.extract.ExtractResult;

public class ResultManager extends WorkerManager {

	private ResultHandler handler;
	
	public ResultManager(int nWorkers, QueueManager queueManager, Counter counter, Logger logger, ResultHandler handler) {
		super(nWorkers, queueManager, counter, logger);
		this.handler = handler;
	}

	protected void handleResult(Task task, WorkerResult result) {
		// 计数器加1
		if (task instanceof ResultTask) {
			long count = getCounter().plus();
			final ResultTask rtask = (ResultTask)task;
			final ExtractResult extractResult = rtask.getResult();
			getLogger().info("消费了第"+count+"个结果: "+extractResult);
			this.handler.handle(extractResult, count);
		}
	}

	protected Task takeTask() {
		return getQueueManager().getResultQueue().take();
	}

	protected Worker buildWorker() {
		return new Worker(this) {
			public void work(Task task) {
				getManager().done(task, null);
			}
		};
	}

	protected void clear() {
	}

}
