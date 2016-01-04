package net.kernal.spiderman.worker;

import net.kernal.spiderman.K;
import net.kernal.spiderman.Spiderman.Counter;
import net.kernal.spiderman.conf.Conf;
import net.kernal.spiderman.task.DuplicateCheckTask;

public class DuplicateCheckWorker extends Worker {
	
	private DuplicateCheckTask task = null;
	
	public DuplicateCheckWorker(DuplicateCheckTask task, Conf conf, Counter counter) {
		super(conf, counter);
		this.task = task;
	}

	public void run() {
		final String httpMethod = task.getRequest().getMethod();
		final String url = task.getRequest().getUrl();
		final String value = httpMethod+"#"+url;
		final String key = K.md5(value);
		final boolean flag = conf.getDb().contains("urls", key);
		if (!flag) {
			conf.getDb().put("urls", key, (byte)0);
			super.putTheNewTaskToDownloadQueue(task.getDownloadTask());
		}
		
		conf.getReportings().reportDuplicateCheck(key, flag, task.getRequest());
	}

}
