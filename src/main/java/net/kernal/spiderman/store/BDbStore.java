package net.kernal.spiderman.store;

import java.io.File;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

public class BDbStore implements KVStore {

	private Environment env;
	private Database db;
	
	public BDbStore(File file, String dbName) {
		// Open the environment. Create it if it does not already exist.
		EnvironmentConfig envCfg = new EnvironmentConfig();
		envCfg.setAllowCreate(true);
		env = new Environment(file, envCfg);

		// Open the database. Create it if it does not already exist.
		DatabaseConfig dbCfg = new DatabaseConfig();
		dbCfg.setAllowCreate(true);
		dbCfg.setSortedDuplicates(true);
		db = env.openDatabase(null, dbName, dbCfg);
	}
	
	public boolean contains(String key) {
		return get(key) != null;
	}

	public void put(String key, byte[] value) {
		this.db.put(null, new DatabaseEntry(key.getBytes()), new DatabaseEntry(value));
	}

	public byte[] get(String key) {
		DatabaseEntry value = new DatabaseEntry();
		OperationStatus stat = this.db.get(null, new DatabaseEntry(key.getBytes()), value, LockMode.DEFAULT);
		return stat == OperationStatus.SUCCESS ? value.getData() : null;
	}
	
	public void close() {
		this.env.sync();
		this.db.close();
		this.env.cleanLog();
		this.env.close();
	}

}
