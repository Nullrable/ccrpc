package cc.rpc.demo.provider;

import cc.rpc.core.api.RpcResponse;
import cc.rpc.core.config.AppProperties;
import cc.rpc.core.config.ProviderProperties;
import cc.rpc.core.config.ZkProperties;
import cc.rpc.demo.api.UserService;
import jakarta.annotation.Resource;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author nhsoft.lsd
 */
@RestController
public class ProviderController {

    @Resource
    UserService userService;

    @Resource
    private ProviderProperties providerProperties;

    @Resource
    private AppProperties appProperties;

    @Resource
    private ZkProperties zkProperties;

    @Resource
    private Environment environment;

    @GetMapping("/setPorts")
    public RpcResponse setPorts(@RequestParam("ports") String ports) {
        userService.setPort(ports);
        RpcResponse response = new RpcResponse(true, ports, null);
        return response;
    }

    @GetMapping("/properties")
    public String properties() {

        System.out.println(environment.getProperty("ccrpc.provider.metas.dc"));

        return appProperties.toString() + " " + providerProperties.toString() + " " + zkProperties.toString();
    }
}
