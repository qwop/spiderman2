package net.kernal.spiderman.store;

public interface KVStore {

	public boolean contains(String key);
	
	public void put(String key, byte[] value);
	
	public byte[] get(String key);
	
	public void close();
	
}
