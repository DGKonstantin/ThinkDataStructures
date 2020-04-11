package com.allendowney.thinkdast;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.io.IOException;


public class MyClassKostia {
    public static void main(String[] args) throws IOException {
        String url = "https://en.wikipedia.org/wiki/Java_(programming_language)";

        Connection conn = Jsoup.connect(url);
        Document document = conn.get();

        Element element = document.getElementById("mw-content-text");
        Elements paragrarhs = element.select("p");

        Element para = paragrarhs.get(1);

        Iterable<Node> iter = new WikiNodeIterable(para);
        for(Node node : iter){
            if (node instanceof TextNode)
                System.out.println(node);
        }
    }
}
