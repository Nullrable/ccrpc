package cc.rpc.demo.consumer;

import cc.rpc.core.annotation.CcConsumer;
import cc.rpc.demo.api.User;
import cc.rpc.demo.api.UserService;
import jakarta.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.annotation.Order;
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

    @Bean
    ApplicationRunner start() {
        return x -> {
            int test1 = userService.findId(1);
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
        };
    }
}
