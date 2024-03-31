package cc.rpc.demo.consumer;

import cc.rpc.demo.provider.CcRpcDemoProviderApplication;
import jakarta.annotation.PreDestroy;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

@AutoConfigureMockMvc
@SpringBootTest(classes = CcRpcDemoConsumerApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(value = "test")
class CcrpcDemoConsumerApplicationTests {

    static ApplicationContext context;

    @BeforeAll
    public static void init() {
        System.out.println("starting consumer BeforeAll");
        context = SpringApplication.run(CcRpcDemoProviderApplication.class, "--server.port=7001");
    }

    @Test
    public void contextLoad() {
        System.out.println("starting consumer test");
    }

    @AfterAll
    public static void destory(){
        SpringApplication.exit(context, () -> 1 );
    }

}
