package xin.eason.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.converter.jackson.JacksonConverterFactory;
import retrofit2.Retrofit;
import xin.eason.infrastructure.gateway.IWechatService;

@Configuration
public class RetrofitConfig {

    private static final String BASE_URL = "https://api.weixin.qq.com/";

    @Bean
    public Retrofit retrofit() {
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(JacksonConverterFactory.create(
                        new ObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                )).build();
    }

    @Bean
    public IWechatService iWechatService(Retrofit retrofit) {
        return retrofit.create(IWechatService.class);
    }
}
