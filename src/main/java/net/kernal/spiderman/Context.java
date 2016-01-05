package net.kernal.spiderman;

import java.util.concurrent.CountDownLatch;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import net.kernal.spiderman.conf.Conf;
import net.kernal.spiderman.downloader.DefaultDownloader;
import net.kernal.spiderman.downloader.Downloader;
import net.kernal.spiderman.parser.JavaInvoker;
import net.kernal.spiderman.parser.TransformParser;
import net.kernal.spiderman.queue.QueueManager;

public class Context {

	private Conf conf;
	
	private Counter counter;
	private Downloader downloader;
	private QueueManager queueManager;
	private ScriptEngine scriptEngine;
	
	public Context(Conf conf) {
		if (conf.getSeeds().isEmpty()) 
			throw new RuntimeException("少年,请添加一个种子来让蜘蛛侠行动起来!参考：conf.addSeed");
		if (conf.getTargets().isEmpty()) 
			throw new RuntimeException("少年,请添加一个目标来让蜘蛛侠行动起来!参考：conf.addTarget");
		
		this.conf = conf;
		
		// build counter
		this.counter = new Counter();
		int parsedLimit = conf.getProperties().getInt("parsedLimit", 0);
		if (parsedLimit > 0) {
			this.counter.setCountDown(new CountDownLatch(parsedLimit));
		} 
		
		// build queue manager
		this.queueManager = new QueueManager(conf, counter);
		
		// build downloader
		this.downloader = new DefaultDownloader(conf.getProperties());
		// build script engine
		final String engineName = conf.getProperties().getString("scriptEngine", "nashorn");
		this.scriptEngine = new ScriptEngineManager().getEngineByName(engineName);
		if (this.scriptEngine != null) {
			final JavaInvoker javaInvoker = new JavaInvoker(conf.getFunctions());
			this.conf.getTargets().all().forEach((target) -> {
				target.getModel().getFields().forEach((field) -> {
					//加trim处理
					field.addParser(new TransformParser() {
						public Object transform(Object oldValue) {
							return oldValue == null ? oldValue : oldValue instanceof String ? ((String)oldValue).trim() : oldValue;
						}
					});
					field.getParsers().forEach((parser) -> {
						parser.setScriptEngine(this.scriptEngine);
						parser.setJavaInvoker(javaInvoker);
					});
				});
			});
		}
	}

	public Conf getConf() {
		return conf;
	}

	public Counter getCounter() {
		return counter;
	}

	public Downloader getDownloader() {
		return downloader;
	}

	public QueueManager getQueueManager() {
		return queueManager;
	}

	public ScriptEngine getScriptEngine() {
		return scriptEngine;
	}
	
}
