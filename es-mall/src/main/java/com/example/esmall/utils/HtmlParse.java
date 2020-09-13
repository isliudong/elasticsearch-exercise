package com.example.esmall.utils;


import com.example.esmall.dto.JdContent;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 28415@hand-china.com 2020/08/26 20:43
 */
public class HtmlParse {
    public static void main(String[] args) throws IOException {
        new HtmlParse().parseJd("苹果").forEach(System.out::println);
    }

    /**
     * 解析网页获取商品详情
     * @return 商品列表
     * @throws IOException 解析失败
     */
    public List<JdContent> parseJd(String keyWorld) throws IOException {
        ArrayList<JdContent> goods = new ArrayList<>();
        String url="https://search.jd.com/Search?keyword="+keyWorld;
        Document document= Jsoup.parse(new URL(url),30000);
        Element goodsList = document.getElementById("J_goodsList");
        Elements elements = goodsList.getElementsByTag("li");

        for (Element element : elements) {
            String img = element.getElementsByTag("img").eq(0).attr("src");
            String price = element.getElementsByClass("p-price").eq(0).text();
            String title = element.getElementsByClass("p-name").eq(0).text();
            JdContent good = new JdContent(img, title, price);
            if(!good.getTitle().isEmpty()){
                goods.add(good);
            }
        }
        return goods;
    }


}
