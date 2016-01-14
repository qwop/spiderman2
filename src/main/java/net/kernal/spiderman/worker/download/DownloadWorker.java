package net.kernal.spiderman.worker.download;

import net.kernal.spiderman.K;
import net.kernal.spiderman.Spiderman;
import net.kernal.spiderman.worker.Worker;
import net.kernal.spiderman.worker.WorkerManager;

public class DownloadWorker extends Worker {

	private Downloader downloader;
	
	public DownloadWorker(WorkerManager manager, DownloadTask task, Downloader downloader) {
		super(manager, task);
		this.downloader = downloader;
	}
	
	public void run() {
		if (this.downloader == null) {
			throw new Spiderman.Exception("缺少下载器");
		}
		if (this.getTask() == null) {
			throw new Spiderman.Exception("缺少任务对象");
		}
		
		final DownloadTask task = (DownloadTask)this.getTask();
		final Downloader.Request request = task.getRequest();
		final Downloader.Response response = this.downloader.download(request);
		if (response == null) {
			return;
		}
		// 处理重定向
		final int statusCode = response.getStatusCode();
		final String location = response.getLocation();
		if (K.isNotBlank(location) && K.isIn(statusCode, 301, 302)) {
			// 告诉经理完成任务，并将结果传递过去
			Downloader.Request newRequest = new Downloader.Request(location);
			this.getManager().done(task, newRequest);
			return ;
		}
		if (response.getBody() == null || response.getBody().length == 0) {
			return;
		}
		// 处理响应体文本编码问题
		String charsetName = K.getCharsetName(response.getCharset());
		if (K.isBlank(charsetName)) {
			charsetName = getCharsetFromBodyStr(response.getBodyStr());
		}
		if (K.isNotBlank(charsetName)) {
			response.setCharset(charsetName);
		}
		// 获取响应体文本内容
		final String bodyStr = K.byteToString(response.getBody(), charsetName); 
		// 若内容为空，结束任务
		if (K.isBlank(bodyStr)) {
			return;
		}
		response.setBodyStr(bodyStr);
		
		// 告诉经理完成任务，并将结果传递过去
		this.getManager().done(task, response);
	}
	
	private String getCharsetFromBodyStr(final String bodyStr) {
		if (K.isBlank(bodyStr)) {
			return null;
		}
		
		String html = bodyStr.trim().toLowerCase();
		String s1 = K.findOneByRegex(html, "(?=<meta ).*charset=.[^/]*");
		if (K.isBlank(s1)) {
			return null;
		}
		
		String s2 = K.findOneByRegex(s1, "(?=charset\\=).[^;/\"']*");
		if (K.isBlank(s2)) {
			return null;
		}
		String charsetName = s2.replace("charset=", "");
		return K.getCharsetName(charsetName);
	}
	
}
