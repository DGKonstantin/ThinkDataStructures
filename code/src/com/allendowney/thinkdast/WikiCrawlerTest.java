/**
 *
 */
package com.allendowney.thinkdast;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.*;

import java.io.IOException;
import java.util.Map;

import org.jsoup.select.Elements;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import redis.clients.jedis.Jedis;

/**
 * @author downey
 *
 */
public class WikiCrawlerTest {

	private Jedis jedis;
	private WikiCrawler wc;
	private JedisIndex index;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		// make a WikiCrawler
		jedis = JedisMaker.make();
		index = new JedisIndex(jedis);
		String source = "https://en.wikipedia.org/wiki/Java_(programming_language)";
		wc = new WikiCrawler(source, index);

		// for testing purposes, load up the queue
		WikiFetcher wf = new WikiFetcher();
		Elements paragraphs = wf.readWikipedia(source);
		wc.queueInternalLinks(paragraphs);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		jedis.close();
	}

	/**
	 * Test method for {@link WikiCrawler#crawl()}.
	 * @throws IOException
	 */
	@Test
	public void testCrawl() throws IOException {
		String url1 = "https://en.wikipedia.org/wiki/Java_(programming_language)";
		String url2 = "https://en.wikipedia.org/wiki/General-purpose_language";
		String url3 = "https://en.wikipedia.org/wiki/Programming_language";

		String res = wc.crawl(true);
		System.out.printf("url: %s, ress: %s\n", url1, res);
		assertThat(url1.equals(res), is(true));
		assertThat(wc.queueSize(), is(414));

		res = wc.crawl(true);
		System.out.printf("url: %s, ress: %s\n", url2, res);
		assertThat(url2.equals(res), is(true));
		assertThat(wc.queueSize(), is(416));

		res = wc.crawl(true);
		System.out.printf("url: %s, ress: %s\n", url3, res);
		assertThat(url3.equals(res), is(true));
		assertThat(wc.queueSize(), is(693));

		Map<String, Integer> map = index.getCounts("the");

		int count = map.get(url1);
		assertThat(count, is(253));

		count = map.get(url2);
		assertThat(count, is(2));

		count = map.get(url3);
		assertThat(count, is(272));
	}
}
