package cc.rpc.core.api;

/**
 * @author nhsoft.lsd
 */
public interface Filter {

    /**
     * 返回不等于null 直接返回 ，如果return null filter 继续往下走。 //TODO nhsoft.lsd 这个实现方式不是很好理解，可以改为责任链模式
     * @param request 请求
     * @return 响应结果
     */
    Object preFilter(RpcRequest request);

    /**
     * 返回不等于null 直接返回 ，如果return null filter 继续往下走
     *
     * @param request 请求
     * @param data 响应结果
     * @return 响应结果
     */
    Object postFilter(RpcRequest request, Object data);
}
