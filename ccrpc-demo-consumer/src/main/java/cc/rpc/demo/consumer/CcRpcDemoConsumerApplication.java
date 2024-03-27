package cc.rpc.demo.consumer;

import cc.rpc.core.annotation.CcConsumer;
import cc.rpc.core.api.LoadBalancer;
import cc.rpc.core.api.RegisterCenter;
import cc.rpc.core.api.Router;
import cc.rpc.core.cluster.RoundRibbonLoadBalancer;
import cc.rpc.core.consumer.ConsumerBootstrap;
import cc.rpc.core.provider.ProviderBootstrap;
import cc.rpc.core.registry.ZkRegisterCenter;
import cc.rpc.demo.api.User;
import cc.rpc.demo.api.UserService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class CcRpcDemoConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CcRpcDemoConsumerApplication.class, args);
    }


}
