package cc.rpc.demo.consumer;

import cc.rpc.core.annotation.CcConsumer;
import cc.rpc.core.api.LoadBalancer;
import cc.rpc.core.api.RegisterCenter;
import cc.rpc.core.api.Router;
import cc.rpc.core.cluster.RoundRibbonLoadBalancer;
import cc.rpc.core.consumer.ConsumerBootstrap;
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
@RestController
@ComponentScan("cc.rpc")
public class CcRpcDemoConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CcRpcDemoConsumerApplication.class, args);
    }

    @Value("${ccrpc.zkserver}")
    private String zkserver;

    @CcConsumer
    UserService userService;

    @GetMapping("/findId")
    public String findId(@RequestParam("id") Integer id) {
        return userService.findId(id);
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public RegisterCenter registerCenter() {
        return new ZkRegisterCenter(zkserver);
    }

    @Bean
    @Order(Integer.MIN_VALUE)
    public ApplicationRunner createConsumerApplicationRunner(ConsumerBootstrap consumerBootstrap) {
        return x -> {
            consumerBootstrap.start();
        };
    }

    @Bean
    ApplicationRunner start() {
        return x -> {
            String test1 = userService.findId(1);
            System.out.println("==============> test1: " + test1);

            String test2 = userService.findId(1, "李白");
            System.out.println("==============> test2: " + test2);

            List<User> test3 = userService.listAll();
            System.out.println("==============> test3: " + test3.get(0).getName());

            List<User> test4 = userService.findById(List.of(1, 2, 3));
            System.out.println("==============> test4: " + test4.get(0).getId());

            Integer[] test5 = userService.arrayById();
            System.out.println("==============> test5: " + Arrays.toString(test5));

            List<User> test6 = userService.findByArray(new Integer[] {100, 200, 300});
            System.out.println("==============> test6: " + test6);

            User test7 = userService.read(999);
            System.out.println("==============> test7: " + test7.getId() + "_" + test7.getName());

            boolean test8 = userService.save(new User(1, "2"));
            System.out.println("==============> test8: " + test8);

            Map<String, User> test9 = userService.map(new User(1, "131232"));
            System.out.println("==============> test9: " + test9.get("test").getId() + "_" + test9.get("test").getId());

            double test10 = userService.findDouble(888);
            System.out.println("==============> test10: " + test10);

            double test11 = userService.findInteger(888);
            System.out.println("==============> test11: " + test11);

            User[] users = userService.arrayUser();
            System.out.println("==============> test11: " + users[0].getName());

            List<User> usersList = new ArrayList<>();
            usersList.add(new User(1, "0302_LSD"));
            usersList.add(new User(1, "0303_LSD"));
            usersList = userService.saveList(usersList, 1);
            System.out.println("==============> test12: " + usersList.toString());
            Map<String, User> mapuser = new HashMap<>();
            mapuser.put("1", new User(1, "0302_LSD"));
            mapuser.put("2", new User(1, "0303_LSD"));
            mapuser = userService.saveMap(mapuser);
            System.out.println("==============> test13: " + mapuser.toString());

            List<Map<String, User>> mapList = new ArrayList<>();

            Map<String, User> mapuser1 = new HashMap<>();
            mapuser1.put("1", new User(1, "0302_LSD"));
            mapuser.put("2", new User(1, "0303_LSD"));
            mapList.add(mapuser1);

            mapList = userService.saveMapList(mapList);
            System.out.println("==============> test14: " + mapList.toString());

        };
    }
}
