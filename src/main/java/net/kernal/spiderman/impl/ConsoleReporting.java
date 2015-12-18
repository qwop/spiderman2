package net.kernal.spiderman.impl;

import java.util.Date;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import net.kernal.spiderman.Downloader.Response;
import net.kernal.spiderman.K;
import net.kernal.spiderman.Parser.ParsedResult;
import net.kernal.spiderman.Reporting;
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
//		System.out.println("[Spiderman][创建任务]"+K.formatNow()+"\r\n  "+newTask.getRequest().getUrl());
	}

	public void reportParsedResult(ParsedResult parsedResult) {
		System.err.println("[Spiderman][解析结果]"+K.formatNow()+"\r\n  "+JSON.toJSONString(parsedResult.first(), SerializerFeature.PrettyFormat));
	}
	
	public void reportStop() {
		long cost = System.currentTimeMillis() - this.startAt;
		System.out.println("[Spiderman][结束]"+K.formatNow()+"\r\n  总共花费时间:"+cost+"ms");
	}

}
