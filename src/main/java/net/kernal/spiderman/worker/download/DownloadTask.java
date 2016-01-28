package net.kernal.spiderman.worker.download;

import net.kernal.spiderman.Seed;
import net.kernal.spiderman.worker.Task;
import net.kernal.spiderman.worker.extract.ExtractTask;

public class DownloadTask extends Task {

	private static final long serialVersionUID = 6126003860229810350L;
	
	public DownloadTask(Seed seed) {
		this(seed, false, new Downloader.Request(seed.getUrl()));
	}
	
	public DownloadTask(Seed seed, boolean isUnique) {
		this(seed, isUnique, new Downloader.Request(seed.getUrl()));
	}
	
	public DownloadTask(ExtractTask task, boolean isUnique, Downloader.Request request) {
		super(task.getSeed(), task, isUnique?"download_"+task.getSeed().getUrl()+"#"+request.getUrl():null, request);
	}
	
	public DownloadTask(Seed seed, boolean isUnique, Downloader.Request request) {
		super(seed, null, isUnique?"download_"+seed.getUrl()+"#"+request.getUrl():null, request);
	}
	
}
