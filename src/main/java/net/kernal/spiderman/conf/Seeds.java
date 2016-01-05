package net.kernal.spiderman.conf;

import java.util.ArrayList;
import java.util.List;

import net.kernal.spiderman.downloader.Downloader;

public class Seeds {
	
		private List<Seed> seeds;
		
		public Seeds() {
			this.seeds = new ArrayList<Seed>();
		}
		public List<Seed> all() {
			return this.seeds;
		}
		public boolean isEmpty() {
			return this.seeds.isEmpty();
		}
		public Seeds add(Downloader.Request request) {
			return add(new Seed(null, request));
		}
		public Seeds add(Seed seed) {
			this.seeds.add(seed);
			return this;
		}
		public Seeds add(String url) {
			return this.add(new Downloader.Request(url));
		}
	}