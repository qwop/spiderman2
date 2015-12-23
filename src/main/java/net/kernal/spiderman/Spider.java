package net.kernal.spiderman;

import java.util.ArrayList;
import java.util.List;

import net.kernal.spiderman.parser.FieldParser;
import net.kernal.spiderman.parser.ModelParser;
import net.kernal.spiderman.parser.Parser;
import net.kernal.spiderman.parser.Parser.ParsedResult;

/**
 * 蜘蛛侠的蜘蛛大军，战斗力极强
 * @author 赖伟威 l.weiwei@163.com 2015-12-10
 *
 */
public class Spider implements Runnable {

	private Spiderman.Conf conf;
	private Task task;
	private Spiderman.Counter counter;
	
	public Spider(Spiderman.Conf conf, Task task, Spiderman.Counter counter) {
		this.conf = conf;
		this.task = task;
		this.counter = counter;
	}
	
	public void run() {
		final Downloader.Request request = this.task.getRequest();
		// 从网页下载内容
		final Downloader.Response response = this.conf.getDownloader().download(request);
		if (response == null) {
			return;
		}
		this.conf.getReportings().reportDownload(response);
		// 处理重定向任务
		final int statusCode = response.getStatusCode();
		final String location = response.getLocation();
		if (K.isNotBlank(location) && K.isIn(statusCode, 301, 302)) {
			// 重定向，创建新任务
			this.putTheNewTaskToQueue(location, K.HTTP_GET);
			return ;
		}
		if (200 != statusCode) {
			return;
		}
		response.setHtml(K.byteToString(response.getBody(), response.getCharset()));
		if (K.isBlank(response.getHtml())) {
			return;
		}
		
		this.counter.addDownload();
		
		// 匹配目标
		final List<Target> matchedTargets = this.matchingTargets(request);
		// 解析目标
		K.foreach(matchedTargets, new K.ForeachCallback<Target>() {
			public void each(int i, final Target target) {
				Target.Model model = target.getModel();
				final ModelParser modelParser = model.getParser();
				if (modelParser == null) {
					throw new RuntimeException("请为Target["+target.getName()+"].Model设置解析器，比如model.addParser");
				}
				modelParser.setResponse(response);
				counter.addTarget();
				final ParsedResult modelParsedResult = modelParser.parse();
				if (modelParsedResult == null || K.isEmpty(modelParsedResult.all())) {
					return;
				}
				ParsedResult parsedResult = modelParsedResult;
				List<Target.Model.Field> fields = model.getFields();
				if (K.isNotEmpty(fields)) {
					final List<Parser.Model> parsedModels = new ArrayList<Parser.Model>();
					
					if (model.isArray()) {
						for (Object _parsed : modelParsedResult.all()) {
							Parser.Model _parsedModel = new Parser.Model();
							for (Target.Model.Field f : fields) {
								ParsedResult _parsedResult = new ParsedResult(_parsed);
								for (FieldParser p : f.getParsers()) {
									p.setModelParser(modelParser);
									p.setPrevParserResult(_parsedResult);
									_parsedResult = p.parse();
								}
								if (_parsedResult == null || K.isEmpty(_parsedResult.all())) {
									continue;
								}
								_parsedModel.put(f.getName(), _parsedResult.all().toArray(new Object[]{}));
							}
							parsedModels.add(_parsedModel);
						}
					} else {
						Parser.Model _parsedModel = new Parser.Model();
						for (Target.Model.Field f : fields) {
							ParsedResult _parsedResult = new ParsedResult(modelParsedResult.first());
							for (FieldParser p : f.getParsers()) {
								p.setModelParser(modelParser);
								p.setPrevParserResult(_parsedResult);
								_parsedResult = p.parse();
							}
							if (_parsedResult == null || K.isEmpty(_parsedResult.all())) {
								continue;
							}
							
							if (f.isForNewTask()) {
								List<String> urls = new ArrayList<String>();
//								if (f.isArray()) {
									for (Object val : _parsedResult.all()) {
										urls.add((String)val);
									}
//								} else {
//									urls.add((String)_parsedResult.first());
//								}
								final String httpMethod = f.getProperties().getString("httpMethod", K.HTTP_GET);
								for(String url : urls) {
									// 创建任务进入队列
									putTheNewTaskToQueue(url, httpMethod);
								}
							}
							_parsedModel.put(f.getName(), _parsedResult.all().toArray(new Object[]{}));
						}
						parsedModels.add(_parsedModel);
					}
					if (K.isNotEmpty(parsedModels)) {
						parsedResult = ParsedResult.fromList(parsedModels);
					}
				}
				
				// 报告解析结果
				if (parsedResult != null && K.isNotEmpty(parsedResult.all())) {
					conf.getReportings().reportParsedResult(parsedResult);
				}
			}
		});
	}
	
	/**
	 * 找出匹配给定任务的目标配置列表
	 * @param task 需要匹配的任务
	 * @return 符合匹配的目标配置
	 */
	public List<Target> matchingTargets(final Downloader.Request request) {
		final List<Target> matchedTargets = new ArrayList<Target>();
		K.foreach(conf.getTargets().getAll(), new K.ForeachCallback<Target>() {
			public void each(int i, Target target) {
				if (target.matches(request)) {
					matchedTargets.add(target);
				}
			}
		});
		return matchedTargets;
	}
	
	/**
	 * 将新任务放入队列
	 * @param newTask 任务
	 */
	public void putTheNewTaskToQueue(String url, String httpMethod) {
		if (K.isALLNull(url)) return;
//		String resolveUrl = K.resolveUrl(task.getRequest().getUrl(), url);
		String resolveUrl = url;
		Task newTask = null;
		Downloader.Request request = new Downloader.Request(resolveUrl, httpMethod);
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
		conf.getReportings().reportNewTask(newTask);
	}
	
}
