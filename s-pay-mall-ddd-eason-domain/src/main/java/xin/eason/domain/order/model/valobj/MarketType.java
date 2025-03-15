package xin.eason.domain.order.model.valobj;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum MarketType {
    NO_MARKET(0, "无营销"),
    MARKETED(1, "拼团营销");

    @EnumValue
    private final Integer code;
    @JsonValue
    private final String desc;

    MarketType(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
