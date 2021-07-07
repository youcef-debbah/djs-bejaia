package dz.ngnex.bean;

import javax.ejb.Local;
import java.util.Map;

@Local
public interface ExtraBean {

    Integer RESTART_WARNING_ID = 1;
    Integer SHUTDOWN_WARNING_ID = 2;

    Integer ADVERTISEMENTS_CATEGORY = 1;

    String get(Integer id);

    Map<Integer, String> getAll(Integer category);

    String put(int id, String value);

    String put(int id, String value, Integer category);

    String remove(Integer id);

    void removeAll(Integer category);

    void clear();

}
