package dev.jeschke.spring.warmup;

import static java.net.http.HttpClient.Redirect.ALWAYS;

import java.net.http.HttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.client.RestClient;

@EnableAsync
@Configuration
@ComponentScan(excludeFilters = @Filter(IgnoreBean.class))
public class WarmUpConfiguration {
    @Bean("warmUpRestClient")
    public RestClient restClient() {
        final var httpClient = HttpClient.newBuilder().followRedirects(ALWAYS).build();
        return RestClient.builder()
                .requestFactory(new JdkClientHttpRequestFactory(httpClient))
                .build();
    }
}
