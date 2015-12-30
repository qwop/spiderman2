package net.kernal.spiderman.conf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Targets {
	
		private List<Target> list;
		
		public Targets() {
			this.list = new ArrayList<Target>();
		}
		
		public List<Target> all() {
			return this.list;
		}
		
		public Targets add(Target... target) {
			this.list.addAll(Arrays.asList(target));
			return this;
		}
		
		public Targets addAll(List<Target> targets) {
			this.list.addAll(targets);
			return this;
		}
		
		public boolean isEmpty() {
			return this.list.isEmpty();
		}
		
	}