package com.allendowney.thinkdast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

public class WikiPhilosophy {

    final static List<String> visited = new ArrayList<String>();
    final static WikiFetcher wf = new WikiFetcher();

    /**
     * Tests a conjecture about Wikipedia and Philosophy.
     *
     * https://en.wikipedia.org/wiki/Wikipedia:Getting_to_Philosophy
     *
     * 1. Clicking on the first non-parenthesized, non-italicized link
     * 2. Ignoring external links, links to the current page, or red links
     * 3. Stopping when reaching "Philosophy", a page with no links or a page
     *    that does not exist, or when a loop occurs
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        String destination = "https://en.wikipedia.org/wiki/Philosophy";
        String source = "https://en.wikipedia.org/wiki/Java_(programming_language)";

        testConjecture(destination, source, 10);
    }

    /**
     * Starts from given URL and follows first link until it finds the destination or exceeds the limit.
     *
     * @param destination
     * @param source
     * @throws IOException
     */
    public static void testConjecture(String destination, String source, int limit) throws IOException {
        if (limit == 0) {
            System.out.println("Out of limit iteration");
            return;
        }
        if (source.equals(destination)) {
            System.out.println("Finished with " + limit + " iteration");
            return;
        }
        String url = getFirstLink(source);
        testConjecture(destination, url, limit--);
    }

    private static String getFirstLink(String source) throws IOException {
        Connection conn = Jsoup.connect(source);
        Document doc = conn.get();
        Element content = doc.getElementById("mw-content-text");
        String newSurce;
        if ((newSurce = getFirstLinkBySelect(content, "p")) != null) {
            return newSurce;
        }else return getFirstLinkBySelect(content, "ul");
    }

    private static String getFirstLinkBySelect(Element content, String cssQuery){
        Elements elements = content.select(cssQuery);

        for (Element element : elements){
            Iterable<Node> iterable = new WikiNodeIterable(element);
            Iterator<Node> iterator = iterable.iterator();

            while (iterator.hasNext()){
                Node node = iterator.next();
                if (node.hasAttr("href")){
                    System.out.println(node.absUrl("href"));
                    return node.absUrl("href");
                }
            }
        }
        return null;
    }

    private static void recursiveDFS(Node node) {
        //if (node instanceof TextNode) {
            System.out.print(node);
        //}
        for (Node child: node.childNodes()) {
            recursiveDFS(child);
        }
    }
}
