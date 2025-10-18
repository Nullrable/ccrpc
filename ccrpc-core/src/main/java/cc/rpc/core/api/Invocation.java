package cc.rpc.core.api;

import java.util.HashMap;
import java.util.Map;

/**
 * @author nhsoft.lsd
 */
public class Invocation {

    private Map<String, Object> attachments = new HashMap<>();

    public String getConsistentHashArg() {
        return attachments.get("consistentHashArg") == null ? "default" : (String) attachments.get("consistentHashArg");
    }

    public void putConsistentHashArg(String arg) {
        attachments.put("consistentHashArg", arg);
    }

    public void put(String key, Object value) {
        attachments.put(key, value);
    }

    public void getAttachment(String key) {
        attachments.get(key);
    }
}