package net.kernal.spiderman.worker.result;

import net.kernal.spiderman.worker.Task;
import net.kernal.spiderman.worker.Worker;
import net.kernal.spiderman.worker.WorkerManager;
import net.kernal.spiderman.worker.WorkerResult;
/**
 * 我们可爱的工人们.
 * 1. 从任务队列管理器里申领任务
 * 2. 调用子类实现的work方法完成具体工作
 * 3. 完成后继续申领任务，直到经理喊收工啦!
 * <Short overview of features> 
 * <Features detail> 
 * 
 * @author		qwop
 * @date 		May 21, 2017 
 * @version		[The version number, May 21, 2017] 
 * @see			[Related classes/methods] 
 * @since		[Products/Module version]
 */
public class ResultWorker extends Worker {

	public ResultWorker(WorkerManager manager) {
		super(manager);
	}

	public void work(Task task) {
		getManager().done(new WorkerResult(null, (ResultTask)task, null));
	}

}
