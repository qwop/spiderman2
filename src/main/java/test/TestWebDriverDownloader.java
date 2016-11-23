package test;

import net.kernal.spiderman.kit.Properties;
import net.kernal.spiderman.worker.download.Downloader;
import net.kernal.spiderman.worker.download.impl.WebDriverDownloader;

/**
 * @author 赖伟威 l.weiwei@163.com 2016-03-28
 * @author <a href='http://krbit.github.io'>Krbit</a>
 * @version V0.1.0
 */
public class TestWebDriverDownloader {
    public static void main(String[] args) {
        final String url = "http://www.tianyancha.com/search?key=%E5%8C%97%E4%BA%AC%E7%99%BE%E5%BA%A6%E7%BD%91%E8%AE%AF%E7%A7%91%E6%8A%80%E6%9C%89%E9%99%90%E5%85%AC%E5%8F%B8&checkFrom=searchBox";
        final Properties opts = new Properties();
        opts.put("worker.download.chrome.driver", "dist/chromedriver.exe");
        Downloader downloader = new WebDriverDownloader(opts);
        Downloader.Request request = new Downloader.Request(url);
        Downloader.Response response = downloader.download(request);
        final Throwable err = response.getException();
        if (err != null) {
        	err.printStackTrace();
        	return;
        }
        final String body = response.getBodyStr();
        System.out.println(body);
    }
}
