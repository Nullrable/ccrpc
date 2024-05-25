package cc.rpc.core.api;

/**
 * @author nhsoft.lsd
 */
public class CcRpcException extends RuntimeException {
    public CcRpcException() {

    }

    public CcRpcException(final String message) {
        super(message);
    }

    public CcRpcException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public CcRpcException(final Throwable cause) {
        super(cause);
    }

    //X 表示技术性错误：比如 X001 表示 noSuchMethod
    //Y 表示业务异常
    //Z 表示暂时确认不了的错误

    public static final String RESPONSE_NULL = "X001-" + "http response body is null";
    public static final String ILLEGAL_ACCESS_EX = "X002-" + "IllegalAccessException";
    public static final String READ_TIMEOUT_EX = "X003-" + "read timeout";
    public static final String TRAFFIC_LIMIT = "X004-" + "traffic controller limit";
    public static final String APP_RETRIES_MUST_GATHER_THAN_ZERO = "X004-" + "app.retries must gather than 0";

    public static final String UNKNOWN = "Z001-" + "unknown error ";
}
