package net.kernal.spiderman.worker.download;

import net.kernal.spiderman.K;
import net.kernal.spiderman.Seed;
import net.kernal.spiderman.worker.Task;

public class DownloadTask extends Task {

	private static final long serialVersionUID = 6126003860229810350L;
	
	private Downloader.Request request;
	
	public DownloadTask(Seed seed, Downloader.Request request) {
		super(seed);
		this.request = request;
	}
	
	public String getUniqueKey() {
		final String key = K.md5(getSeed().getName()+"#"+this.request.getUrl());
		return key;
	}
	
	public Downloader.Request getRequest() {
		return this.request;
	}
	
}
