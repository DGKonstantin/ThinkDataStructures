package com.allendowney.thinkdast;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import redis.clients.jedis.Jedis;


public class WikiCrawler {
	// keeps track of where we started
	@SuppressWarnings("unused")
	private final String source;

	// the index where the results go
	private JedisIndex index;

	// queue of URLs to be indexed
	private Queue<String> queue = new LinkedList<String>();

	// fetcher used to get pages from Wikipedia
	final static WikiFetcher wf = new WikiFetcher();

	/**
	 * Constructor.
	 *
	 * @param source
	 * @param index
	 */
	public WikiCrawler(String source, JedisIndex index) {
		this.source = source;
		this.index = index;
		queue.offer(source);
	}

	/**
	 * Returns the number of URLs in the queue.
	 *
	 * @return
	 */
	public int queueSize() {
		return queue.size();
	}

	/**
	 * Gets a URL from the queue and indexes it.
	 * @param testing
	 *
	 * @return URL of page indexed.
	 * @throws IOException
	 */
	public String crawl(boolean testing) throws IOException {
		if (queue.isEmpty()) return null;
		String url = queue.poll();
		if (!testing && index.isIndexed(url)) return null;
		Elements paragrarhs = testing ? wf.readWikipedia(url) : wf.fetchWikipedia(url);
		index.indexPage(url, paragrarhs);
		queueInternalLinks(paragrarhs);
        return url;
	}

	/**
	 * Parses paragraphs and adds internal links to the queue.
	 * 
	 * @param paragraphs
	 */
	// NOTE: absence of access level modifier means package-level
	void queueInternalLinks(Elements paragraphs) {
		for(Element node : paragraphs){
			queueInternalLinks(node);
		}
	}


	private void queueInternalLinks(Element paragraph) {
		Elements elts = paragraph.select("a[href]");
		for (Element elt: elts) {
			boolean isImage = elt.attr("class").equals("image");
			boolean isRef = elt.attr("href").contains("#");
			String relURL = elt.attr("href");
			if (relURL.startsWith("/wiki/")
					&& !isImage && !isRef) {
				String absURL = "https://en.wikipedia.org" + relURL;
				//System.out.println(absURL);
				queue.offer(absURL);
			}
		}
	}

	public static void main(String[] args) throws IOException {
		// make a WikiCrawler
		Jedis jedis = JedisMaker.make();
		JedisIndex index = new JedisIndex(jedis);
		String source = "https://en.wikipedia.org/wiki/Java_(programming_language)";
		WikiCrawler wc = new WikiCrawler(source, index);
		
		// for testing purposes, load up the queue
		index.deleteAllKeys();
		Elements paragraphs = wf.fetchWikipedia(source);
		wc.queueInternalLinks(paragraphs);
//		for (String url : wc.queue){
//			System.out.println(url);
//		}
		// loop until we index a new page
		String res;
//		do {
//			System.out.println(wc.queueSize());
//			res = wc.crawl(false);
//			//System.out.println(res);
//            // REMOVE THIS BREAK STATEMENT WHEN crawl() IS WORKING
//            //break;
//		} while (!res.equals("https://en.wikipedia.org/wiki/Library_(computing)"));
		
		Map<String, Integer> map = index.getCounts("ass");
		for (Entry<String, Integer> entry: map.entrySet()) {
			System.out.println(entry);
		}
	}
}
