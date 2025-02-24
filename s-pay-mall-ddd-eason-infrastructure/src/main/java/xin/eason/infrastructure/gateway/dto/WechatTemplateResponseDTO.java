package xin.eason.infrastructure.gateway.dto;

import lombok.Data;

@Data
public class WechatTemplateResponseDTO {
    private String errmsg;
    private Integer errcode;
    private Long msgid;
}
