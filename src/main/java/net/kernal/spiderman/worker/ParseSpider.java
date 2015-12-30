package net.kernal.spiderman.worker;

import java.util.List;

import net.kernal.spiderman.K;
import net.kernal.spiderman.Spiderman.Conf;
import net.kernal.spiderman.Spiderman.Counter;
import net.kernal.spiderman.Target;
import net.kernal.spiderman.downloader.Downloader;
import net.kernal.spiderman.parser.Parser.ParsedResult;
import net.kernal.spiderman.task.ParseTask;

/**
 * 负责解析的蜘蛛工人
 * @author 赖伟威 l.weiwei@163.com 2015-12-10
 * @author 赖伟威 l.weiwei@163.com 2015-12-30
 *
 */
public class ParseSpider extends Worker {

	private ParseTask task;
	
	public ParseSpider(ParseTask task, Conf conf, Counter counter) {
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
			// 报告解析结果
			this.conf.getReportings().reportParsedResult(parsedResult);
			// 解析结果计数＋1
			this.counter.parsedPlus();
			// 若字段配置为新任务来使用，则将它的解析结果(URL地址列表)作为新任务放入队列
			if (K.isNotEmpty(parsedResult.getUrlsForNewTask())) {
				parsedResult.getUrlsForNewTask().forEach(arr -> {
					super.putTheNewTaskToQueue(arr[0], arr[1]);
				});
			}
		});
	}
	
}
