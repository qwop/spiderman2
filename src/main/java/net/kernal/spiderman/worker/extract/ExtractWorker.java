package net.kernal.spiderman.worker.extract;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import net.kernal.spiderman.K;
import net.kernal.spiderman.worker.Task;
import net.kernal.spiderman.worker.Worker;
import net.kernal.spiderman.worker.WorkerManager;
import net.kernal.spiderman.worker.WorkerResult;
import net.kernal.spiderman.worker.download.Downloader;
import net.kernal.spiderman.worker.extract.conf.Field;
import net.kernal.spiderman.worker.extract.conf.Model;
import net.kernal.spiderman.worker.extract.conf.Page;
import net.kernal.spiderman.worker.extract.conf.Page.Models;
import net.kernal.spiderman.worker.extract.conf.filter.ScriptableFilter;
import net.kernal.spiderman.worker.extract.conf.filter.TrimFilter;

public class ExtractWorker extends Worker {
	
	private ExtractManager manager;
	
	public ExtractWorker(WorkerManager manager) {
		super(manager);
		this.manager = (ExtractManager)manager;
	}
	
	public void work(Task t) {
		final ExtractTask task = (ExtractTask)t;
		final List<Page> pages = manager.getPages();
		final Downloader.Response response = task.getResponse();
		final Downloader.Request request = response.getRequest();
		
		pages.parallelStream()//多线程来做
		.filter(page -> page.matches(request))//过滤，只要能匹配request的page
		.forEach(page -> {
			final Extractor.Builder builder = page.getExtractorBuilder();
			final String pageName = page.getName();
			final Models models = page.getModels();
			final Extractor extractor = builder.build(task, pageName, models.all().toArray(new Model[]{}));
			
			// 执行抽取
			extractor.extract(new Extractor.Callback() {
				public void onModelExtracted(ModelEntry entry) {
					final ExtractResult result = new ExtractResult(pageName, entry.getModel().getName(), entry.getProperties(), request);
					manager.done(new WorkerResult(page, task, result));
				}
				public void onFieldExtracted(FieldEntry entry) {
					final Field field = entry.getField();
					final boolean isArray = field.getBoolean("isArray", false);
					final boolean isForNewTask = field.getBoolean("isForNewTask", false);
					final boolean isDistinct = field.getBoolean("isDistinct", false);
					final List<Object> values = entry.getValues();
					
					// 处理过滤器
					final List<Field.ValueFilter> filters = new ArrayList<Field.ValueFilter>();
					filters.add(new TrimFilter());// 添加一个trim过滤器
					filters.addAll(field.getFilters());// 添加多个子元素配置的过滤器
					
					// 执行过滤, 得到过滤后的新值
					Collection<String> newValues = (isForNewTask||isDistinct) ? new HashSet<String>(values.size()) : new ArrayList<String>(values.size());
					values.parallelStream()
						.filter(v -> v instanceof String).map(v -> (String)v)// 保证值是String类型
						.forEach(v -> {
							AtomicReference<String> v2 = new AtomicReference<String>(v);
							filters.forEach(ft -> {
								if (ft instanceof ScriptableFilter) {// 只要支持脚本的过滤器
									((ScriptableFilter)ft).setScriptEngine(manager.getScriptEngine());//给它们设置脚本引擎
								}
								String nv = ft.filter(extractor, v2.get());
								if (K.isNotBlank(nv)) {
									v2.set(nv);// 将上一个结果作为下一个参数过滤
								}
							});
							newValues.add(v2.get());
						});
					
					// 新URL入队列
					if (isForNewTask) {
						newValues.parallelStream()
							.filter(url -> !request.getUrl().equals(url))
							.map(url -> new Downloader.Request(url))
							.forEach(req -> manager.done(new WorkerResult(page, task, req))); 
					}
					
					// 设置结果
					entry.setData(isArray ? newValues.stream().collect(Collectors.toList()) : newValues.stream().findFirst().get());
				}
			});
		});
	}

}
