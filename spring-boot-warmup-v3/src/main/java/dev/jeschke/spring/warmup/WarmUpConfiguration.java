package dev.jeschke.spring.warmup;

import static java.net.http.HttpClient.Redirect.ALWAYS;

import java.net.http.HttpClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@AutoConfiguration
@ComponentScan(excludeFilters = @Filter(IgnoreBean.class))
public class WarmUpConfiguration {

    @Bean("defaultWarmUpHttpClient")
    public HttpClient defaultHttpClient() {
        return HttpClient.newBuilder().followRedirects(ALWAYS).build();
    }
}
