package xin.eason.trigger.http;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xin.eason.domain.auth.service.ILoginService;
import xin.eason.types.common.Result;

import java.util.Map;

@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/api/v1/login")
@RequiredArgsConstructor
public class LoginController {

    private final ILoginService loginService;

    /**
     * 用户获取 <b>Ticket</b> 的接口
     * @return <b>Ticket</b>
     */
    @GetMapping("/get_ticket")
    public Result<String> getTicket(){
        String ticket = loginService.getTicket();
        return Result.success(ticket);
    }

    /**
     * 前端轮训登录状态接口
     * @param ticket 之前获取到的用于生成二维码的 <b>Ticket</b>
     * @return 用户信息的视图对象
     */
    @GetMapping("/check_login")
    public Result<Map<String, String>> checkLogin(String ticket){
        Map<String, String> userInfoMap = loginService.checkLogin(ticket);
        return Result.success(userInfoMap);
    }
}
