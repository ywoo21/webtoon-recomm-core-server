package artntech.ccp.webtoonrecomm.service.impl;

import artntech.ccp.webtoonrecomm.domain.Webtoon;
import artntech.ccp.webtoonrecomm.service.WebtoonCrawlingService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class WebtoonCrawlingServiceImpl implements WebtoonCrawlingService {

    private final String url = "https://comic.naver.com/webtoon/weekday.nhn";

    @Override
    public List<Webtoon> crawlAll() throws IOException {

        List<Webtoon> webtoonList = new ArrayList<>();

        // 웹툰 전체 리스트 페이지를 최초로 Jsoup을 통해 연결
        Document doc = Jsoup.connect(url).get();
        Elements elements =  doc.select(".thumb").select("a").select("img");

        int id = 0;

        for(Element element : elements){

            String imageUrl = element.attr("src");
            String title = element.attr("title");

            Webtoon webtoon =
                    Webtoon.builder()
                            .id(String.valueOf(id++))
                            .imageUrl(imageUrl)
                            .title(title)
                            .build();

            webtoonList.add(webtoon);
        }
        return webtoonList;
    }
}
