package net.kernal.spiderman.store;

public interface KVStore {

	public boolean contains(String group, String key);
	
	public void put(String group, String key, byte[] value);
	
	public byte[] get(String group, String key);
	
	public void removeKeys(String group);
	
	public void removeKey(String group, String key);
	
	public void close();
	
}
