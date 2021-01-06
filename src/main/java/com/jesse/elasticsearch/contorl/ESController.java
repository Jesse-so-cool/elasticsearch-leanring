package com.jesse.elasticsearch.contorl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jesse.elasticsearch.entity.Movie;
import com.opencsv.CSVReader;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.*;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.document.DocumentField;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.AbstractQueryBuilder;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO
 *
 * @author jesse hsj
 * @date 2021/1/5 11:23
 */
@Controller("/es")
@RequestMapping("/es")
public class ESController {

    //@Autowired
    //private TransportClient transportClient;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @RequestMapping("/search")
    @ResponseBody
    public List<Movie> get(@RequestParam(name="k")String k){

        IndexRequest indexRequest = new IndexRequest("movie");
        String source = "{\n" +
                "      \"_source\": [\"title\",\"overview\"],\n" +
                "      \"size\": 20, \n" +
                "      \"query\": {\n" +
                "          \"multi_match\": {\n" +
                "              \"type\": \"most_fields\", \n" +
                "              \"query\": \"" + k +"\",\n" +
                "              \"fields\": [\"overview^10\"]\n" +
                "          }\n" +
                "      },\n" +
                "      \"highlight\" : {\n" +
                "            \"fields\" : {\n" +
                "              \"overview\" : { \"pre_tags\" : [\"<em>\"], \"post_tags\" : [\"</em>\"] \n" +
                "                \n" +
                "              },\n" +
                "              \"title\" : { \"pre_tags\" : [\"<em>\"], \"post_tags\" : [\"</em>\"] \n" +
                "              }\n" +
                "            }\n" +
                "        }\n" +
                "  }";
        SearchRequest request = new SearchRequest("movie");
        AbstractQueryBuilder query = null;
        if (k.equals("")){
            query = QueryBuilders.matchAllQuery();
        }else {
            query = QueryBuilders.multiMatchQuery(k,"title","overview");
        }
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(query);
        sourceBuilder.from(0);
        sourceBuilder.size(20);
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");
        highlightBuilder.field("overview");
        highlightBuilder.preTags("<span style=\"color:red\">");
        highlightBuilder.postTags("</span>");
        sourceBuilder.highlighter(highlightBuilder);
        request.source(sourceBuilder);
        try {
            SearchResponse search = restHighLevelClient.search(request, RequestOptions.DEFAULT);
            long total = search.getHits().getTotalHits().value;
            System.out.println(total);
            SearchHit[] hits = search.getHits().getHits();
            List<Movie> ls = new ArrayList<>();
            for (SearchHit hit : hits) {
                String sourceAsString = hit.getSourceAsString();
                Movie movie = JSON.parseObject(sourceAsString, Movie.class);
                //Movie movie = new Movie();
                Map<String, HighlightField> highlightFields = hit.getHighlightFields();
                HighlightField title= highlightFields.get("title");
                if (title != null) {
                    String res = "";
                    for (Text text : title.fragments()) {
                        res += text.string();
                    }
                    movie.setTitle(res);
                }
                HighlightField overview= highlightFields.get("overview");
                if (overview != null) {
                    String res = "";
                    for (Text text : overview.fragments()) {
                        res += text.string();
                    }
                    movie.setOverview(res);
                }
                ls.add(movie);
            }
            return ls;
        } catch (IOException e) {
            e.printStackTrace();
        }
        //indexRequest.source(source,XContentType.JSON);
//        try {
//            IndexResponse indexResponse = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
//            return new ResponseEntity(indexResponse, HttpStatus.OK);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        return null;
    }



    @RequestMapping("/importdata")
    @ResponseBody
    public ResponseEntity importdata() throws IOException {
        importHighLevelData();

        return new ResponseEntity("", HttpStatus.OK);
    }

    private void importHighLevelData() throws IOException {

        //批量插入
        BulkRequest bulkRequest = new BulkRequest();
        int lineId = 0;
        InputStreamReader in = new InputStreamReader(new FileInputStream("datasource/data_tmdb_5000_movies.csv"), Charset.forName("UTF-8"));
        CSVReader reader = new CSVReader(in, ',');
        List<String[]> allRecords = reader.readAll();
        for (String[] records : allRecords) {
            lineId++;
            if (lineId == 1) {
                continue;
            }
            try {
                //index 索引
                IndexRequest indexRequest = new IndexRequest("movie");
                Map<String, Object> data = new HashMap<>();
//                String date = records[10];
//                if(date == null || date.equals("")){
//                    date = "1970/01/01";
//                }
                //data.put("date",date);
                data.put("title",records[16]);
                data.put("overview",records[6]);
                indexRequest.source(JSON.toJSONString(data),XContentType.JSON);
                //restHighLevelClient.index(indexRequest,RequestOptions.DEFAULT);
                bulkRequest.add(indexRequest);
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
        restHighLevelClient.bulk(bulkRequest,RequestOptions.DEFAULT);

    }

    private void importData() throws IOException {
        //批量插入
        BulkRequest bulkRequest = new BulkRequest();
        int lineId = 0;
        InputStreamReader in = new InputStreamReader(new FileInputStream("datasource/data_tmdb_5000_movies.csv"), Charset.forName("UTF-8"));
        CSVReader reader = new CSVReader(in, ',');
        List<String[]> allRecords = reader.readAll();
        for (String[] records : allRecords) {
            lineId++;
            if(lineId == 1){
                continue;
            }
            try{
                String date = records[10];
                if(date == null || date.equals("")){
                    date = "1970/01/01";
                }
                //IndexRequest一条索引记录
                IndexRequest indexRequest = new IndexRequest("movie", "_doc", String.valueOf(lineId - 1)).source(XContentType.JSON,
                        "title", records[16],
                        "tagline", records[15],
                        "release_date", date,
                        //"cast",cast,
                        "overview", records[6]);
                bulkRequest.add(indexRequest);

            }catch(Exception ex){
                ex.printStackTrace();
            }
        }
        reader.close();
//        transportClient.bulk(bulkRequest, new ActionListener<BulkResponse>() {
//            @Override
//            public void onResponse(BulkResponse bulkItemResponses) {
//                System.out.println(bulkItemResponses);
//            }
//
//            @Override
//            public void onFailure(Exception e) {
//                System.out.println(e);
//            }
//        });
    }
}
