package cc.rpc.demo.api;

import cc.rpc.core.CcProvider;
import org.springframework.stereotype.Component;

/**
 * @author nhsoft.lsd
 */
@CcProvider
@Component
public class UserService {

    public User findById(Integer id){

        return new User(1, "LSD");
    }
}
