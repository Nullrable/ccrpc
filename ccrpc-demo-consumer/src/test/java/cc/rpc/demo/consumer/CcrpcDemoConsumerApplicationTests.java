package cc.rpc.demo.consumer;

import cc.rpc.demo.provider.CcRpcDemoProviderApplication;
import java.io.IOException;
import org.apache.curator.test.TestingServer;
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

    static TestingServer zkServer;

    @BeforeAll
    public static void init() throws Exception {
        System.out.println("starting consumer...");

        zkServer = new TestingServer(2182, true);

        context = SpringApplication.run(CcRpcDemoProviderApplication.class, "--server.port=7001", "--ccrpc.zk.server=localhost:2182");

    }

    @Test
    public void contextLoad() {
        System.out.println("starting consumer test");
    }

    @AfterAll
    public static void destroy() throws IOException {
        System.out.println("stopping consumer...");
        SpringApplication.exit(context, () -> 1 );
        zkServer.stop();

    }


}
