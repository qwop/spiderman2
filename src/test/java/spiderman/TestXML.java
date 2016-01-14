package spiderman;

import java.io.File;

import com.alibaba.fastjson.JSON;

import net.kernal.spiderman.Context;
import net.kernal.spiderman.K;
import net.kernal.spiderman.Spiderman;
import net.kernal.spiderman.conf.Conf;
import net.kernal.spiderman.conf.XMLConfBuilder;
import net.kernal.spiderman.worker.extract.TextExtractor;
import net.kernal.spiderman.worker.extract.conf.Page;

public class TestXML {

	/**
	 * 测试XML文件方式来构建配置对象，这样的好处是可以将那些不需要代码编写的目标配置规则放到XML文件去，减少代码复杂性和冗杂性。
	 * 还可以在代码里添加种子，添加目标，添加属性等等来跟XML文件的配置进行合并。
	 */
	public static void main(String[] args) {
		final String kw = "蜘蛛侠";
		final Conf conf = 
				new XMLConfBuilder(new File("src/main/resources/spiderman.conf.xml"))//XML配置构建器
				.addSeed(kw, "http://www.baidu.com/s?wd="+K.urlEncode("\""+kw+"\""))//百度网页搜索种子
				.addSeed(kw, "http://news.baidu.com/ns?word="+K.urlEncode("\""+kw+"\""))//百度新闻搜索种子
				.addSeed(kw, "http://zhidao.baidu.com/search?word="+K.urlEncode("\""+kw+"\""))//百度知道搜索种子
				.addPage(new Page("网页内容"){//目标
					public void config(UrlMatchRules rules, Models models) {
						setExtractorBuilder(TextExtractor.builder());//设置抽取器(解析器)
						rules.addNegativeContainsRule("baidu");//目标URL规则
					}
				})
				.build();
		
		Context ctx = new Context(conf, (r, c) -> {
			System.err.println("抓取到第"+c+"个结果：\r\n"+JSON.toJSONString(r, true));
		});
		
		new Spiderman(ctx).go();//别忘记看控制台信息哦，结束之后会有统计信息的
	}
	
}
