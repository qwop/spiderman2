package net.kernal.spiderman.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import net.kernal.spiderman.Downloader.Response;
import net.kernal.spiderman.K;
import net.kernal.spiderman.parser.Parser;
import net.kernal.spiderman.Reporting;
import net.kernal.spiderman.Spiderman;
import net.kernal.spiderman.Task;

public class ConsoleReporting implements Reporting {

	private long startAt;
	
	public void reportStart() {
		this.startAt = System.currentTimeMillis();
		System.out.println("[Spiderman][开始]"+K.formatNow());
	}
	
	public void reportDownload(Response response) {
		StringBuilder sb = new StringBuilder("[Spiderman][下载网页]:"+K.formatNow()+"\r\n  ["+response.getStatusCode()+"]"+response.getRequest().getUrl());
		if (K.isNotBlank(response.getLocation())) {
			sb.append("\r\n  redirect ").append(response.getLocation());
		}
		System.out.println(sb.toString());
	}

	public void reportNewTask(Task newTask) {
		System.out.println("[Spiderman][创建任务]"+K.formatNow()+"\r\n  "+newTask.getRequest().getUrl());
	}

//	static List<String> KEYWORDS = new ArrayList<String>();
//	static {
//		List<String> keywords = K.readLine(new File("src/main/resources/keywords.txt"));
//		if (keywords != null) {
//			for (String kw : keywords) {
//				KEYWORDS.addAll(Arrays.asList(kw.split(" ")));
//			}
//		}
//	}
	
	public void reportParsedResult(Parser.ParsedResult parsedResult) {
//		Parser.Model model = (Parser.Model)parsedResult.first();
//		String title = model.getString("title");
//		String text = model.getString("text");
//		String content = title+"\r\n"+text;
//		List<String> ws = new ArrayList<String>();
//		for (String kw : KEYWORDS) {
//			if (content.matches("[\\s\\S]*"+kw+"[\\s\\S]*")){
//				ws.add(kw);
//			}
//		}
//		if (!ws.isEmpty()) {
			System.err.println("[Spiderman][解析结果]"+K.formatNow()+"\r\n  "+JSON.toJSONString(parsedResult.first(), SerializerFeature.PrettyFormat));
//		}
	}
	
	public void reportStop(Spiderman.Counter counter, int poolSize, int activeCount, long completedTaskCount) {
		long cost = System.currentTimeMillis() - this.startAt;
		String fmt = "\r\n\r\n\r\n[Spiderman][结束]%s\r\n  总共花费时间:%sms \r\n  线程池: 总数(%s) 运行中(%s) 已完成(%s) \r\n  计数器: 已下载(%s) 目标(%s) 当前队列(%s) ";
		System.out.println(String.format(fmt, K.formatNow(), cost, poolSize, activeCount, completedTaskCount, counter.getCountDownload().get(), counter.getCountTarget().get(), counter.getCountQueue().get()));
	}

}
