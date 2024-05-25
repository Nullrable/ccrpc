package cc.rpc.demo.consumer;

import cc.rpc.core.annotation.CcConsumer;
import cc.rpc.core.api.CcRpcException;
import cc.rpc.core.config.ConsumerProperties;
import cc.rpc.demo.api.User;
import cc.rpc.demo.api.UserService;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author nhsoft.lsd
 */
@RestController
@Configuration
public class ConsumerController {

    @CcConsumer
    UserService userService;

    @Resource
    private ConsumerProperties consumerProperties;

    @Resource
    private Environment environment;

    @GetMapping("/findId")
    public String findId(@RequestParam("id") Integer id) {
        try {
            return userService.findId(id);
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @GetMapping("/getFaultLimit")
    public int getFaultLimit() {
        return consumerProperties.getFaultLimit();
    }

    @GetMapping("/exception")
    public String exception() {
        try {
            userService.exception();
            return "invoker exception";
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @GetMapping("/timeout")
    public String timout() {
        try {
            User user = userService.timeout(2000);
            return user.toString();
        } catch (Exception e) {
            return e.getMessage();
        }
    }
}
