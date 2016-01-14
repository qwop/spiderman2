package net.kernal.spiderman.worker;

import java.io.Serializable;

import net.kernal.spiderman.Seed;

public abstract class Task implements Serializable{

	private static final long serialVersionUID = 2506296221733528670L;

	private Seed seed;
	
	protected Task(Seed seed) {
		this.seed = seed;
	}
	
	public Seed getSeed() {
		return this.seed;
	}

	public abstract String getUniqueKey();
	
}
