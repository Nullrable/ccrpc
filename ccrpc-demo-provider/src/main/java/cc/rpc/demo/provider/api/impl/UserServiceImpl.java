package cc.rpc.demo.provider.api.impl;

import cc.rpc.core.annotation.CcProvider;
import cc.rpc.core.api.CcRpcException;
import cc.rpc.core.api.RpcContext;
import cc.rpc.demo.api.User;
import cc.rpc.demo.api.UserService;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;

/**
 * @author nhsoft.lsd
 */
@CcProvider
@Slf4j
public class UserServiceImpl implements UserService {

    @Override
    public String findId(final Integer id) {
        return environment.getProperty("server.port");
    }

    @Override
    public String findId(final Integer id, final String name) {
        return id + "_" + name;
    }

    @Resource
    private Environment environment;

    @Override
    public List<User> listAll() {
        List<User> users = new ArrayList<>();
        User user = new User(1, "LSD" + environment.getProperty("server.port"));
        users.add(user);

        User user2 = new User(2, "LSD2"+ environment.getProperty("server.port"));
        users.add(user2);
        return users;
    }

    @Override
    public List<User> findById(final List<Integer> ids) {
        List<User> users = new ArrayList<>();
        ids.forEach(id -> {
            users.add(new User(id, id + "_LSD"+ environment.getProperty("server.port")));
        });
        return users;
    }

    @Override
    public Integer[] arrayById() {
        return new Integer[]{1, 2, 3};
    }

    @Override
    public List<User> findByArray(final Integer[] ids) {
        List<User> users = new ArrayList<>();
        Arrays.stream(ids).forEach(id -> {
            users.add(new User(id, id + "_LSD"+ environment.getProperty("server.port")));
        });
        return users;
    }

    @Override
    public User read(final Integer id) {
        return new User(id, id + "_LSD" + environment.getProperty("server.port"));
    }

    @Override
    public boolean save(final User user) {
        return true;
    }

    @Override
    public Map<String, User> map(final User user) {
        Map<String, User> map = new HashMap<>();
        map.put("test", user);
        return map;
    }

    @Override
    public double findDouble(final Integer id) {
        return 1.11;
    }

    @Override
    public Integer findInteger(final long id) {
        return Integer.valueOf(String.valueOf(id));
    }

    @Override
    public User[] arrayUser() {
        return new User[]{new User(1, "LSD Array" + environment.getProperty("server.port"))};
    }

    @Override
    public List<User> saveList(final List<User> userList, int id) {
        userList.forEach(user -> log.info(user.getName()+ "_FromConsumer"));

        return userList;
    }

    @Override
    public Map<String, User> saveMap(final Map<String, User> map) {
        map.values().forEach(user ->log.info("saveMap====>" + user.getName()+ "_FromConsumer"));
        return map;
    }

    @Override
    public List<Map<String, User>> saveMapList(final List<Map<String, User>> mapList) {
        mapList.forEach(map ->  map.values().forEach(user ->log.info("saveMapList====>" + user.getName()+ "_FromConsumer")));
        return mapList;
    }

    String ports = "9002";
    @Override
    public User timeout(int timout) {

        System.out.println(" =======> 跨应用上下文调用：" + RpcContext.get("lsd"));

        String port = environment.getProperty("server.port");

        assert port != null;
        if (port.equals(ports)) {
            try {
                Thread.sleep(timout);
            } catch (InterruptedException e) {
                throw new CcRpcException(e);
            }
        }
        return new User(1, 1 + "_timeout_" + port);
    }

    @Override
    public void setPort(final String port) {
        setPorts(port);
    }

    public void setPorts(final String ports) {
        this.ports = ports;
    }
}
