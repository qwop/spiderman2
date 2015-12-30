package net.kernal.spiderman;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * HashMap Help类
 * @author 赖伟威 l.weiwei@163.com 2015-12-10
 *
 */
public class Properties extends HashMap<String, Object> {
	private static final long serialVersionUID = 1L;
	public String getString(String Stringey, String defaultVal) {
		try {
			Object v = get(Stringey);
			if (v == null) return defaultVal;
			if (v instanceof Object[]){
				Object[] nv = (Object[])v;
				return String.valueOf(nv[0]);
			}
			return String.valueOf(v);
		} catch (Throwable e){
			
		}
		return defaultVal;
	}
	
	public Integer getInteger(String Stringey, Integer defaultVal) {
		try {
			return Integer.parseInt(getString(Stringey));
		} catch (Throwable e){
			
		}
		return defaultVal;
	}
	
	public int getInt(String Stringey, int defaultVal) {
		try {
			return Integer.parseInt(getString(Stringey));
		} catch (Throwable e){
			
		}
		return defaultVal;
	}
	
	public byte getByte(String Stringey, byte defaultVal) {
		try {
			return Byte.parseByte(getString(Stringey));
		} catch (Throwable e){
			
		}
		return defaultVal;
	}

	public Byte getByte(String Stringey) {
		try {
			return Byte.parseByte(getString(Stringey));
		} catch (Throwable e){
			
		}
		return null;
	}

	public Long getLong(String Stringey, Long defaultVal) {
		try {
			return Long.parseLong(getString(Stringey));
		} catch (Throwable e){
			
		}
		return defaultVal;
	}
	
	public long getLong(String Stringey, long defaultVal) {
		try {
			return Long.parseLong(getString(Stringey));
		} catch (Throwable e){
			
		}
		return defaultVal;
	}

	public float getFloat(String Stringey, float defaultVal) {
		try {
			return Float.parseFloat(getString(Stringey));
		} catch (Throwable e){
			
		}
		return defaultVal;
	}
	
	public Float getFloat(String Stringey, Float defaultVal) {
		try {
			return Float.parseFloat(getString(Stringey));
		} catch (Throwable e){
			
		}
		return defaultVal;
	}

	public double getDouble(String Stringey, double defaultVal) {
		try {
			return Double.parseDouble(getString(Stringey));
		} catch (Throwable e){
			
		}
		return defaultVal;
	}
	
	public Double getDouble(String Stringey, Double defaultVal) {
		try {
			return Double.parseDouble(getString(Stringey));
		} catch (Throwable e){
			
		}
		return defaultVal;
	}

	public Boolean getBoolean(String Stringey, Boolean defaultVal) {
		try {
			return Boolean.parseBoolean(getString(Stringey));
		} catch (Throwable e){
			
		}
		return defaultVal;
	}
	
	public boolean getBoolean(String Stringey, boolean defaultVal) {
		try {
			return Boolean.parseBoolean(getString(Stringey));
		} catch (Throwable e){
			
		}
		return defaultVal;
	}

	public Integer getInteger(String Stringey) {
		try {
			return Integer.parseInt(getString(Stringey));
		} catch (Throwable e){
			
		}
		return null;
	}
	
	public int getInt(String Stringey) {
		try {
			return Integer.parseInt(getString(Stringey));
		} catch (Throwable e){
			
		}
		
		return 0;
	}

	public String getString(String Stringey) {
		try {
			return getString(Stringey, null);
		} catch (Throwable e){
			
		}
		return null;
	}

	public Long getLong(String Stringey) {
		try {
			return Long.parseLong(getString(Stringey));
		} catch (Throwable e){
			
		}
		return null;
	}

	public Float getFloat(String Stringey) {
		try {
			return Float.parseFloat(getString(Stringey));
		} catch (Throwable e){
			
		}
		return null;
	}

	public Double getDouble(String Stringey) {
		try {
			return Double.parseDouble(getString(Stringey));
		} catch (Throwable e){
			
		}
		return null;
	}

	public Boolean getBoolean(String Stringey) {
		try {
			return Boolean.parseBoolean(getString(Stringey));
		} catch (Throwable e){
			
		}
		return null;
	}
	
	public List<String> getListString(String key) {
		return getListString(key, null, ",");
	}
	public List<String> getListString(String key, String defaultVal, String split) {
		List<String> list = new ArrayList<String>();
		Object obj = get(key);
		if (obj != null) {
			//若本身就是List 或者 Array 类型，直接返回
			if (obj instanceof List) {
				for (Object v : (List<?>)obj){
					list.add(String.valueOf(v));
				}
				return list;
			}
			
			if (obj instanceof Object[]) {
				for (Object v : (Object[])obj){
					list.add(String.valueOf(v));
				}
				return list;
			}
		}
		
		//否则按给定的split进行分隔变成数组返回
		String[] arr = this.getString(key, defaultVal).split(split);
		for (String s : arr){
			list.add(s);
		}
		
		return list;
	}
	
	public Class<?> getClass(String key, Class<?> defaultValue) {
		if (!this.containsKey(key)) {
			return defaultValue;
		}
		
		return (Class<?>)this.get(key);
	}
	
}