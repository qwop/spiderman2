package net.kernal.spiderman.reporting;

import java.util.ArrayList;
import java.util.List;

import net.kernal.spiderman.Counter;
import net.kernal.spiderman.K;
import net.kernal.spiderman.downloader.Downloader;
import net.kernal.spiderman.downloader.Downloader.Request;
import net.kernal.spiderman.parser.Parser;
import net.kernal.spiderman.task.Task;

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
	
	public void reportParsedResult(final Task task, final Parser.ParsedResult parsedResult) {
		K.foreach(this.reportings, new K.ForeachCallback<Reporting>() {
			public void each(int i, Reporting item) {
				item.reportParsedResult(task, parsedResult);
			}
		});
	}
	
	public void reportStop(final Counter counter) {
		K.foreach(this.reportings, new K.ForeachCallback<Reporting>() {
			public void each(int i, Reporting item) {
				item.reportStop(counter);
			}
		});
	}

	public void reportDuplicate(String key, Request request) {
		this.reportings.forEach(reporting -> {
			reporting.reportDuplicate(key, request);
		});
	}
	
}
