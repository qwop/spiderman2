package net.kernal.spiderman;

import java.util.ArrayList;
import java.util.List;

import net.kernal.spiderman.Spiderman.Conf;
import net.kernal.spiderman.Spiderman.Counter;
import net.kernal.spiderman.downloader.Downloader;
import net.kernal.spiderman.parser.Parser.ParsedResult;

/**
 * 蜘蛛侠的蜘蛛大军
 * @author 赖伟威 l.weiwei@163.com 2015-12-10
 *
 */
public class Spider implements Runnable {

	/**
	 * 配置对象
	 */
	private Conf conf;
	/**
	 * 任务
	 */
	private Task task;
	/**
	 * 计数器
	 */
	private Counter counter;

	public Spider(Conf conf, Task task, Counter counter) {
		this.conf = conf;
		this.task = task;
		this.counter = counter;
	}
	
	public void run() {
		// 从任务包里拿到请求对象
		final Downloader.Request request = this.task.getRequest();
		// 将请求丢给下载器进行下载
		final Downloader.Response response = this.conf.getDownloader().download(request);
		if (response == null) {
			return;
		}
		// 下载计数＋1
		this.counter.downloadPlus();
		// 处理重定向
		final int statusCode = response.getStatusCode();
		final String location = response.getLocation();
		if (K.isNotBlank(location) && K.isIn(statusCode, 301, 302)) {
			this.putTheNewTaskToQueue(K.HTTP_GET, location);
			return ;
		}
		if (response.getBody() == null || response.getBody().length == 0) {
			return;
		}
		
		// 处理响应体文本编码问题
		String charsetName = K.getCharsetName(response.getCharset());
		if (K.isBlank(charsetName)) {
			charsetName = getCharsetFromBodyStr(response.getBodyStr());
		}
		if (K.isNotBlank(charsetName)) {
			response.setCharset(charsetName);
		}
		// 获取响应体文本内容
		final String bodyStr = K.byteToString(response.getBody(), charsetName); 
		// 若内容为空，结束任务
		if (K.isBlank(bodyStr)) {
			return;
		}
		response.setBodyStr(bodyStr);
		// 报告下载事件
		this.conf.getReportings().reportDownload(response);
		// 匹配目标
		final List<Target> matchedTargets = this.matchingTargets(request);
		// 解析目标
		for (Target target : matchedTargets) {
			final ParsedResult parsedResult = target.parse(response);
			if (parsedResult != null && K.isNotEmpty(parsedResult.all())) {
				// 解析结果计数＋1
				this.counter.parsedPlus();
				// 报告解析结果
				this.conf.getReportings().reportParsedResult(parsedResult);
				// 若字段配置为新任务来使用，则将它的解析结果(URL地址列表)作为新任务放入队列
				if (K.isNotEmpty(parsedResult.getUrlsForNewTask())) {
					for (String[] arr : parsedResult.getUrlsForNewTask()) {
						putTheNewTaskToQueue(arr[0], arr[1]);
					}
				}
			}
		}
	}
	
	/**
	 * 找出匹配给定任务的目标配置列表
	 * @param task 需要匹配的任务
	 * @return 符合匹配的目标配置
	 */
	private List<Target> matchingTargets(final Downloader.Request request) {
		final List<Target> matchedTargets = new ArrayList<Target>();
		K.foreach(conf.getTargets().all(), new K.ForeachCallback<Target>() {
			public void each(int i, Target target) {
				if (target.matches(request)) {
					matchedTargets.add(target);
					// 目标计数＋1
					counter.targetPlus();
				}
			}
		});
		return matchedTargets;
	}
	
	/**
	 * 将新任务放入队列
	 * @param newTask 任务
	 */
	public void putTheNewTaskToQueue(String httpMethod, String url) {
		if (K.isBlank(url)) {
			return;
		}
		
		Task newTask = null;
		Downloader.Request request = new Downloader.Request(url, httpMethod);
		List<Target> matchedTargets = this.matchingTargets(request);
		if (K.isNotEmpty(matchedTargets)) {
			Integer p = null;
			for (Target tgt : matchedTargets) {
				int _p = tgt.getRules().getPriority();
				p = p == null ? _p : (_p < p ? _p : p);
			}
			newTask = new Task(request, p == null ? 10 : p);
		} else {
			newTask = new Task(request, 500);
		}
		conf.getTaskQueue().put(newTask);
		// 队列计数+1
		counter.queuePlus();
		// 状态报告: 创建新任务
		conf.getReportings().reportNewTask(newTask);
	}
	
	private String getCharsetFromBodyStr(final String bodyStr) {
		if (K.isBlank(bodyStr)) {
			return null;
		}
		
		String html = bodyStr.trim().toLowerCase();
		String s1 = K.findOneByRegex(html, "(?=<meta ).*charset=.[^/]*");
		if (K.isBlank(s1)) {
			return null;
		}
		
		String s2 = K.findOneByRegex(s1, "(?=charset\\=).[^;/\"']*");
		if (K.isBlank(s2)) {
			return null;
		}
		String charsetName = s2.replace("charset=", "");
		return K.getCharsetName(charsetName);
	}
}
