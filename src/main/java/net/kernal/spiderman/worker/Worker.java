package net.kernal.spiderman.worker;

import java.util.ArrayList;
import java.util.List;

import net.kernal.spiderman.Context;
import net.kernal.spiderman.conf.Target;
import net.kernal.spiderman.downloader.Downloader;
import net.kernal.spiderman.task.Task;

/**
 * 工人抽象类
 * 赖伟威 l.weiwei@163.com 2015-12-30
 *
 */
public abstract class Worker implements Runnable {
	
	protected Context context;

	public Worker(Context context) {
		this.context = context;
	}
	/**
	 * 找出匹配给定任务的目标配置列表
	 * @param task 需要匹配的任务
	 * @return 符合匹配的目标配置
	 */
	protected List<Target> matchingTargets(final Downloader.Request request) {
		final List<Target> matchedTargets = new ArrayList<Target>();
		context.getConf().getTargets().all().forEach(target -> {
			if (target.matches(request)) {
				matchedTargets.add(target);
			}
		});
		return matchedTargets;
	}
	public static interface Builder {
		public Worker build(Task task, Context context);
	}
	
}
