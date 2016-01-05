package net.kernal.spiderman.store;

/**
 * 只有Master才启用的哈
 * @author 赖伟威 l.weiwei@163.com 2016-01-04
 *
 */
public class ZBusDbService implements KVDb {

	private KVDb db;
	public ZBusDbService(KVDb db) {
		this.db = db;
	}
	
	public boolean contains(String region, Object key) {
		return db.contains(region, key);
	}

	public void put(String region, Object key, Object value) {
		db.put(region, key, value);
	}

	public Object get(String region, Object key) {
		return db.get(region, key);
	}

	public void close() {
		db.close();
	}

}
