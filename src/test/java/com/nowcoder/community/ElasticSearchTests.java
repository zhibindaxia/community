package com.nowcoder.community;

import com.alibaba.fastjson.JSON;
import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.entity.DiscussPost;
import com.sun.javaws.IconUtil;
import org.apache.ibatis.annotations.Delete;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.util.QueryBuilder;
import org.assertj.core.data.Index;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import javax.management.Query;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 *
 */
@SpringBootTest
public class ElasticSearchTests {

    @Autowired
    @Qualifier("restHighLevelClient")
    private RestHighLevelClient client;

    @Autowired
    private DiscussPostMapper discussPostMapper;
//    @Autowired
//    @Qualifier
//    private

    @Test
    void
     testCreateIndex() throws IOException {
        //1.创建索引请求
        CreateIndexRequest request=new CreateIndexRequest("community");
        //2.执行请求,获得响应
        CreateIndexResponse createIndexResponse= client.indices().create(request, RequestOptions.DEFAULT);
        //
        System.out.println(createIndexResponse);
    }

    //测试获取索引
    @Test
    void testEXistIndex() throws IOException {
        GetIndexRequest request=new GetIndexRequest("community");
        boolean exists=client.indices().exists(request,RequestOptions.DEFAULT);
        System.out.println(exists);
    }
    //测试删除索引
    @Test
    void testDelete() throws IOException {
        DeleteIndexRequest request=new DeleteIndexRequest("community");

        AcknowledgedResponse delete=client.indices().delete(request,RequestOptions.DEFAULT);
        System.out.println(delete.isAcknowledged());
    }
    //测试添加文档
    //创建对象
    @Test
    void testAddDocument() throws IOException {
        //创建对象
        IndexRequest request=new IndexRequest("community");
        //规则 put/community/_doc/1
        request.id("1");
        request.timeout(TimeValue.timeValueSeconds(5));
        request.timeout("5s");
    //        discussRepository.save(discussMapper.selectDiscussPostById(241));
    //        discussRepository.save(discussMapper.selectDiscussPostById(242));
    //        discussRepository.save(discussMapper.selectDiscussPostById(243));
        //创建请求

        request.source(JSON.toJSONString(discussPostMapper.selectDiscussPostById(241)), XContentType.JSON);
        IndexResponse indexResponse= client.index(request,RequestOptions.DEFAULT);

        System.out.println(indexResponse.toString());
        System.out.println(indexResponse.status());//对应我们命令返回的状态 CREATED
    }
    //获取文档,判断是否存在get /index/doc/1
    @Test
    void testIsExists() throws IOException {
        GetRequest getRequest=new GetRequest("community","1");

        getRequest.fetchSourceContext(new FetchSourceContext(false));
        getRequest.storedFields("_none_");

        boolean exists=client.exists(getRequest,RequestOptions.DEFAULT);
        System.out.println(exists);
    }
    //获得文档的信息
    @Test
    void testGetDocument() throws IOException {
        GetRequest getRequest=new GetRequest("community","1");
        GetResponse getResponse=client.get(getRequest,RequestOptions.DEFAULT);
        System.out.println(getResponse.getSourceAsString());
        System.out.println(getResponse);
    }
    //更新文档信息
    @Test
    void testUpdateDocument() throws IOException{
        UpdateRequest updateRequest=new UpdateRequest("community","1");
        updateRequest.timeout("5s");
        updateRequest.doc(JSON.toJSONString(discussPostMapper.selectDiscussPostById(242)),XContentType.JSON);
        UpdateResponse updateResponse=client.update(updateRequest,RequestOptions.DEFAULT);

    }
    //删除文档
    @Test
    void testDeleteDocument() throws IOException{
        DeleteRequest deleteRequest=new DeleteRequest("community","1");
        deleteRequest.timeout("5s");

        DeleteResponse deleteResponse=client.delete(deleteRequest,RequestOptions.DEFAULT);

        System.out.println(deleteResponse.status());

    }

    //批量插入数据
    @Test
    void testBulkRequest() throws IOException{
        BulkRequest bulkRequest=new BulkRequest();
        bulkRequest.timeout("10s");

        ArrayList<DiscussPost> discussPostList=new ArrayList<>();
        discussPostList.add(discussPostMapper.selectDiscussPostById(241));
        discussPostList.add(discussPostMapper.selectDiscussPostById(242));
        discussPostList.add(discussPostMapper.selectDiscussPostById(243));
        discussPostList.add(discussPostMapper.selectDiscussPostById(244));
        discussPostList.add(discussPostMapper.selectDiscussPostById(245));

        //批处理请求
        for(int i=0;i<discussPostList.size();i++){
            //批量更新和批量删除,就在这里修改对应的请求就可以了
            bulkRequest.add(
                    new IndexRequest("community")
                            .id(""+(i+1))
                            .source(JSON.toJSONString(discussPostList.get(i)),XContentType.JSON)
            );

        }

        BulkResponse bulkResponse= client.bulk(bulkRequest,RequestOptions.DEFAULT);
        System.out.println(bulkResponse.hasFailures()); //是否失败

    }

    //查询
    //搜索请求
    //条件构造
    @Test
    void testSearch() throws IOException {
         SearchRequest request= new SearchRequest("community");
         //构建搜索的条件
        SearchSourceBuilder sourceBuilder= new SearchSourceBuilder();
        //查询条件 我们使用queryBuilders 工具来实现
        // queryBuilders.termQuery精确查询
        //
        MatchAllQueryBuilder matchAllQueryBuilder=QueryBuilders.matchAllQuery();
        //

//        TermQueryBuilder termQueryBuilder=QueryBuilders.termQuery("title","华人");
        sourceBuilder.query(matchAllQueryBuilder);
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        request.source(sourceBuilder);

        SearchResponse searchResponse= client.search(request,RequestOptions.DEFAULT);

        System.out.println(JSON.toJSONString(searchResponse.getHits()));
        searchResponse.getHits();
        System.out.println("================");
        for (SearchHit documentFields:searchResponse.getHits()) {
            System.out.println(documentFields.getSourceAsMap());
        }

    }
}
