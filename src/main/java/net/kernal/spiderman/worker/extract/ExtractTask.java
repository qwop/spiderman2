package net.kernal.spiderman.worker.extract;

import net.kernal.spiderman.Seed;
import net.kernal.spiderman.worker.Task;
import net.kernal.spiderman.worker.download.Downloader;

public class ExtractTask extends Task {

	private static final long serialVersionUID = 7914255707423166114L;
	
	private Downloader.Response response;
	
	public ExtractTask(Seed seed, boolean isUnique, Downloader.Response response) {
		super(seed, isUnique);
		this.response = response;
	}
	
	public String getUniqueKey() {
		final String key = "extract_"+getSeed().getUrl()+"#"+this.response.getRequest().getUrl();
		return key;
	}
	
	public Downloader.Response getResponse() {
		return this.response;
	}
	
}
