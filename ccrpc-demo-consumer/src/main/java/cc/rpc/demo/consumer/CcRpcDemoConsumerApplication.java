package cc.rpc.demo.consumer;

import cc.rpc.core.annotation.CcConsumer;
import cc.rpc.demo.api.UserService;
import jakarta.annotation.Resource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@ComponentScan("cc.rpc")
public class CcRpcDemoConsumerApplication {


    public static void main(String[] args) {
        SpringApplication.run(CcRpcDemoConsumerApplication.class, args);
    }

    @CcConsumer
    UserService userService;

    @GetMapping("/findId")
    public Integer findId(@RequestParam("id") Integer id) {
        return userService.findId(id);
    }
}
