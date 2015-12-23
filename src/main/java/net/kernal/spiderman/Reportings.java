package net.kernal.spiderman;

import java.util.ArrayList;
import java.util.List;

import net.kernal.spiderman.parser.Parser;

public class Reportings implements Reporting{

	private List<Reporting> reportings;
	public Reportings() {
		this.reportings = new ArrayList<Reporting>();
	}
	
	public Reportings add(Reporting reporting) {
		this.reportings.add(reporting);
		return this;
	}
	
	public void reportStart() {
		K.foreach(this.reportings, new K.ForeachCallback<Reporting>() {
			public void each(int i, Reporting item) {
				item.reportStart();
			}
		});
	}
	
	public void reportDownload(final Downloader.Response response) {
		K.foreach(this.reportings, new K.ForeachCallback<Reporting>() {
			public void each(int i, Reporting item) {
				item.reportDownload(response);
			}
		});
	}
	
	public void reportNewTask(final Task newTask) {
		K.foreach(this.reportings, new K.ForeachCallback<Reporting>() {
			public void each(int i, Reporting item) {
				item.reportNewTask(newTask);
			}
		});
	}
	
	public void reportParsedResult(final Parser.ParsedResult parsedModels) {
		K.foreach(this.reportings, new K.ForeachCallback<Reporting>() {
			public void each(int i, Reporting item) {
				item.reportParsedResult(parsedModels);
			}
		});
	}
	
	public void reportStop(final Spiderman.Counter counter, final int poolSize, final int activeCount, final long completedTaskCount) {
		K.foreach(this.reportings, new K.ForeachCallback<Reporting>() {
			public void each(int i, Reporting item) {
				item.reportStop(counter, poolSize, activeCount, completedTaskCount);
			}
		});
	}
	
}
