package cc.rpc.demo.api;

import java.util.List;
import java.util.Map;

/**
 * @author nhsoft.lsd
 */
public interface UserService {

    String findId(Integer id);

    String findId(Integer id, String name);

    List<User> listAll();

    List<User> findById(List<Integer> ids);

    Integer[] arrayById();

    List<User> findByArray(Integer[] ids);

    User read(Integer id);

    boolean save(User user);

    Map<String, User> map(User user);

    double findDouble(Integer id);

    Integer findInteger(long id);

    User[] arrayUser();

    List<User> saveList(List<User> userList, int id);

    Map<String, User> saveMap(Map<String, User> map);

    List<Map<String, User>> saveMapList(List<Map<String, User>> mapList);

}
