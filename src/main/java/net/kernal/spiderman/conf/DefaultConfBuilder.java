package net.kernal.spiderman.conf;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import net.kernal.spiderman.Properties;
import net.kernal.spiderman.downloader.DefaultDownloader;
import net.kernal.spiderman.queue.DefaultTaskQueue;
import net.kernal.spiderman.reporting.ConsoleReporting;
import net.kernal.spiderman.task.TaskManager;

/**
 * 默认的配置构建器
 * @author 赖伟威 l.weiwei@163.com 2015-12-01
 *
 */
public abstract class DefaultConfBuilder implements Conf.Builder {

	protected Conf conf;
	public DefaultConfBuilder() {
		super();
		conf = new Conf();
		conf.setDownloadTaskQueue(new TaskManager(new DefaultTaskQueue(), new DefaultTaskQueue()))
			.setParseTaskQueue(new TaskManager(new DefaultTaskQueue(), new DefaultTaskQueue()))
			.setDownloader(new DefaultDownloader(conf.getProperties()))
			.addReporting(new ConsoleReporting());
	}
	/**
	 * 留给客户端程序去添加属性
	 * @param properties
	 */
	public abstract void addProperty(Properties properties);
	/**
	 * 留给客户端程序去添加种子
	 * @param seeds
	 */
	public abstract void addSeed(Seeds seeds);
	/**
	 * 留给客户端程序去添加目标
	 * @param targets
	 */
	public abstract void addTarget(Targets targets);
	
	/**
	 * 构建Spiderman.Conf对象
	 */
	public Conf build() {
		this.addProperty(conf.getProperties());
		final String engineName = conf.getProperties().getString("scriptEngine", "nashorn");
		final ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName(engineName);
		this.conf.setScriptEngine(scriptEngine);
		
		this.addSeed(conf.getSeeds());
		this.addTarget(conf.getTargets());
		for (Target target : conf.getTargets().all()) {
			target.configModel(target.getModel());
			target.configRules(target.getRules());
		}
		return conf;
	}

}
