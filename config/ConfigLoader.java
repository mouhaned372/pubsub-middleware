package fr.telecom.middleware.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.InputStream;

/**
 * Chargeur de configuration pour le middleware.
 */
public class ConfigLoader {
    private static final Logger logger = LoggerFactory.getLogger(ConfigLoader.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Charge la configuration depuis un fichier JSON.
     */
    public static MiddlewareConfig loadConfig(String configFile) {
        try {
            InputStream is = ConfigLoader.class.getClassLoader().getResourceAsStream(configFile);
            if (is == null) {
                logger.warn("Fichier de configuration {} non trouvé, utilisation des valeurs par défaut", configFile);
                return createDefaultConfig();
            }

            MiddlewareConfig config = objectMapper.readValue(is, MiddlewareConfig.class);
            logger.info("Configuration chargée depuis {}", configFile);
            return config;

        } catch (Exception e) {
            logger.error("Erreur lors du chargement de la configuration, utilisation des valeurs par défaut", e);
            return createDefaultConfig();
        }
    }

    /**
     * Crée une configuration par défaut.
     */
    private static MiddlewareConfig createDefaultConfig() {
        MiddlewareConfig config = new MiddlewareConfig();
        config.setName("PubSub-Realtime-Middleware");
        config.setVersion("1.0.0");
        config.setMaxMemoryKB(512);
        config.setRealtimeEnabled(true);
        config.setDefaultThreadPoolSize(4);

        return config;
    }

    /**
     * Convertit une configuration JSON en objet QoS.
     */
    public static fr.telecom.middleware.qos.QoS convertToQoS(MiddlewareConfig.QoSConfig qosConfig) {
        fr.telecom.middleware.qos.QoS.Reliability reliability =
                fr.telecom.middleware.qos.QoS.Reliability.valueOf(qosConfig.getReliability());

        fr.telecom.middleware.qos.QoS.Priority priority =
                fr.telecom.middleware.qos.QoS.Priority.valueOf(qosConfig.getPriority());

        return new fr.telecom.middleware.qos.QoS.Builder()
                .reliability(reliability)
                .priority(priority)
                .deadlineMs(qosConfig.getDeadlineMs())
                .maxLatencyMs(qosConfig.getMaxLatencyMs())
                .redundancyLevel(qosConfig.getRedundancyLevel())
                .persistence(qosConfig.isPersistence())
                .build();
    }
}