package net.kernal.spiderman.store;

public interface KVDb {

	public boolean contains(String region, Object key);
	public void put(String region, Object key, Object value);
	public Object get(String region, Object key);
	public void close();
	
}
