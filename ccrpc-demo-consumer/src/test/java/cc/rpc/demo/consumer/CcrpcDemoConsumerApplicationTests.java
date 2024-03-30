package cc.rpc.demo.consumer;

import cc.rpc.demo.provider.CcRpcDemoProviderApplication;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@AutoConfigureMockMvc
@SpringBootTest(classes = CcRpcDemoConsumerApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(value = "test")
class CcrpcDemoConsumerApplicationTests {

    @BeforeAll
    public static void init() {
        System.out.println("starting consumer BeforeAll");
        SpringApplication.run(CcRpcDemoProviderApplication.class, "--server.port=7001");
    }

    @Test
    public void contextLoad() {
        System.out.println("starting consumer test");
    }

}
