package net.kernal.spiderman.worker.result;

import net.kernal.spiderman.Seed;
import net.kernal.spiderman.worker.Task;
import net.kernal.spiderman.worker.extract.ExtractResult;

public class ResultTask extends Task {

	private static final long serialVersionUID = -7531379852428467887L;
	
	private ExtractResult result;
	
	public ResultTask(Seed seed, ExtractResult result) {
		super(seed);
		this.result = result;
	}
	
	public ExtractResult getResult() {
		return result;
	}

	public String getUniqueKey() {
		return getSeed().getUrl()+"#"+result.getPageName()+"#"+result.getModelName();
	}

}
