package com.logger.mikrotik;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.prometheus.client.Gauge;
import io.prometheus.client.CollectorRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CustomMetricsExporter {

    private static final CollectorRegistry collectorRegistry = new CollectorRegistry();
    private static final Map<String, Gauge> gauges = new ConcurrentHashMap<>();

    public static void exportMetrics(Map<String, String> data) {
        for (Map.Entry<String, String> entry : data.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            // Create a metric name and use the key as a label
            String metricName = "custom_metric_" + key.replaceAll("[^a-zA-Z0-9_]", "_");

            // Get or create the gauge
            Gauge gauge = gauges.computeIfAbsent(metricName, k -> Gauge.build()
                    .name(k)
                    .help("Custom metric for key: " + key)
                    .labelNames("value")
                    .register()); // Register with the CollectorRegistry

            // Set the gauge value with the string converted to double
            double numericValue = convertStringToDouble(value);
            gauge.labels(value).set(numericValue);
        }
    }

    private static double convertStringToDouble(String input) {
        return input.hashCode();
    }
}
