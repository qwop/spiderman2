package net.kernal.spiderman.store;

import java.io.File;

import org.mapdb.DB;
import org.mapdb.DBMaker;

public class MapDb implements KVDb {
	
	private DB db;
	
	public MapDb(File file) {
		this.db = DBMaker.newFileDB(file)
	               .closeOnJvmShutdown()
	               .encryptionEnable("password")
	               .make();
	}
	
	public boolean contains(String region, Object key) {
		return db.getTreeMap(region).containsKey(key);
	}
	
	public void put(String region, Object key, Object value) {
		db.getTreeMap(region).put(key, value);
		db.commit();
	}
	
	public Object get(String region, Object key) {
		return db.getTreeMap(region).get(key);
	}
	
	public void close() {
		this.db.close();
	}
	
}
