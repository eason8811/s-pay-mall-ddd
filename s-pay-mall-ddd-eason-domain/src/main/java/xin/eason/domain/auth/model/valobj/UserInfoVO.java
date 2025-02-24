package xin.eason.domain.auth.model.valobj;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserInfoVO {
    private String token;
    private String userOpenId;
    private String ticket;
    private String loginStatus;
}
