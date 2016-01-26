package net.kernal.spiderman.worker.download;

import net.kernal.spiderman.worker.Task;

public class DownloadTask extends Task {

	private static final long serialVersionUID = 6126003860229810350L;
	
	public DownloadTask(Downloader.Request request) {
		this(false, request, request);
	}
	
	public DownloadTask(boolean isUnique, Downloader.Request request) {
		this(isUnique, request, request);
	}
	
	public DownloadTask(Downloader.Request seed, Downloader.Request request) {
		this(false, seed, request);
	}
	
	public DownloadTask(boolean isUnique, Downloader.Request seed, Downloader.Request request) {
		super(isUnique?"download_"+seed.getUrl()+"#"+request.getUrl():null, seed, request);
	}
	
}
