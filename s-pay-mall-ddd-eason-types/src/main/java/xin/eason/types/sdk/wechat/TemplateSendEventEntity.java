package xin.eason.types.sdk.wechat;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

@Data
@XStreamAlias("xml")
public class TemplateSendEventEntity {
    @XStreamAlias("ToUserName")
    private String toUserName;

    @XStreamAlias("FromUserName")
    private String fromUserName;

    @XStreamAlias("CreateTime")
    private String createTime;

    @XStreamAlias("MsgType")
    private String msgType;

    @XStreamAlias("Event")
    private String event;

    @XStreamAlias("MsgID")
    private String msgId;

    @XStreamAlias("Status")
    private String status;
}
