package net.kernal.spiderman.worker.extract;

import net.kernal.spiderman.worker.Task;
import net.kernal.spiderman.worker.download.Downloader;

public class ExtractTask extends Task {

	private static final long serialVersionUID = 7914255707423166114L;
	
	private Downloader.Response response;
	
	public ExtractTask(boolean isUnique, Downloader.Request seed, Downloader.Response response) {
		super(isUnique?"extract_"+seed.getUrl()+"#"+response.getRequest().getUrl():null, seed, response.getRequest());
		this.response = response;
	}
	
	public Downloader.Response getResponse() {
		return this.response;
	}
	
}
