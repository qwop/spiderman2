package net.kernal.spiderman.worker;

import java.io.Serializable;

import net.kernal.spiderman.Seed;

/**
 * 任务抽象
 * @author 赖伟威 l.weiwei@163.com 2016-01-16
 *
 */
public abstract class Task implements Serializable{

	private static final long serialVersionUID = 2506296221733528670L;

	/** 种子 */
	private Seed seed;
	/** 是否唯一，不允许重复  */
	private boolean isUnique;
	
	protected Task(Seed seed, boolean isUnique) {
		this.seed = seed;
		this.isUnique = isUnique;
	}
	
	public Seed getSeed() {
		return this.seed;
	}
	
	public boolean isUnique() {
		return isUnique;
	}
	
	public abstract String getUniqueKey();
	
}
