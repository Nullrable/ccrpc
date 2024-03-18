package cc.rpc.demo.api;

/**
 * @author nhsoft.lsd
 */
public interface UserService {

    User findById(Integer id);

    int findId(Integer id);

    int findId(Integer id,String name);
}
