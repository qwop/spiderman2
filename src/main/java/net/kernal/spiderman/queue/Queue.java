package net.kernal.spiderman.queue;

public interface Queue<T> {

	public T take();
	
	public void append(T task);
	
	public void clear();
	
}
