package artntech.ccp.webtoonrecomm.service;


import artntech.ccp.webtoonrecomm.domain.Webtoon;
import artntech.ccp.webtoonrecomm.dto.WebtoonRes;

import java.io.IOException;
import java.util.List;

public interface WebtoonService {
    void saveAll() throws IOException;
    void saveWebtoon(final Webtoon webtoon) throws IOException;
    List<WebtoonRes> getAllWebtoons() throws IOException;
    List<WebtoonRes> searchWebtoon(final Webtoon webtoon);
}
