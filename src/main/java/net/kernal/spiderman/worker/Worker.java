package net.kernal.spiderman.worker;

import java.util.ArrayList;
import java.util.List;

import net.kernal.spiderman.K;
import net.kernal.spiderman.Spiderman.Counter;
import net.kernal.spiderman.conf.Conf;
import net.kernal.spiderman.conf.Target;
import net.kernal.spiderman.downloader.Downloader;
import net.kernal.spiderman.task.DownloadTask;
import net.kernal.spiderman.task.ParseTask;
import net.kernal.spiderman.task.Task;

/**
 * 工人抽象类
 * 赖伟威 l.weiwei@163.com 2015-12-30
 *
 */
public abstract class Worker implements Runnable {
	/**
	 * 配置对象
	 */
	protected Conf conf;
	/**
	 * 计数器
	 */
	protected Counter counter;

	public Worker(Conf conf, Counter counter) {
		this.conf = conf;
		this.counter = counter;
	}
	
	/**
	 * 将新任务放入下载队列
	 */
	protected void putTheNewTaskToQueue(String httpMethod, String url) {
		if (K.isBlank(url)) {
			return;
		}
		Downloader.Request request = new Downloader.Request(url, httpMethod);
		Task newTask = new DownloadTask(request, 500);
		this.putTheNewTaskToQueue(newTask);
	}
	
	/**
	 * 将新任务放入队列
	 * @param newTask 任务
	 */
	protected void putTheNewTaskToQueue(Task newTask) {
		List<Target> matchedTargets = this.matchingTargets(newTask.getRequest());
		if (K.isNotEmpty(matchedTargets)) {
			Integer p = null;
			for (Target tgt : matchedTargets) {
				int _p = tgt.getRules().getPriority();
				p = p == null ? _p : (_p < p ? _p : p);
			}
			newTask.setPriority(p == null ? 10 : p);
		} else {
			newTask.setPriority(500);
		}
		if (newTask instanceof DownloadTask) {
			if (newTask.isPrimary()) {
				conf.getPrimaryDownloadTaskQueue().put(newTask);
				// 队列计数+1
				counter.primaryDownloadQueuePlus();
			} else {
				conf.getSecondaryDownloadTaskQueue().put(newTask);
				// 队列计数+1
				counter.secondaryDownloadQueuePlus();
			}
			
		} else if (newTask instanceof ParseTask) {
			if (newTask.isPrimary()) {
				conf.getPrimaryParseTaskQueue().put(newTask);
				// 队列计数+1
				counter.primaryParseQueuePlus();
			} else {
				conf.getSecondaryParseTaskQueue().put(newTask);
				// 队列计数+1
				counter.secondaryParseQueuePlus();
			}
		}
		
		// 状态报告: 创建新任务
		conf.getReportings().reportNewTask(newTask);
	}
	
	/**
	 * 找出匹配给定任务的目标配置列表
	 * @param task 需要匹配的任务
	 * @return 符合匹配的目标配置
	 */
	protected List<Target> matchingTargets(final Downloader.Request request) {
		final List<Target> matchedTargets = new ArrayList<Target>();
		conf.getTargets().all().forEach(target -> {
			if (target.matches(request)) {
				matchedTargets.add(target);
			}
		});
		return matchedTargets;
	}
	
}
