package org.moera.naming;

import java.security.Security;
import java.util.ArrayList;
import java.util.List;
import jakarta.inject.Inject;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.moera.naming.rpc.CleanRequestIdInterceptor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
@EnableScheduling
public class MoeraNamingApplication implements WebMvcConfigurer {

    @Inject
    private CleanRequestIdInterceptor cleanRequestIdInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(cleanRequestIdInterceptor);
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        for (HttpMessageConverter<?> converter : converters) {
            if (converter instanceof MappingJackson2HttpMessageConverter jacksonConverter) {
                List<MediaType> supportedMediaTypes = new ArrayList<>(jacksonConverter.getSupportedMediaTypes());
                supportedMediaTypes.add(MediaType.valueOf("application/json-rpc"));
                jacksonConverter.setSupportedMediaTypes(supportedMediaTypes);
            }
        }
    }

    public static void main(String[] args) {
        Security.addProvider(new BouncyCastleProvider());
        SpringApplication.run(MoeraNamingApplication.class, args);
    }

}
