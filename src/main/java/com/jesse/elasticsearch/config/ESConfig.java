package com.jesse.elasticsearch.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;

/**
 * TODO
 *
 * @author jesse hsj
 * @date 2021/1/5 11:25
 */
@Configuration
public class ESConfig {
    @Bean
    public TransportClient getClient(){
        TransportClient transportClient = null;
        try {
            Settings settings = Settings.builder()
                    .put("cluster.name","hsj").build();
            transportClient = new PreBuiltTransportClient(settings);
            TransportAddress firstAddress = new TransportAddress(InetAddress.getByName("192.168.240.92"),Integer.parseInt("9300"));
            TransportAddress secondAddress = new TransportAddress(InetAddress.getByName("192.168.240.92"),Integer.parseInt("9301"));
            transportClient.addTransportAddress(firstAddress);
            transportClient.addTransportAddress(secondAddress);

        }catch (Exception e){
            e.printStackTrace();

        }
        return transportClient;
    }

    @Bean
    public RestHighLevelClient restHighLevelClient() {

        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost("192.168.240.92", 9200, "http"),
                        new HttpHost("192.168.240.92", 9201, "http")));

        return client;
    }
}
