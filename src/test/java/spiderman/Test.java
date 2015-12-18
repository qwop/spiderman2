package spiderman;

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
import net.kernal.spiderman.impl.XPathParser;

public class Test {

	public static void main(String[] args) throws InterruptedException {
		// 构建配置
		Spiderman.Conf conf = new DefaultConfBuilder(){
			public void addSeed(Seeds seeds) {
				// 加入种子链接
				seeds.add("http://www.baidu.com/s?wd="+K.urlEncode("Spiderman 努力做更好的爬虫"));
			}
			public void addTarget(Targets targets) {
				// 设定目标
				targets.add(
					new Target("baidu-list"){
						public void configRules(Rules rules) {
							rules.addRegexRule("(?=http://www\\.baidu\\.com/s\\?wd\\=).[^&;]*");// URL规则
						}
						public void configModel(Model model) {
							model.addParser(new HtmlCleanerParser());
							model.addField("page_url")
								 .addParser(new XPathParser("//div[@id='page']//a[@href]", "href"))
								 .asNewTask();
						}
					}, 
					new Target("baidu-detail"){
						public void configRules(Rules rules) {
							rules.addStartsWithRule("http://www.baidu.com/s?wd=");// URL规则
						}
						public void configModel(Model model) {
							model.addParser(new HtmlCleanerParser());
							model.addField("target_urls")
								 .addParser(new XPathParser("//div[@id='content_left']//div[@class='result c-container ']//h3//a[@href]", "href"))
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
			public void addProperty(Properties p) {
				p.put("duration", "20s");
				p.put("threadSize", 50);
//				p.put("downloader.limit", 50);
				// 线程：1    网页：50   时间：116739ms
				// 线程：50   网页：50   时间：4411ms
				// 线程：200  网页：50   时间：3542ms   有效 线程池完成406个任务
				// 线程：100  网页：100  时间：6378ms   有效 线程池完成358个任务
				// 线程：500  网页：100  时间：4962ms   有效 线程池完成823个任务
				// 线程：200  网页：200  时间：8562ms   有效
				// 线程：500  网页：200  时间：9817ms   参数搞错，无效，需重新测试
				// 线程：500  网页：400  时间：9396ms   参数搞错，无效，需重新测试
				// 线程：500  网页：500  时间：9294ms   参数搞错，无效，需重新测试
				// 线程：500  网页：800  时间：9627ms   参数搞错，无效，需重新测试
				// 线程：1000 网页：800  时间：9176ms   参数搞错，无效，需重新测试 线程池完成1007个任务，使用线程数量最大191
				// 线程：200  网页：800  时间：9320ms   参数搞错，无效，需重新测试 线程池完成1115个任务，使用线程数量最大189
				// 线程：500  网页：
//				p.put("downloader.redirectsEnabled", true);
//				p.put("downloader.userAgent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36");
			}
		}.build();
		
		new Spiderman(conf).go();
	}
	
}
