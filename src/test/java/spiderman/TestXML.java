package spiderman;

import java.io.File;

import net.kernal.spiderman.Context;
import net.kernal.spiderman.K;
import net.kernal.spiderman.Spiderman;
import net.kernal.spiderman.conf.Conf;
import net.kernal.spiderman.conf.Target;
import net.kernal.spiderman.conf.XMLConfBuilder;
import net.kernal.spiderman.downloader.Downloader;
import net.kernal.spiderman.parser.TextParser;
import net.kernal.spiderman.parser.TransformParser;
import net.kernal.spiderman.task.Task;

public class TestXML {

	/**
	 * 测试XML文件方式来构建配置对象，这样的好处是可以将那些不需要代码编写的目标配置规则放到XML文件去，减少代码复杂性和冗杂性。
	 * 还可以在代码里添加种子，添加目标，添加属性等等来跟XML文件的配置进行合并。
	 */
	public static void main(String[] args) {
		XMLConfBuilder builder = new XMLConfBuilder(new File("src/main/resources/baidu-search.xml"));//XML配置构建器
		K.readLine(new File("src/main/resources/seeds.txt")).forEach(line -> {
			if (line.startsWith("#")) {
				return;
			}
			builder.addSeed("http://www.baidu.com/s?wd="+K.urlEncode("\""+line+"\""));//百度网页搜索种子
			builder.addSeed("http://news.baidu.com/ns?word="+K.urlEncode("\""+line+"\""));//百度新闻搜索种子
			builder.addSeed("http://zhidao.baidu.com/search?word="+K.urlEncode("\""+line+"\""));//百度知道搜索种子
		});
		Conf conf = builder
			.registerFunction("cleanPageUrl", new TransformParser() {//自定义函数,可在脚本调用
				public Object transform(Object url) {
					// 清理URL,去掉一些杂质,只保留关键词和分页参数，这样就不会重复了
					final String pn = K.findOneByRegex((String)url, "&pn\\=\\d+");
					if (K.isBlank(pn)) {
						return url;
					}
					final Task task = this.modelParser.getTask();
					final Downloader.Request seed = task.getSeed() == null ? task.getRequest() : task.getSeed();
					return seed.getUrl()+pn;
				}
			})
			.addTarget(new Target("网页内容"){//目标
				public void configRules(Rules rules) {
					rules.setPriority(1).addNotContainsRule("baidu");//目标URL规则
				}
				public void configModel(Model model) {
					model.addParser(new TextParser());// 目标解析规则，这里直接用通用的正文抽取器解析
				}
			})
			.set("debug", true)
			.set("duration", "30s")
			.set("mapdb.file", "src/main/resources/mapdb")
			.set("mapdb.deleteFilesAfterClose", true)
			.set("zbus.enabled", false)//是否开启分布式支持
			.set("zbus.serverAddress", "10.8.60.8:15555")//zbus服务器地址
			.set("downloader.primary.threadSize", 1)//下载(主)线程数量
			.set("downloader.secondary.threadSize", 1)//下载(次)线程数量
			.set("parser.primary.threadSize", 1)//解析(主)线程数量
			.set("parser.secondary.threadSize", 1)//解析(次)线程数量
			.set("result.threadSize", 1)//结果处理线程数量
			.build();
		
		new Spiderman(new Context(conf)).go();//别忘记看控制台信息哦，结束之后会有统计信息的,查看关键词"[结束]"(去掉双引号来查找)
	}
	
}
