package net.kernal.spiderman.worker.extract;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import net.kernal.spiderman.K;
import net.kernal.spiderman.Properties;
import net.kernal.spiderman.queue.Queue.Element;
import net.kernal.spiderman.worker.Worker;
import net.kernal.spiderman.worker.WorkerManager;
import net.kernal.spiderman.worker.WorkerResult;
import net.kernal.spiderman.worker.download.Downloader;
import net.kernal.spiderman.worker.extract.conf.Field;
import net.kernal.spiderman.worker.extract.conf.Field.ValueFilter;
import net.kernal.spiderman.worker.extract.conf.Model;
import net.kernal.spiderman.worker.extract.conf.Page;
import net.kernal.spiderman.worker.extract.conf.Page.Models;
import net.kernal.spiderman.worker.extract.conf.filter.TrimFilter;

public class ExtractWorker extends Worker {
	
	private ExtractManager manager;
	
	public ExtractWorker(WorkerManager manager) {
		super(manager);
		this.manager = (ExtractManager)manager;
	}
	
	public void work(Element t) {
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
			
			final Map<String, Properties> modelsCtx = new HashMap<String, Properties>();
			// 执行抽取
			extractor.extract(new Extractor.Callback() {
				public void onModelExtracted(ModelEntry entry) {
					String modelName = entry.getModel().getName();
					if (K.isBlank(modelName)) {
						modelName = "no-name";
					} 
					final Properties fields = entry.getProperties();
					modelsCtx.put(modelName, fields);
					final ExtractResult result = new ExtractResult(pageName, modelName, fields);
					manager.done(new WorkerResult(page, task, result));
				}
				public void onFieldExtracted(FieldEntry entry) {
					final Field field = entry.getField();
					final boolean isArray = field.getBoolean("isArray", false);
					final boolean isForNewTask = field.getBoolean("isForNewTask", false);
					final boolean isDistinct = field.getBoolean("isDistinct", false);
					final Collection<?> values = entry.getValues();
					
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
								String nv = ft.filter(new ValueFilter.Context(extractor, modelsCtx, v));
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
