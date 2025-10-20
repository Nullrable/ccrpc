package cc.rpc.core.cluster;

import cc.rpc.core.api.Invocation;
import cc.rpc.core.api.LoadBalancer;
import cc.rpc.core.meta.InstanceMeta;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import lombok.Getter;

/**
 * 核心特性
 * 1. 多维度评估
 * 平均响应时间：统计历史请求的平均响应时间
 * 活跃连接数：当前正在处理的请求数量
 * 服务器权重：服务器性能权重配置
 * 2. 综合得分计算
 * 得分 = (平均响应时间 × 活跃连接数) / 权重
 * 得分越低，服务器越优先被选择
 *
 * @author nhsoft.lsd
 */
public class ShortestResponseLoadBalancer implements LoadBalancer {

    private final Map<String, ServerNode> serverNodes = new ConcurrentHashMap<>();

    public ShortestResponseLoadBalancer() {

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        // 定期重置统计数据，避免历史数据影响过大
        scheduler.scheduleAtFixedRate(this::resetStatistics,
                5, 5, TimeUnit.MINUTES);
    }

    // 定期重置统计数据
    private void resetStatistics() {
        for (ServerNode server : serverNodes.values()) {
            // 保留50%的历史数据，避免完全重置(每5分钟衰减一半)
            long currentTotal = server.totalResponseTime.get();
            long currentCount = server.requestCount.get();

            server.totalResponseTime.set(currentTotal / 2);
            server.requestCount.set(currentCount / 2);
        }
    }

    private void addNodes(final List<InstanceMeta> providers) {
        Set<String> currentPaths = providers.stream()
                .map(InstanceMeta::toPath)
                .collect(Collectors.toSet());

        // 移除下线节点
        serverNodes.keySet().removeIf(k -> !currentPaths.contains(k));

        // 增加新节点
        for (InstanceMeta provider : providers) {
            serverNodes.computeIfAbsent(provider.toPath(), k -> new ServerNode(provider))
                    .setInstanceMeta(provider);
        }
    }

    @Override
    public InstanceMeta choose(final List<InstanceMeta> providers, Invocation invocation) {

        //这个有性能问题，serverNodes 每次调用都要判断，性能较差
        addNodes(providers);

        List<ServerNode> servers = serverNodes.values().stream().filter(p -> p.getInstanceMeta().toPath().equals(p.getInstanceMeta().toPath())).toList();

        // 计算所有服务器的得分
        double totalScore = 0;
        Map<ServerNode, Double> scoreMap = new HashMap<>();

        for (ServerNode server : servers) {
            double score = server.calculateScore();
            // 使用倒数，使得得分低的服务器权重更高
            double invertedScore = score > 0 ? 1.0 / score : Double.MAX_VALUE;
            scoreMap.put(server, invertedScore);
            totalScore += invertedScore;
        }

        // 按权重随机选择
        double random = Math.random() * totalScore;
        double currentSum = 0;

        for (Map.Entry<ServerNode, Double> entry : scoreMap.entrySet()) {
            currentSum += entry.getValue();
            if (random <= currentSum) {
                ServerNode selected = entry.getKey();
                selected.incrementConnections();
                return selected.getInstanceMeta();
            }
        }

        ServerNode fallback = servers.get(0);
        fallback.incrementConnections();
        return fallback.getInstanceMeta();

    }

    // 记录请求完成
    public void recordRequest(InstanceMeta instance, long responseTime) {
        ServerNode serverNode = serverNodes.get(instance.toPath());
        if (serverNode == null) {
            return;
        }
        serverNode.recordResponse(responseTime);
        serverNode.decrementConnections();
    }

    @Getter
    private static class ServerNode {

        private InstanceMeta instanceMeta;
        private final AtomicLong totalResponseTime;  // 总响应时间
        private final AtomicLong requestCount;        // 请求计数
        private final AtomicLong activeConnections;   // 活跃连接数
        private final int weight;                     // 服务器权重

        public ServerNode(InstanceMeta instanceMeta) {
            this.instanceMeta = instanceMeta;
            this.totalResponseTime = new AtomicLong(0);
            this.requestCount = new AtomicLong(0);
            this.activeConnections = new AtomicLong(0);
            this.weight = instanceMeta.getWeight() != null ? instanceMeta.getWeight() : 1;
        }

        // 获取平均响应时间
        public long getAverageResponseTime() {
            long count = requestCount.get();
            if (count == 0) {
                return 0;
            }
            return totalResponseTime.get() / count;
        }

        public void setInstanceMeta(final InstanceMeta instanceMeta) {
            this.instanceMeta = instanceMeta;
        }

        // 记录请求响应时间
        public void recordResponse(long responseTime) {
            totalResponseTime.addAndGet(responseTime);
            requestCount.incrementAndGet();
        }

        // 增加活跃连接
        public void incrementConnections() {
            activeConnections.incrementAndGet();
        }

        // 减少活跃连接
        public void decrementConnections() {
            activeConnections.decrementAndGet();
        }

        // 计算综合得分（响应时间越短、权重越高、活跃连接越少，得分越低越好）
        public double calculateScore() {
            long avgTime = getAverageResponseTime();
            long connections = activeConnections.get();

            // 如果没有历史数据，使用权重倒数作为初始得分
            if (avgTime == 0) {
                return (connections + 1) / (double) weight;
            }

            // 综合得分 = (平均响应时间 * 活跃连接数) / 权重
            return (avgTime * (connections + 1)) / (double) weight;
        }

        @Override
        public String toString() {
            return String.format("%s:%d (权重:%d, 平均响应:%dms, 活跃连接:%d)",
                    instanceMeta.getHost(), instanceMeta.getPort(), weight, getAverageResponseTime(), activeConnections.get());
        }
    }
}
