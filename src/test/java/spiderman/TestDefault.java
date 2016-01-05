package spiderman;

import net.kernal.spiderman.Context;
import net.kernal.spiderman.K;
import net.kernal.spiderman.Properties;
import net.kernal.spiderman.Spiderman;
import net.kernal.spiderman.conf.Conf;
import net.kernal.spiderman.conf.DefaultConfBuilder;
import net.kernal.spiderman.conf.Functions;
import net.kernal.spiderman.conf.Seeds;
import net.kernal.spiderman.conf.Target;
import net.kernal.spiderman.conf.Targets;
import net.kernal.spiderman.parser.HtmlCleanerParser;
import net.kernal.spiderman.parser.TextParser;
import net.kernal.spiderman.parser.TransformParser;

public class TestDefault {
	
	/**
	 * 这个测试代码完全使用Java代码的方式来配置抽取规则，可以看到配置躲起来之后代码不太好看，至少是比较繁杂的。
	 * 另外一个TestXML例子就使用大部分配置通过XML文件加载，小部分用Java代码处理，看起来会好很多。
	 */
	public static void main(String[] args) throws InterruptedException {
		//Conf conf = new BaiduSearchConfBuilder().build();// 复杂的例子
		Conf conf = new DefaultConfBuilder() {
			public void registerFunction(Functions functions) {
				// 添加自定义函数
			}
			public void addSeed(Seeds seeds) {
				// 添加种子
				seeds.add("http://www.baidu.com/s?wd="+K.urlEncode("\"蜘蛛侠\""));
			}
			public void addTarget(Targets targets) {
				// 添加目标
				targets.add(new Target("列表页面URL"){
					public void configRules(Rules rules) {
						rules.addRegexRule("(?=http://www\\.baidu\\.com/s\\?wd\\=).[^&;]*");
					}
					public void configModel(Model model) {
						model.addParser(new HtmlCleanerParser());
						model.addField("pager_url")
							 .addParser(new HtmlCleanerParser.FieldPaser("//div[@id='page']//a[@href]", "href"))
							 .addParser(new TransformParser() {
								public Object transform(Object oldValue) {
									return "http://www.baidu.com"+oldValue;
								}
							  })
							 .asNewTask();
					}
				});
				targets.add(new Target("详情页面URL"){
					public void configRules(Rules rules) {
						rules.addStartsWithRule("http://www.baidu.com/s?wd=");
					}
					public void configModel(Model model) {
						model.addParser(new HtmlCleanerParser());
						model.addField("detail_url")
							 .addParser(new HtmlCleanerParser.FieldPaser("//div[@id='content_left']//div[@class='result c-container ']//h3//a[@href]", "href"))
							 .asNewTask();
					}
				});
				targets.add(new Target("网页内容"){
					public void configRules(Rules rules) {
						rules.setPriority(1).addNotContainsRule("baidu");
					}
					public void configModel(Model model) {
						model.addParser(new TextParser());
					}
				});
			}
			public void addProperty(Properties p) {
				p.put("zbus.enabled", true);//是否开启分布式支持
				p.put("zbus.serverAddress", "10.8.60.8:15555");//zbus服务器地址
//				p.put("zbus.timeout", "10s");
//				p.put("duration", "10s");//持续时间
				p.put("downloader.primary.threadSize", 50);//下载(主)线程数量
				p.put("downloader.secondary.threadSize", 10);//下载(次)线程数量
				p.put("parser.primary.threadSize", 50);//解析(主)线程数量
				p.put("parser.secondary.threadSize", 1);//解析(次)线程数量
				p.put("parsedLimit", 100);//解析网页数量上限，达到后将会自动结束行动
			}
		}.build();
		
		// 开始抓取
		new Spiderman(new Context(conf)).go();
	}
	
}
