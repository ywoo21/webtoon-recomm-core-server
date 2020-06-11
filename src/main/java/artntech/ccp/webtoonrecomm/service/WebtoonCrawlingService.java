package artntech.ccp.webtoonrecomm.service;

import artntech.ccp.webtoonrecomm.domain.Webtoon;

import java.io.IOException;
import java.util.List;

public interface WebtoonCrawlingService {
    List<Webtoon> crawlAll() throws IOException;
}
