package spammy.eve.global.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.util.Timeout;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Slf4j
@Configuration
public class AppConfig {

    @Bean
    public RestClient restClient() {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(Timeout.ofSeconds(30))
                .setResponseTimeout(Timeout.ofMinutes(10))
                .build();

        HttpClient httpClient = HttpClientBuilder.create()
                .setDefaultRequestConfig(requestConfig)
                .build();

        HttpComponentsClientHttpRequestFactory factory =
                new HttpComponentsClientHttpRequestFactory(httpClient);

        return RestClient.builder()
                .requestFactory(factory)
                .requestInterceptor((request, body, execution) -> {
                    log.info("ESI 요청: {} {}", request.getMethod(), request.getURI());
                    return execution.execute(request, body);
                })
                .build();
    }
}
