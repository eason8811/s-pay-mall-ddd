package xin.eason.infrastructure.gateway.dto;

import lombok.Data;

/**
 * 用于封装请求微信公众平台接口后返回的 <b>二维码</b> 信息
 */
@Data
public class WechatQrCodeResponseDTO {
    private String ticket;

    private Integer expireSeconds;

    private String url;

    private Integer errcode;

    private String errmsg;
}
