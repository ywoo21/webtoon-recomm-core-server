package artntech.ccp.webtoonrecomm.api;

import artntech.ccp.webtoonrecomm.domain.Webtoon;
import artntech.ccp.webtoonrecomm.dto.WebtoonRes;
import artntech.ccp.webtoonrecomm.service.impl.WebtoonCrawlingServiceImpl;
import artntech.ccp.webtoonrecomm.service.impl.WebtoonServiceImpl;

import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

/**
 * 메인 API 컨트롤러
 */
@RestController
public class WebtoonController {

    private final WebtoonCrawlingServiceImpl webtoonCrawlingService;
    private final WebtoonServiceImpl webtoonImageFeatureService;

    public WebtoonController(final WebtoonCrawlingServiceImpl webtoonCrawlingService,
                             final WebtoonServiceImpl webtoonImageFeatureService){
        this.webtoonCrawlingService = webtoonCrawlingService;
        this.webtoonImageFeatureService = webtoonImageFeatureService;
    }

    /**
     * 웹툰 크롤링 + ES Index Request
     * @throws IOException
     */
    @GetMapping("/save")
    public void crawlAndSave() throws IOException {
        webtoonImageFeatureService.saveAll();
    }

    /**
     * 웹툰 검색 조회
     * @return List<WebtoonRes>
     */
    @GetMapping("/search")
    public List<WebtoonRes> getAllSearchedWebtoons(@RequestParam("keyword") final String keyword){
        return webtoonImageFeatureService.getSearchedWebtoons(keyword);
    }

    /**
     * 웹툰 랜덤 조회
     * @return List<WebtoonRes>
     */
    @GetMapping("/random")
    public List<WebtoonRes> getAllWebtoons(){
        return webtoonImageFeatureService.getAllWebtoons();
    }

    /**
     * 웹툰 추천 결과 조회
     * @param webtoon
     * @return
     */
    @PostMapping("/recomm")
    public List<WebtoonRes> getRecommendedWebtoons(@RequestBody final Webtoon webtoon){
        return webtoonImageFeatureService.searchWebtoon(webtoon);
    }
}
