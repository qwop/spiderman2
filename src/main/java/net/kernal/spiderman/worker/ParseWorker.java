package net.kernal.spiderman.worker;

import java.util.List;

import net.kernal.spiderman.Context;
import net.kernal.spiderman.K;
import net.kernal.spiderman.conf.Target;
import net.kernal.spiderman.downloader.Downloader;
import net.kernal.spiderman.parser.Parser.ParsedResult;
import net.kernal.spiderman.task.DownloadTask;
import net.kernal.spiderman.task.ParseTask;
import net.kernal.spiderman.task.ResultTask;

/**
 * 负责解析的蜘蛛工人
 * @author 赖伟威 l.weiwei@163.com 2015-12-10
 * @author 赖伟威 l.weiwei@163.com 2015-12-30
 *
 */
public class ParseWorker extends Worker {

	private ParseTask task;
	
	public ParseWorker(ParseTask task, Context context) {
		super(context);
		this.task = task;
	}
	
	public void run() {
		final Downloader.Response response = task.getResponse();
		final Downloader.Request request = response.getRequest();
		final Downloader.Request seed = task.getSeed() == null ? request : task.getSeed();
		// 匹配目标
		final List<Target> matchedTargets = super.matchingTargets(request);
		// 解析目标
		matchedTargets.forEach(target -> {
			final ParsedResult parsedResult = target.parse(task);
			if (parsedResult == null || K.isEmpty(parsedResult.all())) {
				return;
			}
			// 将解析结果放入队列
			this.context.getQueueManager().put(new ResultTask(seed, parsedResult, request));
						
			// 若字段配置为新任务来使用，则将它的解析结果(URL地址列表)作为新任务放入队列
			if (K.isNotEmpty(parsedResult.getUrlsForNewTask())) {
				parsedResult.getUrlsForNewTask().forEach(url -> {
					Downloader.Request req = new Downloader.Request(url);
					Target.Rules.KeyGenerator gen = target.getRules().getKeyGenerator();
					final Object key = gen != null ? gen.gen(task, url) : null;
					final DownloadTask dTask = new DownloadTask(seed, req, key, 500);
					this.context.getQueueManager().put(dTask);
				});
			}
		});
	}
	
}
