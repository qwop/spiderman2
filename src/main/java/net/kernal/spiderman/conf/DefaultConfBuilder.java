package net.kernal.spiderman.conf;

import net.kernal.spiderman.Properties;
import net.kernal.spiderman.Spiderman;
import net.kernal.spiderman.worker.extract.conf.Page;

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
	}
	
	/**
	 * 留给客户端程序去添加参数
	 * @param properties
	 */
	public abstract void configParams(Properties params);
	/**
	 * 留给客户端程序去添加种子
	 * @param seeds
	 */
	public abstract void configSeeds(Conf.Seeds seeds);
	/**
	 * 留给客户端程序去添加需要抽取的页面
	 * @param targets
	 */
	public abstract void configPages(Conf.Pages pages);
	
	/**
	 * 构建Spiderman.Conf对象
	 */
	public Conf build() {
		this.configParams(conf.getParams());
		this.configSeeds(conf.getSeeds());
		this.configPages(conf.getPages());
		for (Page page : conf.getPages().all()) {
			page.config(page.getRules(), page.getModels());
			if (page.getExtractorBuilder() == null) {
				throw new Spiderman.Exception("页面[name="+page.getName()+"]缺少可以构建抽取器的对象，请设置一个 models.setExtractorBuilder");
			}
		}
		
		return conf;
	}

}
