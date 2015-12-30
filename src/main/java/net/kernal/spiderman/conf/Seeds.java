package net.kernal.spiderman.conf;

import java.util.ArrayList;
import java.util.List;

import net.kernal.spiderman.downloader.Downloader;

public class Seeds {
		private List<Downloader.Request> requests;
		public Seeds() {
			this.requests = new ArrayList<Downloader.Request>();
		}
		public List<Downloader.Request> all() {
			return this.getAll();
		}
		public List<Downloader.Request> getAll(){
			return this.requests;
		}
		public boolean isEmpty() {
			return this.requests.isEmpty();
		}
		public Seeds add(Downloader.Request request) {
			this.requests.add(request);
			return this;
		}
		public Seeds add(String url) {
			return this.add(new Downloader.Request(url));
		}
	}