package net.kernal.spiderman.client;

import java.io.File;

import net.kernal.spiderman.Context;
import net.kernal.spiderman.Properties;
import net.kernal.spiderman.Spiderman;
import net.kernal.spiderman.conf.Conf;
import net.kernal.spiderman.conf.XMLConfBuilder;

/**
 * 启动类
 * @author 赖伟威 l.weiwei@163.com 2016-01-20
 *
 */
public class Bootstrap {

	/** 以XML文件方式来构建配置对象，这样的好处是可以将那些不需要代码编写的配置规则放到XML去，减少代码处理。*/
	public static void main(String[] args) {
		final Properties params = Properties.from(args);// 将参数里的 -k1 v1 -k2 v2 转成 map
		final String xml = params.getString("-conf", "spiderman-bootstrap.xml");// 获得XML配置文件路径
		final Conf conf = new XMLConfBuilder(new File(xml)).build();// 通过XMLBuilder构建CONF对象
		conf.bindObjectForScript("$seeds", conf.getSeeds());// 绑定种子集合对象给脚本用来添加种子用
		new Spiderman(new Context(conf)).go();//启动，别忘记看控制台信息哦，结束之后会有统计信息的
	}
	
}
