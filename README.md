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
/** 网易国内新闻采集 */
public class TestGeneral {
	public static void main(String[] args) {
		final String xml = "general-example.xml";
		final Conf conf = new XMLConfBuilder(xml).build();// 通过XMLBuilder构建CONF对象
		final Context ctx = new Context(conf, (task, c) -> {
			final ExtractResult er = task.getResult();
			final String json =  JSON.toJSONString(er.getFields(), true);
			final String fmt = "获取第%s个[page=%s, model=%s, url=%s]结果：\r\n%s";
			System.err.println(String.format(fmt, c, er.getPageName(), er.getModelName(), task.getRequest().getUrl(), json));
		});
		new Spiderman(ctx).go();//启动，别忘记看控制台信息哦，结束之后会有统计信息的
	}
}
```

general-example.xml
```
<?xml version="1.0" encoding="UTF-8"?>
<spiderman name="网易国内新闻采集">
    <property key="duration" value="60s" /><!-- 运行时间 0 表示永久，可以给 {n}s {n}m {n}h {n}d -->
    <property key="logger.level" value="INFO" /><!-- 日志级别 INFO DEBUG WARN ERROR OFF -->
    <property key="worker.download.size" value="10" /><!-- 下载线程数 -->
    <property key="worker.extract.size" value="10" /><!-- 页面抽取线程数 -->
    <property key="worker.result.size" value="10" /><!-- 结果处理线程数 -->
    <property key="queue.element.repeatable" value="false" /><!-- 队列元素是否允许重复，默认允许，若不允许，则使用重复检查器在元素入队列前进行检查 -->
    <property key="queue.checker.bdb.file" value="store/checker" /><!-- 检查器需要用到BDb存储 -->
    <seed url="http://news.163.com/domestic/" /><!-- 写死种子入口的方式 -->
    <extract><!-- 页面抽取规则 -->
        <extractor name="Text" class="net.kernal.spiderman.worker.extract.TextExtractor" isDefault="1" /><!-- 正文抽取器 -->
        <extractor name="Links" class="net.kernal.spiderman.worker.extract.LinksExtractor" /><!-- 链接抽取器 -->
        <page name="新闻内容" isUnique="1">
			<url-match-rule type="regex" value="^http://news\.163\.com/\d+/\d+/\d+/.*\.html#f\=dlist$" />
		</page>
		<page name="更多链接" isUnique="1" extractor="Links">
			<url-match-rule type="regex">
			^http://news\.163\.com/((domestic/)|(special/0001124J/guoneinews_\d+\.html#headList))$
			</url-match-rule>
		</page>
	</extract>
</spiderman>
```

更多例子请参考 
> src/test/java/TestAPI.java
> src/test/java/TestGeneral.java
> src/test/java/TestAdvanced.java
> src/main/resources/*-example.xml

