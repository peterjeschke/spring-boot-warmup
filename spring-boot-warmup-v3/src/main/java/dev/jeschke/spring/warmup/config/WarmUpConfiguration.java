package dev.jeschke.spring.warmup.config;

import static java.net.http.HttpClient.Redirect.ALWAYS;

import dev.jeschke.spring.warmup.IgnoreBean;
import java.net.http.HttpClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@AutoConfiguration
@EnableConfigurationProperties(WarmUpConfigurationProperties.class)
@ComponentScan(value = "dev.jeschke.spring.warmup", excludeFilters = @Filter(IgnoreBean.class))
public class WarmUpConfiguration {

    @Bean("defaultWarmUpHttpClient")
    public HttpClient.Builder defaultHttpClient() {
        return HttpClient.newBuilder().followRedirects(ALWAYS);
    }
}
