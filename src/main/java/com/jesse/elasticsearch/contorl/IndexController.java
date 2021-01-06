package com.jesse.elasticsearch.contorl;

import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * TODO
 *
 * @author jesse hsj
 * @date 2021/1/6 15:06
 */
@RestController("index")
public class IndexController {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @GetMapping
    public boolean create(@RequestParam(name="index")String index){
        // 1、创建 创建索引request 参数：索引名mess
        CreateIndexRequest request = new CreateIndexRequest(index);

        // 2、设置索引的settings
        request.settings(Settings.builder().put("index.number_of_shards", 2) // 分片数
                .put("index.number_of_replicas", 1) // 副本数
                .put("analysis.analyzer.default.tokenizer", "ik_smart") // 默认分词器
        );
        String mapping = "{\n" +
                "    \"properties\": {\n" +
                "      \"overview\": {\n" +
                "        \"type\": \"text\",\n" +
                "        \"analyzer\": \"english\",\n" +
                "        \"fields\": {\n" +
                "          \"std\": {\n" +
                "            \"type\": \"text\",\n" +
                "            \"analyzer\": \"standard\"\n" +
                "          }\n" +
                "        }\n" +
                "      },\n" +
                "      \"popularity\": {\n" +
                "        \"type\": \"float\"\n" +
                "      },\n" +
                "      \"title\": {\n" +
                "        \"type\": \"text\",\n" +
                "        \"analyzer\": \"english\",\n" +
                "        \"fields\": {\n" +
                "          \"keyword\": {\n" +
                "            \"type\": \"keyword\",\n" +
                "            \"ignore_above\": 256\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "}";
        request.mapping(mapping,XContentType.JSON);
        try {
            CreateIndexResponse createIndexResponse = restHighLevelClient.indices().create(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


}
