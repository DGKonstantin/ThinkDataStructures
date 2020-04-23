package com.allendowney.thinkdast;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import org.jsoup.select.Elements;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

/**
 * Represents a Redis-backed web search index.
 *
 */
public class JedisIndex {

    private Jedis jedis;
    static int urlCount = 0;
    /**
     * Constructor.
     *
     * @param jedis
     */
    public JedisIndex(Jedis jedis) {
        this.jedis = jedis;
    }

    /**
     * Returns the Redis key for a given search term.
     *
     * @return Redis key.
     */
    private String urlSetKey(String term) {
        return "URLSet:" + term;
    }

    /**
     * Returns the Redis key for a URL's TermCounter.
     *
     * @return Redis key.
     */
    private String termCounterKey(String url) {
        return "TermCounter:" + url;
    }

    /**
     * Checks whether we have a TermCounter for a given URL.
     *
     * @param url
     * @return
     */
    public boolean isIndexed(String url) {
        String redisKey = termCounterKey(url);
        return jedis.exists(redisKey);
    }

    /**
     * Adds a URL to the set associated with `term`.
     *
     * @param term
     * @param tc
     */
    public void add(String term, TermCounter tc) {
        jedis.hincrBy(urlSetKey(term), termCounterKey(tc.getLabel()), tc.get(term));
    }

    private List<Object> pushTermCounter(TermCounter tc){
        Transaction t = jedis.multi();
        String url = tc.getLabel();
        String hashname = termCounterKey(url);

        t.del(hashname);

        for (String term : tc.keySet()){
            Integer count = tc.get(term);
            t.hset(hashname, term, count.toString());
            t.sadd(urlSetKey(term), url);
        }
        List<Object> list = null;
        try{
            list = t.exec();
        }catch (Exception e){
            System.out.println(JedisIndex.urlCount);
            throw e;
        }
        JedisIndex.urlCount++;
        return list;
    }

    /**
     * Looks up a search term and returns a set of URLs.
     *
     * @param term
     * @return Set of URLs.
     */
    public Set<String> getURLs(String term) {
        return jedis.smembers(urlSetKey(term));
    }

    /**
     * Looks up a term and returns a map from URL to count.
     *
     * @param term
     * @return Map from URL to count.
     */
//    public Map<String, Integer> getCounts(String term) {
//        HashMap <String, Integer> map = new HashMap<>();
//        Set<String> urls = getURLs(term);
//        Integer count;
//        for(String url : urls){
//            //System.out.printf("url: %s, term: %s\n", url, term);
//            count = getCount(url, term);
//            map.put(url, count);
//        }
//        return map;
//    }

    public Map<String, Integer> getCounts(String term){
        Set<String> urls = getURLs(term);
        Transaction t = jedis.multi();
        for (String url : urls){
            t.hget(termCounterKey(url), term);
        }

        List<Object> list = t.exec();
        Map <String, Integer> map = new HashMap<>();
        int i = 0;
        for (String url : urls){
            Integer integer = new Integer((String) list.get(i++));
            map.put(url, integer);
        }
    return map;
    }

    /**
     * Returns the number of times the given term appears at the given URL.
     *
     * @param url
     * @param term
     * @return
     */
    public Integer getCount(String url, String term) {
        return Integer.valueOf(jedis.hget(termCounterKey(url), term));
    }

    /**
     * Adds a page to the index.
     *
     * @param url         URL of the page.
     * @param paragraphs  Collection of elements that should be indexed.
     */
    public void indexPage(String url, Elements paragraphs) {
        TermCounter tc = new TermCounter(url);
        tc.processElements(paragraphs);
        pushTermCounter(tc);
    }

    /**
     * Prints the contents of the index.
     *
     * Should be used for development and testing, not production.
     */
    public void printIndex() {
        // loop through the search terms
        for (String term : termSet()) {
            System.out.println(term);
            // for each term, print the pages where it appears
            Set<String> urls = getURLs(term);
            for (String url: urls) {
                Integer count = getCount(url, term);
                System.out.println("    " + url + " " + count);
            }
        }
    }

    /**
     * Returns the set of terms that have been indexed.
     *
     * Should be used for development and testing, not production.
     *
     * @return
     */
    public Set<String> termSet() {
        Set<String> keys = urlSetKeys();
        Set<String> terms = new HashSet<String>();
        for (String key: keys) {
            String[] array = key.split(":");
            if (array.length < 2) {
                terms.add("");
            } else {
                terms.add(array[1]);
            }
        }
        return terms;
    }

    /**
     * Returns URLSet keys for the terms that have been indexed.
     *
     * Should be used for development and testing, not production.
     *
     * @return
     */
    public Set<String> urlSetKeys() {
        return jedis.keys("URLSet:*");
    }

    /**
     * Returns TermCounter keys for the URLS that have been indexed.
     *
     * Should be used for development and testing, not production.
     *
     * @return
     */
    public Set<String> termCounterKeys() {
        return jedis.keys("TermCounter:*");
    }

    /**
     * Deletes all URLSet objects from the database.
     *
     * Should be used for development and testing, not production.
     *
     * @return
     */
    public void deleteURLSets() {
        Set<String> keys = urlSetKeys();
        Transaction t = jedis.multi();
        for (String key: keys) {
            t.del(key);
        }
        t.exec();
    }

    /**
     * Deletes all URLSet objects from the database.
     *
     * Should be used for development and testing, not production.
     *
     * @return
     */
    public void deleteTermCounters() {
        Set<String> keys = termCounterKeys();
        Transaction t = jedis.multi();
        for (String key: keys) {
            t.del(key);
        }
        t.exec();
    }

    /**
     * Deletes all keys from the database.
     *
     * Should be used for development and testing, not production.
     *
     * @return
     */
    public void deleteAllKeys() {
        Set<String> keys = jedis.keys("*");
        Transaction t = jedis.multi();
        for (String key: keys) {
            t.del(key);
        }
        t.exec();
    }

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        Jedis jedis = JedisMaker.make();
        JedisIndex index = new JedisIndex(jedis);

        //index.deleteTermCounters();
        //index.deleteURLSets();
        //index.deleteAllKeys();
        loadIndex(index);


        //index.printIndex();
        String terms = "java, the, a, not";
        for (String term : terms.split(",")){
            term = term.trim();
            Integer count = 0;
            Date start = new Date();
            Map<String, Integer> map = index.getCounts(term);
            for (Entry<String, Integer> entry: map.entrySet()) {
                count += entry.getValue();
            }
            Date finish = new Date();
            System.out.printf("Exec time for %s: %d, Count = %d\n", term, finish.getTime() - start.getTime(), count);
//            Exec time for java: 602, Count = 160
//            Exec time for the: 2491, Count = 2972
//            Exec time for a: 2660, Count = 1116
//            Exec time for not: 2284, Count = 201
        }
    }

    /**
     * Stores two pages in the index for testing purposes.
     *
     * @return
     * @throws IOException
     */
    private static void loadIndex(JedisIndex index) throws IOException {
        WikiFetcher wf = new WikiFetcher();

        String[] urls = new String[]{"https://en.wikipedia.org/wiki/County_seat",
                "https://en.wikipedia.org/wiki/Administrative_center",
                "https://en.wikipedia.org/wiki/Local_government",
                "https://en.wikipedia.org/wiki/Public_administration",
                "https://en.wikipedia.org/wiki/Public_policy",
                "https://en.wikipedia.org/wiki/Wikipedia:Citation_needed",
                "https://en.wikipedia.org/wiki/Wikipedia:Verifiability",
                "https://en.wikipedia.org/wiki/Wikipedia",
                "https://en.wikipedia.org/wiki/Help:Pronunciation_respelling_key",
                "https://en.wikipedia.org/wiki/Pronunciation_respelling_for_English",
                "https://en.wikipedia.org/wiki/Pronunciation_respelling",
                "https://en.wikipedia.org/wiki/Ad_hoc",
                "https://en.wikipedia.org/wiki/List_of_Latin_phrases",
                "https://en.wikipedia.org/wiki/English_language",
                "https://en.wikipedia.org/wiki/West_Germanic_language",
                "https://en.wikipedia.org/wiki/Germanic_languages",
                "https://en.wikipedia.org/wiki/Indo-European_languages",
                "https://en.wikipedia.org/wiki/Language_family",
                "https://en.wikipedia.org/wiki/Language",
                "https://en.wikipedia.org/wiki/Grammar",
                "https://en.wikipedia.org/wiki/Linguistics",
                "https://en.wikipedia.org/wiki/Science",
                "https://en.wikipedia.org/wiki/Knowledge",
                "https://en.wikipedia.org/wiki/Fact",
                "https://en.wikipedia.org/wiki/Reality",
                "https://en.wikipedia.org/wiki/Object_of_the_mind",
                "https://en.wikipedia.org/wiki/Mind",
                "https://en.wikipedia.org/wiki/Intellect",
                "https://en.wikipedia.org/wiki/Truth",
                "https://en.wikipedia.org/wiki/Modernity",
                "https://en.wikipedia.org/wiki/Norm_(social)"};

        Elements paragraphs = null;

        for (String url : urls){
            paragraphs = wf.readWikipedia(url);
            index.indexPage(url, paragraphs);
        }
        System.out.println(urlCount);

    }
}