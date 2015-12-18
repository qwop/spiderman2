package net.kernal.spiderman;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 工具类(Kit)
 * @author 赖伟威 l.weiwei@163.com 2015-12-10
 *
 */
public class K {
	
	public final static String HTTP_GET = "GET";
	public final static String HTTP_POST = "POST";
	
	public final static String trim(String input) {
		return K.isNotBlank(input) ? input.trim() : input;
	}

	/**
	 * 检查给定字符串是否为空(null或者空)
	 * @param input 被判断的字符串
	 * @return 若字符串空，返回true，否则返回false
	 */
	public final static boolean isBlank(String input) {
		return input == null ? true : input.trim().length() == 0;
	}
	
	/**
	 * 检查给定字符串是否不为空(null或者空)
	 * @param input 被判断的字符串
	 * @return 若字符串不空，返回true，否则返回false
	 */
	public final static boolean isNotBlank(String input) {
		return !isBlank(input);
	}
	
	/**
	 * 检查给定字符集名是否有效
	 * @param charsetName 需要检查的字符集名，比如 gbk
	 * @return 若是有效的字符集，返回true，否则返回false
	 */
	public final static boolean isValidCharset(String charsetName) {
		return Charset.forName(charsetName) != null;
	}
	
	/**
	 * 检查给定字符集名是否无效
	 * @param charsetName 需要检查的字符集名，比如 gbk
	 * @return 若是无效的字符集，返回true，否则返回false
	 */
	public final static boolean isNotValidCharset(String charsetName) {
		return !isValidCharset(charsetName);
	}
	
	public final static String byteToString(byte[] byteData) {
		return new String(byteData);
	}
	
	/**
	 * 将给定的二进制数据转换为特定编码的字符串
	 * @param byteContent 二进制数据
	 * @param charset 字符集名
	 * @return 当不给定字符集，返回ISO-8859-1字符串，当给定的字符集不合法时，返回null，否则返回指定编码的字符串
	 */
	public final static String byteToString(byte[] byteData, String charset) {
		if (byteData == null) {
			return null;
		}
		if (isBlank(charset)) {
			return new String(byteData);
		}
		
		if (isNotValidCharset(charset)) {
			return null;
		}
		
		try {
			return new String(byteData, charset);
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}
	
	/**
	 * 判断给定的数值是否在指定的列表中
	 * @param input
	 * @param obj1
	 * @param obj2
	 * @return
	 */
	public final static boolean isIn(int input, int... iList) {
		for (int i : iList) {
			if (input == i) return true;
		}
		return false;
	}
	
	/**
	 * 将给定的时间字符串转换为秒
	 * @param strTime 比如 1h 1h1m1s
	 * @return
	 */
	public static BigDecimal convertToSeconds(String strTime) {
		BigDecimal time = new BigDecimal("0");
        for (String s : strTime.split(" ")) {
        	BigDecimal _time = _convertToSeconds(s);
        	time = time.add(_time);
        }

        return time;
    }

    private static BigDecimal _convertToSeconds(String strTime) {
        float time = 0F;
        try {
            if (strTime.endsWith("s")) {
                time = Float.parseFloat(strTime.replace("s", "")) * 1;
            } else if (strTime.endsWith("m")) {
                time = Float.parseFloat(strTime.replace("m", "")) * 60;
            } else if (strTime.endsWith("h")) {
                time = Float.parseFloat(strTime.replace("h", "")) * 60 * 60;
            } else if (strTime.endsWith("d")) {
                time = Float.parseFloat(strTime.replace("d", "")) * 60 * 60 * 24;
            } else time = Float.parseFloat(strTime);
        } catch (Throwable e) {

        }

        return new BigDecimal(String.valueOf(time));
    }
    
    /**
     * 判断给定的参数是否全都为null
     * @param inputs
     * @return
     */
    public final static boolean isALLNull(Object... inputs) {
    	for (Object input : inputs) {
    		if (input != null) return false;
    	}
    	
    	return true;
    }
    
    /**
     * 判断给定的集合是否为空
     * @param c
     * @return
     */
    public final static boolean isEmpty(Collection<?> c) {
    	return c == null ? true : c.isEmpty();
    }
	
    /**
     * 判断给定的集合是否不为空
     * @param c
     * @return
     */
    public final static boolean isNotEmpty(Collection<?> c) {
    	return !isEmpty(c);
    }
    
    /**
     * 判断给定的数组是否为空
     * @param array
     * @return
     */
    public final static <T> boolean isEmpty(T[] array) {
    	return array == null ? true : array.length == 0;
    }
	
    /**
     * 判断给定的数组是否不为空
     * @param array
     * @return
     */
    public final static <T> boolean isNotEmpty(T[] array) {
    	return !isEmpty(array);
    }
    
    public static abstract class ForeachCallback<T> {
    	private boolean isBreak;
    	public void breakoff(){
    		this.isBreak = true;
    	}
    	public abstract void each(int i, T item);
    }
    
    public final static <T> void foreach(Collection<T> c, ForeachCallback<T> fc ) {
    	if (isNotEmpty(c)) {
    		int i = 0;
    		for (T item : c) {
				fc.each(i, item);
				if (fc.isBreak) {
					break;
				}
    			i++;
    		}
    	} 
    }
    
    public final static <T> void foreach(T[] array, ForeachCallback<T> fc ) {
    	if (isNotEmpty(array)) {
    		for (int i = 0; i < array.length; i++) {
    			T item = array[i];
				fc.each(i, item);
				if (fc.isBreak) {
					break;
				}
    		}
    	} 
    }
    
    public final static String urlEncode(String str){
    	return urlEncode(str, null);
    }
    
    public final static String urlEncode(String str, String charset){
    	try {
    		if (isBlank(charset)) {
    			charset = "UTF-8";
    		}
			return URLEncoder.encode(str, charset);
		} catch (UnsupportedEncodingException e) {
			return str;
		}
    }
    
    public final static String cleanUnicode(String str) {
		char[] xmlChar = str.toCharArray();
		for (int i=0; i < xmlChar.length; ++i) {
	        if (xmlChar[i] > 0xFFFD)
	        	xmlChar[i] =' ';// 用空格替换

	        if (xmlChar[i] < 0x20 && xmlChar[i] != 't' & xmlChar[i] != 'n' & xmlChar[i] != 'r')
	        	xmlChar[i] =' ' ;// 用空格替换
		}

		return new String(xmlChar);
	}
    
    public final static String resolveUrl(String currentUrl, String url) {
    	if (K.isBlank(url)) return url;
    	if (url.startsWith("http://") || url.startsWith("https://")) return url;
    	try {
			URL U = new URL(currentUrl);
			int p = U.getPort();
			StringBuilder builder = new StringBuilder();
			builder.append(U.getProtocol()).append("://");
			builder.append(U.getHost());
			if (p > 0) {
				builder.append(":").append(p);
			}
			for (String u : url.split("/")) {
				if (K.isBlank(u) || "/".equals(u)) continue;
				builder.append("/").append(u);
			}
			return builder.toString();
		} catch (MalformedURLException e) {
			return url;
		}
    }
    
    public static String findOneByRegex(String input, String regex){
    	List<String> tmp = findByRegex(input, regex);
    	return isEmpty(tmp) ? null : tmp.get(0);
    }
    
    public static List<String> findByRegex(String input, String regex){
		List<String> result = new ArrayList<String>();
		Pattern p = Pattern.compile(regex, Pattern.DOTALL);
		Matcher m = p.matcher(input);
		while(m.find()){
			result.add(m.group());
		}
		
		if (result.isEmpty()) return null;
		
		return result;
	}
    
    public final static String byteToStringForHtml(byte[] htmlData, String charsetName) {
    	if (htmlData == null || htmlData.length == 0) 
    		return null;
    	Charset charset = Charset.forName(charsetName);
    	if (charset == null) {
	    	String input = byteToString(htmlData);
			String html = input.trim().toLowerCase();
			String s1 = findOneByRegex(html, "(?=<meta ).*charset=.[^/]*");
			if (isBlank(s1)) 
				return input;
			
			String s2 = findOneByRegex(s1, "(?=charset\\=).[^;/\"']*");
			if (isBlank(s2))
				return input;
			
			charsetName = s2.replace("charset=", "");
    	}
    	
		return byteToString(htmlData, charsetName);
    }
    
    public final static String formatNow() {
    	return formatTime(new Date());
    }
    
    public final static String formatNow(String format) {
    	return formatTime(new Date(), format);
    }
    
    public final static String formatTime(Date time) {
    	return formatTime(time, "yyyy-MM-dd HH:mm:ss");
    }
    
    public final static String formatTime(Date time, String format) {
    	SimpleDateFormat sdf = new SimpleDateFormat(format);
    	return sdf.format(time);
    }
    
}
