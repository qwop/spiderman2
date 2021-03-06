package net.kernal.spiderman.worker.extract.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * 查找IFrame
 * <Short overview of features> 
 * <Features detail> 
 * 
 * @author		qwop
 * @date 		May 21, 2017 
 * @version		[The version number, May 21, 2017] 
 * @see			[Related classes/methods] 
 * @since		[Products/Module version]
 */
public class IframeLinkFinder implements LinksFinder {

	private final String content;

	public IframeLinkFinder(final String content) {
		if (content == null || "".equals(content.trim())) {
			throw new IllegalArgumentException("content cannot be null");
		}
		this.content = content;
	}

	public List<String> getLinks() {
		Pattern pattern = Pattern.compile("(?i)(?s)<\\s*?iframe.*?src\\s*?=\\s*?[\",'](.*?)[\",'].*?>");
		Matcher matcher = pattern.matcher(content);

		List<String> list = new ArrayList<String>();
		while (matcher.find()) {
			final String link = matcher.group(1);
			if (link.startsWith("javascript:")) {
				continue;
			}
			list.add(link);
		}

		return list;
	}

}
