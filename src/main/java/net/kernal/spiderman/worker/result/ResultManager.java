package net.kernal.spiderman.worker.result;

import net.kernal.spiderman.Counter;
import net.kernal.spiderman.logger.Logger;
import net.kernal.spiderman.queue.QueueManager;
import net.kernal.spiderman.queue.Queue.Element;
import net.kernal.spiderman.worker.Worker;
import net.kernal.spiderman.worker.WorkerManager;
import net.kernal.spiderman.worker.WorkerResult;
import net.kernal.spiderman.worker.extract.ExtractManager.ResultHandler;

public class ResultManager extends WorkerManager {

	private ResultHandler handler;
	
	public ResultManager(int nWorkers, QueueManager queueManager, Counter counter, Logger logger, ResultHandler handler) {
		super(nWorkers, queueManager, counter, logger);
		this.handler = handler;
	}

	protected void handleResult(WorkerResult wr) {
		// 计数器加1
		final Counter counter = getCounter();
		final long count = counter.plus();
		final ResultTask rtask = (ResultTask)wr.getTask();
		getLogger().info("消费了第"+count+"个结果: "+rtask.getResult());
		if (this.handler != null) {
			this.handler.handle(rtask, counter);
		}
	}

	protected Element takeTask() {
		return getQueueManager().getResultQueue().take();
	}

	protected Worker buildWorker() {
		return new Worker(this) {
			public void work(Element task) {
				getManager().done(new WorkerResult(null, (ResultTask)task, null));
			}
		};
	}

	protected void clear() {
	}

}
