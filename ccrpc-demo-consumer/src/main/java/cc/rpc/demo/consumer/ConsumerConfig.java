package cc.rpc.demo.consumer;

import cc.rpc.core.annotation.CcConsumer;
import cc.rpc.core.api.Filter;
import cc.rpc.core.api.LoadBalancer;
import cc.rpc.core.api.RegisterCenter;
import cc.rpc.core.api.Router;
import cc.rpc.core.cluster.RoundRibbonLoadBalancer;
import cc.rpc.core.consumer.ConsumerBootstrap;
import cc.rpc.core.filter.CacheFilter;
import cc.rpc.core.filter.MockFilter;
import cc.rpc.core.registry.zk.ZkRegisterCenter;
import cc.rpc.demo.api.User;
import cc.rpc.demo.api.UserService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author nhsoft.lsd
 */
@Configuration
@Data
@Slf4j
public class ConsumerConfig {

    @Bean
    ConsumerBootstrap createConsumerBootstrap() {
        return new ConsumerBootstrap();
    }

    @Bean
    public LoadBalancer loadBalancer() {
        return new RoundRibbonLoadBalancer();
    }
    @Bean
    public Router router() {
        return Router.Default;
    }

    @Value("${ccrpc.zkserver}")
    private String zkserver;

    @Bean
    public RegisterCenter registerCenter() {
        return new ZkRegisterCenter(zkserver);
    }

    @Bean
    public ApplicationRunner providerBootstrapRunner(ConsumerBootstrap consumerBootstrap) {
        return x -> {
            consumerBootstrap.start();
            testCase();
        };
    }

    @Bean
    public Filter filter() {
        return new MockFilter();
    }

    @CcConsumer
    UserService userService;


    private void testCase() {
//        String test1 = userService.findId(1);
//        log.info("==============> test1: " + test1);
//
//        String test2 = userService.findId(1, "李白");
//        log.info("==============> test2: " + test2);
//
        List<User> test3 = userService.listAll();
        log.info("==============> test3: " + test3.get(0).getName());
//
//        List<User> test4 = userService.findById(List.of(1, 2, 3));
//        log.info("==============> test4: " + test4.get(0).getId());
//
//        Integer[] test5 = userService.arrayById();
//        log.info("==============> test5: " + Arrays.toString(test5));
//
//        List<User> test6 = userService.findByArray(new Integer[] {100, 200, 300});
//        log.info("==============> test6: " + test6);
//
//        User test7 = userService.read(999);
//        log.info("==============> test7: " + test7.getId() + "_" + test7.getName());
//
//        boolean test8 = userService.save(new User(1, "2"));
//        log.info("==============> test8: " + test8);
//
//        Map<String, User> test9 = userService.map(new User(1, "131232"));
//        log.info("==============> test9: " + test9.get("test").getId() + "_" + test9.get("test").getId());
//
//        double test10 = userService.findDouble(888);
//        log.info("==============> test10: " + test10);
//
//        double test11 = userService.findInteger(888);
//        log.info("==============> test11: " + test11);
//
//        User[] users = userService.arrayUser();
//        log.info("==============> test11: " + users[0].getName());
//
//        List<User> usersList = new ArrayList<>();
//        usersList.add(new User(1, "0302_LSD"));
//        usersList.add(new User(1, "0303_LSD"));
//        usersList = userService.saveList(usersList, 1);
//        log.info("==============> test12: " + usersList.toString());
//        Map<String, User> mapuser = new HashMap<>();
//        mapuser.put("1", new User(1, "0302_LSD"));
//        mapuser.put("2", new User(1, "0303_LSD"));
//        mapuser = userService.saveMap(mapuser);
//        log.info("==============> test13: " + mapuser.toString());
//
//        List<Map<String, User>> mapList = new ArrayList<>();
//
//        Map<String, User> mapuser1 = new HashMap<>();
//        mapuser1.put("1", new User(1, "0302_LSD"));
//        mapuser.put("2", new User(1, "0303_LSD"));
//        mapList.add(mapuser1);
//
//        mapList = userService.saveMapList(mapList);
//        log.info("==============> test14: " + mapList.toString());
    }
}
