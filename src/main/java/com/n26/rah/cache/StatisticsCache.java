package com.n26.rah.cache;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.n26.rah.util.Constants.STATISTICS_CACHE_SIZE;

public class StatisticsCache<Long, Statistics> extends LinkedHashMap<Long, Statistics> {

    public StatisticsCache(){
        super(STATISTICS_CACHE_SIZE);
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<Long, Statistics> eldest) {
        return this.size() > STATISTICS_CACHE_SIZE;
    }

}
