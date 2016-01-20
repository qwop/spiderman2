package net.kernal.spiderman.worker.result;

import net.kernal.spiderman.worker.AbstractTask;
import net.kernal.spiderman.worker.download.Downloader;
import net.kernal.spiderman.worker.extract.ExtractResult;

public class ResultTask extends AbstractTask {

	private static final long serialVersionUID = -7531379852428467887L;
	
	private ExtractResult result;
	
	public ResultTask(boolean isUnique, Downloader.Request seed, Downloader.Request request, ExtractResult result) {
		super(isUnique?"result_"+result.getPageName()+"#"+result.getModelName()+"#"+seed.getUrl()+"#"+request.getUrl():null, seed, request);
		this.result = result;
	}
	
	public ExtractResult getResult() {
		return result;
	}

}
