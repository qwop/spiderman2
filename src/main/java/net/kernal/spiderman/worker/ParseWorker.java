package net.kernal.spiderman.worker;

import java.util.List;

import net.kernal.spiderman.K;
import net.kernal.spiderman.Spiderman.Counter;
import net.kernal.spiderman.conf.Conf;
import net.kernal.spiderman.conf.Target;
import net.kernal.spiderman.downloader.Downloader;
import net.kernal.spiderman.parser.Parser.ParsedResult;
import net.kernal.spiderman.task.DownloadTask;
import net.kernal.spiderman.task.DuplicateCheckTask;
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
	
	public ParseWorker(ParseTask task, Conf conf, Counter counter) {
		super(conf, counter);
		this.task = task;
	}
	
	public void run() {
		final Downloader.Response response = task.getResponse();
		final Downloader.Request request = response.getRequest();
		// 匹配目标
		final List<Target> matchedTargets = super.matchingTargets(request);
		// 解析目标
		matchedTargets.forEach(target -> {
			final ParsedResult parsedResult = target.parse(response);
			if (parsedResult == null || K.isEmpty(parsedResult.all())) {
				return;
			}
			// 将解析结果放入队列
			conf.getResultTaskQueue().put(new ResultTask(parsedResult, request));
						
			// 解析结果计数＋1
			if (task.isPrimary()) {
				this.counter.primaryParsedPlus();
			} else {
				this.counter.secondaryParsedPlus();
			}
						
			// 若字段配置为新任务来使用，则将它的解析结果(URL地址列表)作为新任务放入队列
			if (K.isNotEmpty(parsedResult.getUrlsForNewTask())) {
				parsedResult.getUrlsForNewTask().forEach(arr -> {
					final String httpMethod = arr[0];
					final String url = arr[1];
					final DownloadTask dTask = new DownloadTask(new Downloader.Request(url, httpMethod), 500);
					final DuplicateCheckTask task = new DuplicateCheckTask(dTask);
					super.putTheNewTaskToDuplicateCheckQueue(task);
				});
			}
		});
	}
	
}
