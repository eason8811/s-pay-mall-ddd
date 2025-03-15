package xin.eason.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.converter.jackson.JacksonConverterFactory;
import retrofit2.Retrofit;
import xin.eason.infrastructure.gateway.IGroupBuyMarketService;
import xin.eason.infrastructure.gateway.IWechatService;

@Configuration
public class RetrofitConfig {

    private static final String WECHAT_BASE_URL = "https://api.weixin.qq.com/";
    private static final String GROUP_BUY_BASE_URL = "http://127.0.0.1:8080/";

    @Bean("wechatRetrofit")
    public Retrofit wechatRetrofit() {
        return new Retrofit.Builder()
                .baseUrl(WECHAT_BASE_URL)
                .addConverterFactory(JacksonConverterFactory.create(
                        new ObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                )).build();
    }

    @Bean("groupBuyRetrofit")
    public Retrofit groupBuyRetrofit() {
        return new Retrofit.Builder()
                .baseUrl(GROUP_BUY_BASE_URL)
                .addConverterFactory(JacksonConverterFactory.create()).build();
    }

    @Bean
    public IWechatService iWechatService(@Qualifier("wechatRetrofit") Retrofit retrofit) {
        return retrofit.create(IWechatService.class);
    }

    @Bean
    public IGroupBuyMarketService iGroupBuyMarketService(@Qualifier("groupBuyRetrofit") Retrofit retrofit) {
        return retrofit.create(IGroupBuyMarketService.class);
    }
}
