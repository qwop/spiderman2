package spiderman;

import net.kernal.spiderman.K;
import net.kernal.spiderman.Properties;
import net.kernal.spiderman.Spiderman;
import net.kernal.spiderman.conf.Conf;
import net.kernal.spiderman.conf.DefaultConfBuilder;
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
				p.put("duration", "20s");//持续时间，超过该时间后将会自动结束，会忽略解析数量多少
				p.put("downloader.threadSize", 20);//下载线程数量
				p.put("parser.threadSize", 10);//解析线程数量
//				p.put("parsedLimit", 10);//解析网页数量上限，达到后将会自动结束行动
			}
		}.build();
		
		// 开始抓取
		new Spiderman(conf).go();
	}
	
}
