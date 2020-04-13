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

    private static int staticLimit = 30;

    public static void main(String[] args) throws IOException {
        String destination = "https://en.wikipedia.org/wiki/Philosophy";
        String source = "https://en.wikipedia.org/wiki/Cumberland,_Maryland";

        testConjecture(destination, source, new ArrayList<String>(), staticLimit);
    }

    /**
     * Starts from given URL and follows first link until it finds the destination or exceeds the limit.
     *
     * @param destination
     * @param source
     * @throws IOException
     */
    public static void testConjecture(String destination, String source, ArrayList<String> list, int limit) throws IOException {
        int iteration = staticLimit - limit;
        if (limit < 0) {
            System.out.println("Out of limit iteration");
            return;
        }
        if (destination.equals(source)) {
            System.out.println("Finished with " + iteration + " iteration");
            return;
        }
        if (source.equals("sorry")){
            System.out.println("Sorry, no links");
            return;
        }
        String url = getFirstLink(source, list);
        list.add(source);
        testConjecture(destination, url, list, --limit);
    }

    private static String getFirstLink(String source, ArrayList<String> list) throws IOException {
        Connection conn = Jsoup.connect(source);
        Document doc = conn.get();
        Element content = doc.getElementById("mw-content-text");
        String newSurce = getFirstLinkBySelect(content, "p", list);
        if (newSurce == null) newSurce = getFirstLinkBySelect(content, "ul", list);
        if (newSurce == null) return "sorry";
        else return newSurce;
    }

    private static String getFirstLinkBySelect(Element content, String query, ArrayList<String> list){
            Elements elements = content.select(query);

            for(Node node : elements){

                Iterable<Node> iterable = new WikiNodeIterable(node);
                Iterator<Node> iterator = iterable.iterator();

                boolean isQuotes = false;

                while (iterator.hasNext()){
                    node = iterator.next();
                    boolean isLink = node.nodeName().equals("a");
                    boolean isWiki = node.absUrl("href").contains("wikipedia.org");
                    boolean isRef = node.attr("href").contains("#");
                    //boolean isCursive = node.parent().attr("class").equals("IPA nopopups noexcerpt") || node.attr("class").equals("IPA nopopups noexcerpt");
                    boolean isCursive = node.parent().nodeName().equals("span");
                    boolean isSmall = node.parent().nodeName().equals("small");
                    boolean isRepeat = list.contains(node.absUrl("href"));
                    if (node instanceof TextNode){
                        if (((TextNode) node).getWholeText().contains("(")) isQuotes = true;
                        if (((TextNode) node).getWholeText().contains(")")) isQuotes = false;
                    }
                    if(isLink && isWiki && !isRef && !isCursive && !isRepeat && !isSmall && !isQuotes){
                        System.out.println(node.absUrl("href"));
                        return node.absUrl("href");
                    }
                }
            }
        return null;
    }

    private static void recursiveDFS(Node node) {
        boolean isCursive = node.nodeName().equals("i");
        boolean isRed = node.nodeName().equals("span");
        boolean isLink = node.nodeName().equals("a");
        boolean isWiki = node.absUrl("href").contains("wikipedia.org");
        if (!isCursive && !isRed){
            if (isLink && isWiki){
                System.out.println(node.absUrl("href"));
                return;
            }
            for (Node child: node.childNodes()) {
            recursiveDFS(child);
            }
        }
    }
}
