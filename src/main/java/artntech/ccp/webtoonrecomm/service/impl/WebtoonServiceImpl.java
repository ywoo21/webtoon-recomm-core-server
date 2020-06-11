package artntech.ccp.webtoonrecomm.service.impl;

import artntech.ccp.webtoonrecomm.domain.Webtoon;
import artntech.ccp.webtoonrecomm.dto.WebtoonRes;
import artntech.ccp.webtoonrecomm.service.WebtoonService;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.lucene.search.function.CombineFunction;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.index.query.functionscore.ScriptScoreQueryBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WebtoonServiceImpl implements WebtoonService {

    @Value("${flask.server.endpoint}")
    private String FLASK_SERVER_ENDPOINT;

    private final WebtoonCrawlingServiceImpl webtoonCrawlingService;
    private final ElasticSearchRestClientServiceImpl elasticSearchRestClientService;
    private final RestHighLevelClient restHighLevelClient;

    public WebtoonServiceImpl(final WebtoonCrawlingServiceImpl webtoonCrawlingService,
                              final ElasticSearchRestClientServiceImpl elasticSearchRestClientService,
                              final RestHighLevelClient restHighLevelClient) {
        this.webtoonCrawlingService = webtoonCrawlingService;
        this.elasticSearchRestClientService = elasticSearchRestClientService;
        this.restHighLevelClient = restHighLevelClient;
    }

    /**
     * 웹툰 크롤링 데이터 전체 저장
     *
     * @throws IOException
     */
    @Override
    public void saveAll() throws IOException {

        List<Webtoon> webtoonList = webtoonCrawlingService.crawlAll();

        webtoonList.forEach(webtoon -> {
            try {
                saveWebtoon(webtoon);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Flask 서버로부터 TF 특징점(imagenet_feature) 응답을 받고, ES 의 'img_list' 에 저장
     *
     * @param webtoon
     * @throws IOException
     */
    @Override
    public void saveWebtoon(final Webtoon webtoon) throws IOException {

        ObjectMapper mapper = new ObjectMapper();

        Map<String, Object> map = new HashMap<>();
        map.put("id", webtoon.getId());
        map.put("imageUrl", webtoon.getImageUrl());
        map.put("title", webtoon.getTitle());

        String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(map);

        HttpClient client = HttpClientBuilder.create().build();

        HttpPost httpPost = new HttpPost(FLASK_SERVER_ENDPOINT + "/saveImagenetFeature");
        httpPost.setEntity(new StringEntity(json, "UTF-8"));
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");

        client.execute(httpPost);
    }

    /**
     * 웹툰 랜덤 조회
     * @return
     */
    @Override
    public List<WebtoonRes> getAllWebtoons() {

        final BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        final FunctionScoreQueryBuilder functionScoreQueryBuilder = QueryBuilders
                .functionScoreQuery(boolQuery, ScoreFunctionBuilders.randomFunction())
                .boostMode(CombineFunction.REPLACE);

        return elasticSearchRestClientService.randomSearchRequestHandler(functionScoreQueryBuilder, "_score",
                0, 100, "img_list");
    }

    /**
     * 웹툰 검색 데이터 조회
     * @return
     */
    public List<WebtoonRes> getSearchedWebtoons(final String searchKeyword) {

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //boolQueryBuilder.(QueryBuilders.wildcardQuery("title", "*" + searchKeyword + "*"));
        boolQueryBuilder.should(QueryBuilders.wildcardQuery("title", "*" + searchKeyword + "*"));
        boolQueryBuilder.should(QueryBuilders.matchPhraseQuery("title", searchKeyword).slop(1));

        return elasticSearchRestClientService.searchRequestHandler(boolQueryBuilder,
                0, 100, "img_list");
    }

    /**
     * @param webtoon
     * @return
     */
    @Override
    public List<WebtoonRes> searchWebtoon(final Webtoon webtoon) {

        try {

            Script script;

            Map<String, Object> params = new HashMap<>();

            /*
             * (4) TF 특징점 - Features from Tensorflow Imagenet Inception_v3 Model
             *             - Using Python Server
             */

            // ES에서 item_id 에 해당하는 imagenet_feature 읽어오기
            String indexName = "img_list";
            String typeName = "_doc";

            String docId = webtoon.getId();

            GetRequest getRequest = new GetRequest(indexName, typeName, docId);
            GetResponse getResponse = restHighLevelClient.get(getRequest, RequestOptions.DEFAULT);

            params.put("imagenet_feature", getResponse.getSourceAsMap().get("imagenet_feature"));

            script = new Script(ScriptType.STORED, null, "calculate-score", params);

            MatchAllQueryBuilder matchAllQueryBuilder = QueryBuilders.matchAllQuery();

            ScriptScoreQueryBuilder scriptScoreQueryBuilder = new ScriptScoreQueryBuilder(matchAllQueryBuilder, script);

            // ES에 Search 요청
            return elasticSearchRestClientService.randomSearchRequestHandler(scriptScoreQueryBuilder, "_score",
                    0, 1000, "img_list");

        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
