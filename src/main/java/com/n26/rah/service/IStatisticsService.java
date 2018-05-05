package com.n26.rah.service;

import com.n26.rah.model.StatisticsRequest;
import com.n26.rah.model.StatisticsResponse;

public interface IStatisticsService {
    boolean addStatistics(StatisticsRequest request, long timestamp);
    StatisticsResponse getStatistics(long timestamp);
    void clearCache();
}
