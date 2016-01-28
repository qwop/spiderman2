package net.kernal.spiderman.worker.result;

import net.kernal.spiderman.worker.Task;
import net.kernal.spiderman.worker.extract.ExtractResult;
import net.kernal.spiderman.worker.extract.ExtractTask;

public class ResultTask extends Task {

	private static final long serialVersionUID = -7531379852428467887L;
	
	private ExtractResult result;
	
	public ResultTask(ExtractTask task, boolean isUnique, ExtractResult result) {
		super(task.getSeed(), task.getSource(), isUnique?"result_"+result.getPageName()+"#"+result.getModelName()+"#"+task.getSeed().getUrl()+"#"+task.getRequest().getUrl():null, task.getRequest());
		this.result = result;
	}
	
	public ExtractResult getResult() {
		return result;
	}

}
