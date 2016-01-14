#Spiderman2
```
简单的说，这是一个网页爬虫工具，专门对网页内容进行抓取和解析
```
- 性能
- 架构简洁
- 易用
- 分布式
- 插件
- UI

要求：
- Java8或以上

快速开始
```
Conf conf = new DefaultConfBuilder() {
	public void configPages(Pages pages) {
		pages.add(new Page("最后结果") {
			public void config(UrlMatchRules rules, Models models) { 
				this.setExtractorBuilder(TextExtractor.builder());
				rules.addNegativeContainsRule("baidu"); 
			}
		});
		pages.add(new Page("百度网页搜索") {
			public void config(UrlMatchRules rules, Models models) {
				this.setExtractorBuilder(HtmlCleanerExtractor.builder());
				rules.addRegexRule("(?=http://www\\.baidu\\.com/s\\?wd\\=).[^&]*(&pn\\=\\d+)?");
				Model model = models.addModel("demo");
				model.addField("详情URL")
					.set("xpath", "//div[@id='content_left']//div[@class='result c-container ']//h3//a[@href]")
					.set("attribute", "href")
					.set("isArray", true)
					.set("isForNewTask", true);
				model.addField("分页URL")
				.set("xpath", "//div[@id='page']//a[@href]")
				.set("attr", "href")
				.set("isArray", true)
				.set("isDistinct", true)
				.set("isForNewTask", true)
				.addFilter((e, v) -> {
					final String pn = K.findOneByRegex(v, "&pn\\=\\d+");
					return K.isBlank(pn) ? v : e.getTask().getSeed().getUrl()+pn;
				});
			}
		});
	}
	public void configSeeds(Seeds seeds) {
		seeds.add(new Seed("http://www.baidu.com/s?wd="+K.urlEncode("\"蜘蛛侠\"")));
	}
	public void configParams(Properties params) {
		params.put("logger.level", Logger.LEVEL_DEBUG);
		params.put("duration", "30s");
		params.put("worker.downloader.size", 5);
		params.put("worker.extractor.size", 5);
	}
}.build();

// 启动蜘蛛侠
Context ctx = new Context(conf, (result, count) -> {
	// handle the extract result
	System.err.println("解析到第"+count+"个:\r\n"+JSON.toJSONString(result, true));
});
new Spiderman(ctx).go();
```
也可以使用配置文件
```
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
```
spiderman.conf.xml
```
<?xml version="1.0" encoding="UTF-8"?>
<spiderman>
    <!-- 引入脚本文件 -->
    <script src="src/main/resources/lib.js" />
    
    <!-- 选项配置 -->
    <property key="duration" value="30s" />
    <property key="logger.level" value="2" />
    <property key="worker.downloader.size" value="5" />
    <property key="worker.extractor.size" value="5" />
    
    <!-- 种子入口 -->
    <seed name="Spiderman">http://www.baidu.com/s?wd=Spiderman</seed>
    
    <!-- 页面抽取规则 -->
	<extract-page name="百度网页搜索">
		<url-match-rule type="regex"><![CDATA[(?=http://www\.baidu\.com/s\?wd\=).[^&]*(&pn\=\d+)?]]></url-match-rule>
		<model>
			<field name="分页URL" isForNewTask="true" isArray="true" xpath="//div[@id='page']//a[@href]" attr="href" filter="resetPageUrl" />
			<field name="详情URl" isForNewTask="true" isArray="true" xpath="//div[@id='content_left']//div[@class='result c-container ']//h3//a[@href]" attr="href" /> 
		</model>
	</extract-page>
	<extract-page name="百度新闻搜索">
		<url-match-rule type="regex"><![CDATA[http://news\.baidu\.com/ns\?word\=.[^&]*(&pn\=\d+)?]]></url-match-rule>
		<model>
			<field name="分页URL" isForNewTask="true" isArray="true" xpath="//p[@id='page']//a[@href]" attr="href" filter="resetPageUrl" />
			<field name="详情URl" isForNewTask="true" isArray="true" xpath="//div[@id='content_left']//div[@class='result']//h3//a[@href]" attr="href" /> 
		</model>
	</extract-page>
	<extract-page name="百度知道搜索">
		<url-match-rule type="regex"><![CDATA[http://zhidao\.baidu\.com/search\?word\=.[^&]*(&pn\=\d+)?]]></url-match-rule>
		<model>
			<field name="分页URL" isForNewTask="true" isArray="true" xpath="//div[@class='pager']//a[@href]" attr="href" filter="resetPageUrl" />
			<field name="详情URl" isForNewTask="true" isArray="true" xpath="//div[@class='list']//dl//dt//a[@href]" attr="href" /> 
		</model>
	</extract-page>
	<extract-page name="百度知道内容">
		<url-match-rule type="startsWith" value="http://zhidao.baidu.com/question/" />
		<model>
			<field name="title" xpath="//h1[@accuse='qTitle']//span/text()" />
			<field name="question" xpath="//pre[@class='line mt-5 q-content']/text()" />
			<field name="answers" isArray="true" xpath="//div[@class='line content']/text()">
				<filter type="script">$this.replace('分享','').replace('评论','').replace('|','')</filter>
			</field>
			<field name="bestAnswer" xpath="//div[@class='wgt-quality mod-shadow']//div[@class='quality-content-detail content']/text()" />
		</model>
	</extract-page>
</spiderman>
```
lib.js
```
/* 脚本内置一个参数：$this 表示上一个解析器解析后的结果 */
var hello = function(name) {
	return "hello, " + name;
}
var addPrefix = function(prefix) {
	return prefix+$this;
}
```