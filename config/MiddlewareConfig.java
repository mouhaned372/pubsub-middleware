package fr.telecom.middleware.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Configuration du middleware chargée depuis JSON.
 */
public class MiddlewareConfig {

    @JsonProperty("name")
    private String name;

    @JsonProperty("version")
    private String version;

    @JsonProperty("maxMemoryKB")
    private int maxMemoryKB;

    @JsonProperty("realtimeEnabled")
    private boolean realtimeEnabled;

    @JsonProperty("defaultThreadPoolSize")
    private int defaultThreadPoolSize;

    @JsonProperty("performance")
    private PerformanceConfig performance;

    @JsonProperty("topics")
    private List<TopicConfig> topics;

    @JsonProperty("faultTolerance")
    private FaultToleranceConfig faultTolerance;

    @JsonProperty("realtime")
    private RealtimeConfig realtime;

    // Getters et Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public int getMaxMemoryKB() { return maxMemoryKB; }
    public void setMaxMemoryKB(int maxMemoryKB) { this.maxMemoryKB = maxMemoryKB; }

    public boolean isRealtimeEnabled() { return realtimeEnabled; }
    public void setRealtimeEnabled(boolean realtimeEnabled) { this.realtimeEnabled = realtimeEnabled; }

    public int getDefaultThreadPoolSize() { return defaultThreadPoolSize; }
    public void setDefaultThreadPoolSize(int defaultThreadPoolSize) { this.defaultThreadPoolSize = defaultThreadPoolSize; }

    public PerformanceConfig getPerformance() { return performance; }
    public void setPerformance(PerformanceConfig performance) { this.performance = performance; }

    public List<TopicConfig> getTopics() { return topics; }
    public void setTopics(List<TopicConfig> topics) { this.topics = topics; }

    public FaultToleranceConfig getFaultTolerance() { return faultTolerance; }
    public void setFaultTolerance(FaultToleranceConfig faultTolerance) { this.faultTolerance = faultTolerance; }

    public RealtimeConfig getRealtime() { return realtime; }
    public void setRealtime(RealtimeConfig realtime) { this.realtime = realtime; }

    // Classes internes pour la configuration
    public static class PerformanceConfig {
        @JsonProperty("monitoringIntervalMs")
        private int monitoringIntervalMs;

        @JsonProperty("enableWorstCaseAnalysis")
        private boolean enableWorstCaseAnalysis;

        @JsonProperty("logLevel")
        private String logLevel;

        @JsonProperty("enableMetrics")
        private boolean enableMetrics;

        public int getMonitoringIntervalMs() { return monitoringIntervalMs; }
        public void setMonitoringIntervalMs(int monitoringIntervalMs) { this.monitoringIntervalMs = monitoringIntervalMs; }

        public boolean isEnableWorstCaseAnalysis() { return enableWorstCaseAnalysis; }
        public void setEnableWorstCaseAnalysis(boolean enableWorstCaseAnalysis) { this.enableWorstCaseAnalysis = enableWorstCaseAnalysis; }

        public String getLogLevel() { return logLevel; }
        public void setLogLevel(String logLevel) { this.logLevel = logLevel; }

        public boolean isEnableMetrics() { return enableMetrics; }
        public void setEnableMetrics(boolean enableMetrics) { this.enableMetrics = enableMetrics; }
    }

    public static class TopicConfig {
        @JsonProperty("name")
        private String name;

        @JsonProperty("description")
        private String description;

        @JsonProperty("qos")
        private QoSConfig qos;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public QoSConfig getQos() { return qos; }
        public void setQos(QoSConfig qos) { this.qos = qos; }
    }

    public static class QoSConfig {
        @JsonProperty("reliability")
        private String reliability;

        @JsonProperty("priority")
        private String priority;

        @JsonProperty("deadlineMs")
        private int deadlineMs;

        @JsonProperty("maxLatencyMs")
        private int maxLatencyMs;

        @JsonProperty("redundancyLevel")
        private int redundancyLevel;

        @JsonProperty("persistence")
        private boolean persistence;

        public String getReliability() { return reliability; }
        public void setReliability(String reliability) { this.reliability = reliability; }

        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }

        public int getDeadlineMs() { return deadlineMs; }
        public void setDeadlineMs(int deadlineMs) { this.deadlineMs = deadlineMs; }

        public int getMaxLatencyMs() { return maxLatencyMs; }
        public void setMaxLatencyMs(int maxLatencyMs) { this.maxLatencyMs = maxLatencyMs; }

        public int getRedundancyLevel() { return redundancyLevel; }
        public void setRedundancyLevel(int redundancyLevel) { this.redundancyLevel = redundancyLevel; }

        public boolean isPersistence() { return persistence; }
        public void setPersistence(boolean persistence) { this.persistence = persistence; }
    }

    public static class FaultToleranceConfig {
        @JsonProperty("enableFaultDetection")
        private boolean enableFaultDetection;

        @JsonProperty("subscriberTimeoutMs")
        private int subscriberTimeoutMs;

        @JsonProperty("maxFailureCount")
        private int maxFailureCount;

        @JsonProperty("enableRecovery")
        private boolean enableRecovery;

        @JsonProperty("recoveryBufferSize")
        private int recoveryBufferSize;

        public boolean isEnableFaultDetection() { return enableFaultDetection; }
        public void setEnableFaultDetection(boolean enableFaultDetection) { this.enableFaultDetection = enableFaultDetection; }

        public int getSubscriberTimeoutMs() { return subscriberTimeoutMs; }
        public void setSubscriberTimeoutMs(int subscriberTimeoutMs) { this.subscriberTimeoutMs = subscriberTimeoutMs; }

        public int getMaxFailureCount() { return maxFailureCount; }
        public void setMaxFailureCount(int maxFailureCount) { this.maxFailureCount = maxFailureCount; }

        public boolean isEnableRecovery() { return enableRecovery; }
        public void setEnableRecovery(boolean enableRecovery) { this.enableRecovery = enableRecovery; }

        public int getRecoveryBufferSize() { return recoveryBufferSize; }
        public void setRecoveryBufferSize(int recoveryBufferSize) { this.recoveryBufferSize = recoveryBufferSize; }
    }

    public static class RealtimeConfig {
        @JsonProperty("enableDeadlineMonitoring")
        private boolean enableDeadlineMonitoring;

        @JsonProperty("deadlineCheckIntervalMs")
        private int deadlineCheckIntervalMs;

        @JsonProperty("enableScheduler")
        private boolean enableScheduler;

        @JsonProperty("schedulerThreadPriority")
        private String schedulerThreadPriority;

        public boolean isEnableDeadlineMonitoring() { return enableDeadlineMonitoring; }
        public void setEnableDeadlineMonitoring(boolean enableDeadlineMonitoring) { this.enableDeadlineMonitoring = enableDeadlineMonitoring; }

        public int getDeadlineCheckIntervalMs() { return deadlineCheckIntervalMs; }
        public void setDeadlineCheckIntervalMs(int deadlineCheckIntervalMs) { this.deadlineCheckIntervalMs = deadlineCheckIntervalMs; }

        public boolean isEnableScheduler() { return enableScheduler; }
        public void setEnableScheduler(boolean enableScheduler) { this.enableScheduler = enableScheduler; }

        public String getSchedulerThreadPriority() { return schedulerThreadPriority; }
        public void setSchedulerThreadPriority(String schedulerThreadPriority) { this.schedulerThreadPriority = schedulerThreadPriority; }
    }
}