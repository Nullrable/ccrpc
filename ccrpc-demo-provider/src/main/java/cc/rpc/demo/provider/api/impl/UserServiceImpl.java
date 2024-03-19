package cc.rpc.demo.provider.api.impl;

import cc.rpc.core.annotation.CcProvider;
import cc.rpc.demo.api.User;
import cc.rpc.demo.api.UserService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * @author nhsoft.lsd
 */
@CcProvider
@Component
public class UserServiceImpl implements UserService {

    @Override
    public int findId(final Integer id) {
        return id;
    }

    @Override
    public String findId(final Integer id, final String name) {
        return id + "_" + name;
    }

    @Override
    public List<User> listAll() {
        List<User> users = new ArrayList<>();
        User user = new User(1, "LSD");
        users.add(user);

        User user2 = new User(2, "LSD2");
        users.add(user2);
        return users;
    }

    @Override
    public List<User> findById(final List<Integer> ids) {
        List<User> users = new ArrayList<>();
        ids.forEach(id -> {
            users.add(new User(id, id + "_LSD"));
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
            users.add(new User(id, id + "_LSD"));
        });
        return users;
    }

    @Override
    public User read(final Integer id) {
        return new User(id, id + "_LSD");
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
        return new User[]{new User(1, "1231232")};
    }
}
