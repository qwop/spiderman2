package net.kernal.spiderman;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import net.kernal.spiderman.conf.Conf;
import net.kernal.spiderman.conf.Conf.Seeds;
import net.kernal.spiderman.logger.ConsoleLogger;
import net.kernal.spiderman.logger.Logger;
import net.kernal.spiderman.queue.QueueManager;
import net.kernal.spiderman.worker.WorkerManager;
import net.kernal.spiderman.worker.download.DownloadManager;
import net.kernal.spiderman.worker.download.Downloader;
import net.kernal.spiderman.worker.download.HttpClientDownloader;
import net.kernal.spiderman.worker.extract.ExtractManager;
import net.kernal.spiderman.worker.extract.ExtractManager.ResultHandler;
import net.kernal.spiderman.worker.extract.conf.Page;
import net.kernal.spiderman.worker.extract.conf.filter.ScriptableFilter;
import net.kernal.spiderman.worker.result.ResultManager;

public class Context {
	
	private Logger logger = null;
	
	private Conf conf;
	private QueueManager queueManager;
	private List<WorkerManager> managers;
	private ScriptEngine scriptEngine;
	
	public Context(Conf conf) {
		this(conf, null);
	}
	
	public Context(Conf conf, ResultHandler rh) {
		final List<Page> pages = conf.getPages().all();
		final Properties params = conf.getParams();
		if (pages.isEmpty()) 
			throw new Spiderman.Exception("少年,请添加一个页面来让蜘蛛侠行动起来!参考：conf.addPage");
		final String engineName = params.getString("scriptEngine", "nashorn");
		scriptEngine = new ScriptEngineManager().getEngineByName(engineName);
		if (scriptEngine == null) 
			throw new Spiderman.Exception("无法获取脚本引擎对象[name="+engineName+"]");
		this.conf = conf;
		// process script
		this.processScript();
		
		// build manager
		this.managers = new ArrayList<WorkerManager>();
		final byte level;
		final String levelStr = params.getString("logger.level");
		if (K.isNotBlank(levelStr)) {
			level = Logger.getLevel(levelStr);
		} else {
			level = params.getByte("logger.level", Logger.LEVEL_INFO);
		}
		this.logger = new ConsoleLogger(Context.class, level);
		
		// 构建队列管理器
		queueManager = new QueueManager(params, new ConsoleLogger(QueueManager.class, level));
		// 构建下载管理器
		final boolean enabled1 = params.getBoolean("worker.download.enabled", true);
		if (enabled1) {
			final Downloader downloader = new HttpClientDownloader(params);
			final int limit = params.getInt("worker.download.result.limit", 0);
			final Counter counter = new Counter(limit, 0);
			final int size = params.getInt("worker.download.size", 1);
			final Logger consoleLogger = new ConsoleLogger(DownloadManager.class, level);
			final DownloadManager downloadManager = new DownloadManager(size, queueManager, counter, consoleLogger, downloader);
			logger.debug("构建下载管理器");
			this.addManager(downloadManager);
		}
		
		// 构建解析管理器
		final boolean enabled2 = params.getBoolean("worker.extract.enabled", true);
		if (enabled2) {
			final int limit = params.getInt("worker.extract.result.limit", 0);
			final Counter counter = new Counter(limit, 0);
			final int size = params.getInt("worker.extract.size", 1);
			final Logger consoleLogger = new ConsoleLogger(ExtractManager.class, level);
			final ExtractManager extractManager = new ExtractManager(size, queueManager, counter, consoleLogger, pages);
			logger.debug("构建解析管理器");
			this.addManager(extractManager);
		}
		
		// 构建结果处理管理器
		final boolean enabled3 = params.getBoolean("worker.result.enabled", true);
		if (enabled3) {
			ResultHandler handler = rh;
			if (handler == null) {
				final String resultHandlerClassName = params.getString("worker.result.handler");
				if (K.isNotBlank(resultHandlerClassName)) {
					Class<ResultHandler> resultHandlerClass = K.loadClass(resultHandlerClassName);
					if (resultHandlerClass == null) {
						throw new Spiderman.Exception("ResultHandler[class="+resultHandlerClassName+"]不存在");
					}
					try {
						handler = resultHandlerClass.newInstance();
					} catch (InstantiationException | IllegalAccessException e) {
						throw new Spiderman.Exception("实例化ResultHandler[class="+resultHandlerClassName+"]失败", e);
					}
				}
			}
			
			if (handler != null) {
				handler.init(this);
			}
			
			final int limit = params.getInt("worker.result.limit", 0);
			final Counter counter = new Counter(limit, 0);
			final int size = params.getInt("worker.result.size", 1);
			final Logger consoleLogger = new ConsoleLogger(ResultManager.class, level);
			final ResultManager resultManager = new ResultManager(size, queueManager, counter, consoleLogger, handler);
			logger.debug("构建结果管理器");
			this.addManager(resultManager);
		}
	}
	
	public Collection<WorkerManager> getManagers() {
		return Collections.unmodifiableCollection(this.managers);
	}
	
	public Context addManager(WorkerManager manager) {
		this.managers.add(manager);
		return this;
	}
	
	public Properties getParams() {
		return this.conf.getParams();
	}
	
	public Seeds getSeeds() {
		return this.conf.getSeeds();
	}
	
	public QueueManager getQueueManager() {
		return this.queueManager;
	}
	
	public Logger getLogger() {
		return this.logger;
	}
	
	public void shutdown() {
		this.queueManager.shutdown();
		this.managers.clear();
		logger.debug("退出...");
	}
	
	private void processScript() {
		// 若配置了script脚本，执行它
		final Map<String, Object> bindings = conf.getBindings();
		final String script = conf.getScript();
		if (K.isNotBlank(script)) {
			try {
				final Bindings bind = new SimpleBindings(bindings);
				scriptEngine.eval(script, bind);
			} catch (ScriptException e) {
				throw new Spiderman.Exception("执行脚本错误", e);
			}
		}
		// 设置脚本引擎
		conf.getPages().all().forEach(page -> {
			page.getModels().all().forEach(p -> {
				p.getFields().forEach(f -> {
					f.getFilters().stream()
						.filter(ft -> ft instanceof ScriptableFilter)
						.map(ft -> (ScriptableFilter)ft)
						.forEach(ft -> {
							ft.setBindings(bindings);
							ft.setScriptEngine(scriptEngine);
						});
				});
			});
		});
	}
	
	public static void main(String[] args) throws ScriptException {
		ScriptEngine e = new ScriptEngineManager().getEngineByName("nashorn");
		Bindings bind = new SimpleBindings();
		Conf conf = new Conf();
		bind.put("$seeds", conf.getSeeds());
		final String script = "var kws = Java.type('net.kernal.spiderman.K').readLine('src/main/resources/keywords.txt'); for(var i=0; i<kws.length; i++) { $seeds.add(kws[i]); }";
		Object v = e.eval(script, bind);
		System.out.println("v->"+v);
		System.out.println("seeds->"+conf.getSeeds().all());
	}
	
}
