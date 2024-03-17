package cc.rpc.demo.provider.api.impl;

import cc.rpc.core.annotation.CcProvider;
import cc.rpc.demo.api.User;
import cc.rpc.demo.api.UserService;
import org.springframework.stereotype.Component;

/**
 * @author nhsoft.lsd
 */
@CcProvider
@Component
public class UserServiceImpl implements UserService {

    @Override
    public User findById(final Integer id) {
        return new User(1, "LSD");
    }

    @Override
    public int findId(final Integer id) {
        return id;
    }
}
