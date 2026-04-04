/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.learning.metric.annotation;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.learning.metric.Metric;
import org.openmarkov.plugin.PluginSearch;

import java.util.HashMap;
import java.util.Set;
import java.util.stream.Stream;

public class MetricManager {
    private HashMap<String, Class<? extends Metric>> metrics;
    private HashMap<String, Class<? extends Metric>> classConditionedMetrics;
    
    /**
     * Constructor for MetricManager.
     */
    @SuppressWarnings("unchecked") public MetricManager() {
        metrics = new HashMap<>();
        classConditionedMetrics = new HashMap<>();
        findAllMetrics().forEach(plugin->{
            MetricType lAnnotation = plugin.getAnnotation(MetricType.class);
            if (!plugin.getAnnotation(MetricType.class).classConditionedMetric()) {
                metrics.put(lAnnotation.name(), plugin);
            } else {
                classConditionedMetrics.put(lAnnotation.name(), plugin);
            }
        });
    }
    
    /**
     * Finds a learning algorithm by name.
     *
     * @param name the algorithm name.
     * @return a learning algorithm.
     */
    public final Class<? extends Metric> getMetricByName(String name) {
        return (metrics.get(name) != null ? metrics : classConditionedMetrics).get(name);
    }
    
    /**
     * Returns the names of all metrics.
     *
     * @return the names of all metrics.
     */
    public final Set<String> getAllMetricNames() {
        return metrics.keySet();
    }
    
    /**
     * Finds all metrics.
     *
     * @return a list of metrics.
     */
    private static @NotNull Stream<Class<? extends Metric>> findAllMetrics() {
        return PluginSearch.init()
                           .annotatedWith(MetricType.class)
                           .childrenOf(Metric.class)
                           .stream();
    }
    
    /**
     * Returns the names of those metrics whose score is conditioned to a class variable
     *
     * @return A set of metric names
     */
    public final Set<String> getClassConditionedMetrics() {
        return classConditionedMetrics.keySet();
    }
    
}

