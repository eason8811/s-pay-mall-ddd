package xin.eason.infrastructure.gateway;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import xin.eason.infrastructure.gateway.dto.LockMarketPayOrderRequestDTO;
import xin.eason.infrastructure.gateway.dto.LockMarketPayOrderResponseDTO;
import xin.eason.infrastructure.gateway.dto.SettlementOrderRequestDTO;
import xin.eason.infrastructure.gateway.dto.SettlementOrderResponseDTO;
import xin.eason.types.common.Result;

public interface IGroupBuyMarketService {
    /**
     * 营销锁单
     *
     * @param requestDTO 锁单商品信息
     * @return 锁单结果信息
     */
    @POST("api/v1/gbm/trade/lock_market_pay_order")
    Call<Result<LockMarketPayOrderResponseDTO>> lockMarketPayOrder(@Body LockMarketPayOrderRequestDTO requestDTO);
    /**
     * 营销结算
     *
     * @param requestDTO 结算商品信息
     * @return 结算结果信息
     */
    @POST("api/v1/gbm/trade/settlement_market_pay_order")
    Call<Result<SettlementOrderResponseDTO>> settlementMarketPayOrder(@Body SettlementOrderRequestDTO requestDTO);
}
