package spiderman;

import java.util.ArrayList;
import java.util.List;

import net.kernal.spiderman.Downloader;
import net.kernal.spiderman.K;
import net.kernal.spiderman.Properties;
import net.kernal.spiderman.Spiderman;
import net.kernal.spiderman.Spiderman.Seeds;
import net.kernal.spiderman.Spiderman.Targets;
import net.kernal.spiderman.Target;
import net.kernal.spiderman.impl.DefaultConfBuilder;
import net.kernal.spiderman.impl.HtmlCleanerParser;
import net.kernal.spiderman.impl.TextParser;
import net.kernal.spiderman.parser.CustomParser;

public class Test {
	
	public static void main(String[] args) throws InterruptedException {
		// 构建配置
		Spiderman.Conf conf = new DefaultConfBuilder(){
			// 添加种子链接
			public void addSeed(final Seeds seeds) {
				String encodedName = K.urlEncode("\"蜘蛛侠\"");
				seeds.add("http://www.baidu.com/s?wd="+encodedName);
				seeds.add("http://news.baidu.com/ns?word="+encodedName+"&pn=0&ct=1&tn=news&ie=utf-8&bt=0&et=0");
			}
			// 添加抽取目标
			public void addTarget(Targets targets) {
				targets.add(
					new Target("baidu-list"){
						public void configRules(Rules rules) {
							rules.addRegexRule("(?=http://www\\.baidu\\.com/s\\?wd\\=).[^&;]*");// URL规则
						}
						public void configModel(Model model) {
							model.addParser(new HtmlCleanerParser());
							model.addField("page_url")
								 .addParser(new HtmlCleanerParser.FieldPaser("//div[@id='page']//a[@href]", "href"))
								 .addParser(new CustomParser() {
									public ParsedResult parse(ParsedResult prevResult) {
										List<String> newValues = new ArrayList<String>();
										for (Object url : prevResult.all()) {
											newValues.add(K.resolveUrl("http://www.baidu.com", (String)url));
										}
										return ParsedResult.fromList(newValues);
									}
								}).asNewTask();
						}
					}, 
					new Target("baidu-detail"){
						public void configRules(Rules rules) {
							rules.addStartsWithRule("http://www.baidu.com/s?wd=");// URL规则
						}
						public void configModel(Model model) {
							model.addParser(new HtmlCleanerParser());
							model.addField("target_urls")
								 .addParser(new HtmlCleanerParser.FieldPaser("//div[@id='content_left']//div[@class='result c-container ']//h3//a[@href]", "href"))
								 .asNewTask();
						}
					}
					,
					new Target("baidu-news-list"){
						public void configRules(Rules rules) {
							rules.addRegexRule("http://news\\.baidu\\.com/ns\\?word\\=.*&pn\\=0&ct\\=1&tn\\=news&ie\\=utf\\-8&bt\\=0&et\\=0");// URL规则
						}
						public void configModel(Model model) {
							model.addParser(new HtmlCleanerParser());
							model.addField("page_url")
								 .addParser(new HtmlCleanerParser.FieldPaser("//p[@id='page']//a[@href]", "href"))
								 .addParser(new CustomParser() {
									 public ParsedResult parse(ParsedResult prevResult) {
										 List<String> newValues = new ArrayList<String>();
										 for (Object url : prevResult.all()) {
											 newValues.add(K.resolveUrl("http://news.baidu.com", (String)url));
										 }
										 return ParsedResult.fromList(newValues);
									  }
								  })
								 .asNewTask();
						}
					}, 
					new Target("baidu-news-detail"){
						public void configRules(Rules rules) {
							rules.addRegexRule("http://news\\.baidu\\.com/ns\\?word\\=.*&pn\\=\\d+&ct\\=1&tn\\=news&ie\\=utf-8&bt\\=0&et\\=0");// URL规则
						}
						public void configModel(Model model) {
							model.addParser(new HtmlCleanerParser());
							model.addField("target_urls")
								 .addParser(new HtmlCleanerParser.FieldPaser("//div[@id='content_left']//div[@class='result']//h3//a[@href]", "href"))
								 .asNewTask();
						}
					}
					, 
					new Target("baidu-page"){
						public void configRules(Rules rules) {
							rules.setPriority(1).add(new Target.Rule(){
								public boolean matches(Downloader.Request request) {
									return !request.getUrl().contains("baidu");
								}
							});
						}
						public void configModel(Model model) {
							model.addParser(new TextParser());
						}
					}
				);
			}
			// 添加配置属性
			public void addProperty(Properties p) {
//				p.put("duration", "30s");// 持续时间，到达该时间后蜘蛛侠会停止活动回到你的身边哦～
				p.put("threadSize", 5);//线程池大小
				p.put("downloader.limit", 50);//下载网页数量限制，达到该数量后蜘蛛侠会停止活动回到你的身边哦～
			}
		}.build();
		
		new Spiderman(conf).go();
	}
	
}
