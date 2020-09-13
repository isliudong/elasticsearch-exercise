package com.example.esmall.service;

import javax.swing.text.Highlighter;

import com.alibaba.fastjson.JSON;
import com.example.esmall.dto.JdContent;
import com.example.esmall.utils.HtmlParse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author 28415@hand-china.com 2020/08/27 09:37
 */
@Service
public class MallService {
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    /**
     * 爬取jd数据到es
     */
    public void initEs(String keyWord) throws IOException {
        //抓取数据
        List<JdContent> jdContents = new HtmlParse().parseJd(keyWord);
        //数据放入es
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.timeout("2m");
        for (JdContent content : jdContents) {
            bulkRequest.add(
                    new IndexRequest("jd_goods")
                    .source(JSON.toJSONString(content), XContentType.JSON)
            );
        }

        //执行请求
        BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);

        bulk.hasFailures();
    }

    /**
     * es关键字搜索
     */
    public List<Map<String,Object>> search(String keyword,int page, int size) throws IOException {

        List<Map<String,Object>> list =new ArrayList<>();
        //构建搜索
        SearchRequest searchRequest = new SearchRequest("jd_goods");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        //精准匹配
        TermQueryBuilder termQuery = QueryBuilders.termQuery("title", keyword);
        sourceBuilder.query(termQuery);
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        sourceBuilder.from(page);
        sourceBuilder.size(size);

        //执行搜索
        searchRequest.source(sourceBuilder);
        SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        //解析
        for (SearchHit fields : response.getHits().getHits()) {
            list.add(fields.getSourceAsMap());
        }

        return list;

    }/**
     * es关键字搜索高亮
     */
    public List<Map<String,Object>> searchKeyLight(String keyword,int page, int size) throws IOException {

        List<Map<String,Object>> list =new ArrayList<>();
        //构建搜索
        SearchRequest searchRequest = new SearchRequest("jd_goods");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");
        highlightBuilder.preTags("<span style='color:red'>");
        highlightBuilder.postTags("</span>");
        //多高亮
        highlightBuilder.requireFieldMatch(false);

        //精准匹配
        TermQueryBuilder termQuery = QueryBuilders.termQuery("title", keyword);
        sourceBuilder.query(termQuery);
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        sourceBuilder.highlighter(highlightBuilder);
        sourceBuilder.from(page);
        sourceBuilder.size(size);

        //执行搜索
        searchRequest.source(sourceBuilder);
        SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        //解析,高亮字段替换原有字段
        for (SearchHit fields : response.getHits().getHits()) {
            Map<String, HighlightField> highlightFields = fields.getHighlightFields();
            HighlightField title = highlightFields.get("title");
            Map<String, Object> sourceAsMap = fields.getSourceAsMap();
            if (title!=null){
                Text[] fragments = title.fragments();
                StringBuilder title2= new StringBuilder();
                for (Text fragment : fragments) {
                    title2.append(fragment);
                }
                sourceAsMap.put("title",title2);
            }
            list.add(sourceAsMap);
        }

        return list;

    }
}
