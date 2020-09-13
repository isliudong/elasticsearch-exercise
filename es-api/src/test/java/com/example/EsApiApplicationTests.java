package com.example;

import com.alibaba.fastjson.JSON;
import com.example.model.User;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class EsApiApplicationTests {

    @Autowired
    RestHighLevelClient restHighLevelClient;

    //创建索引
    @Test
    void test1() throws IOException {
        //创建索引请求
        CreateIndexRequest request = new CreateIndexRequest("ld_index");
        //创建客户端执行请求
        CreateIndexResponse indexResponse = restHighLevelClient.indices().create(request, RequestOptions.DEFAULT);

        System.out.println(indexResponse);
    }
    //获取索引
    @Test
    void test2() throws IOException {
        GetIndexRequest request=new GetIndexRequest("ld_index");
        boolean exists = restHighLevelClient.indices().exists(request, RequestOptions.DEFAULT);
        System.out.println(exists);
    }
    //删除索引
    @Test
    void test3() throws IOException {
        DeleteIndexRequest request = new DeleteIndexRequest("ld_index");
        AcknowledgedResponse delete = restHighLevelClient.indices().delete(request, RequestOptions.DEFAULT);
        System.out.println(delete.isAcknowledged());
    }

    //添加文档
    @Test
    void test4() throws IOException {
        User user=new User("ld",21);
        IndexRequest request = new IndexRequest("ld_index");
        request.id("");
        request.timeout(TimeValue.timeValueSeconds(1));
        request.source(JSON.toJSONString(user), XContentType.JSON);

        IndexResponse response = restHighLevelClient.index(request, RequestOptions.DEFAULT);

        System.out.println(response.toString());
        System.out.println(response.status());

    }
    //批量插入
    @Test
    void test5() throws IOException {
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.timeout("10s");
        ArrayList<User> users = new ArrayList<>();
        users.add(new User("ld1",1));
        users.add(new User("ld2",2));
        users.add(new User("ld3",3));
        users.add(new User("ld4",4));
        users.add(new User("ld5",1));
        users.add(new User("ld6",1));

        for (User user : users) {
            bulkRequest.add(new IndexRequest("ld_index").source(JSON.toJSONString(user),XContentType.JSON));
        }

        BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        System.out.println(bulkResponse.hasFailures());
    }

    //获取文档信息
    @Test
    void test6() throws IOException {
        GetRequest request = new GetRequest("ld_index","0nuCK3QBzo80VenQJz-0");
        GetResponse response = restHighLevelClient.get(request, RequestOptions.DEFAULT);
        System.out.println(response.getSourceAsString());
        System.out.println(response);
    }

    //批量查询
    @Test
    void test7() throws IOException {
        SearchRequest request = new SearchRequest("ld_index");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("name", "ld1");

        searchSourceBuilder.query(termQueryBuilder);
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        request.source(searchSourceBuilder);
        SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        System.out.println(JSON.toJSONString(response.getHits()));
        System.out.println("=====================");

        for (SearchHit fields : response.getHits().getHits()) {
            System.out.println(fields.getSourceAsMap());
        }
    }

}
