package net.kernal.spiderman.worker;

import net.kernal.spiderman.Spiderman.Counter;
import net.kernal.spiderman.conf.Conf;
import net.kernal.spiderman.task.ResultTask;

public class ResultWorker extends Worker {

	private ResultTask task;
	
	public ResultWorker(ResultTask task, Conf conf, Counter counter) {
		super(conf, counter);
		this.task = task;
	}

	public void run() {
		conf.getReportings().reportParsedResult(task.getParsedResult());
	}

}
