package net.kernal.spiderman.worker;

import net.kernal.spiderman.Context;
import net.kernal.spiderman.task.ResultTask;

public class ResultWorker extends Worker {

	private ResultTask task;
	
	public ResultWorker(ResultTask task, Context context) {
		super(context);
		this.task = task;
	}

	public void run() {
		context.getConf().getReportings().reportParsedResult(task, task.getParsedResult());
	}

}
