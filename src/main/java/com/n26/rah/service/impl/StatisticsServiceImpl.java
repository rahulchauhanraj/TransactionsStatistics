package com.n26.rah.service.impl;

import com.n26.rah.builder.StatisticsResponseBuilder;
import com.n26.rah.cache.StatisticsCache;
import com.n26.rah.exception.BadRequestException;
import com.n26.rah.model.Statistics;
import com.n26.rah.model.StatisticsRequest;
import com.n26.rah.model.StatisticsResponse;
import com.n26.rah.service.IStatisticsService;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

import static com.n26.rah.util.Constants.ONE_MINUTE_IN_MS;
import static com.n26.rah.util.Constants.STATISTICS_CACHE_SIZE;

@Service
public class StatisticsServiceImpl implements IStatisticsService{

    private StatisticsCache<Long, Statistics> cache;

    @PostConstruct
    public void init(){
        cache = new StatisticsCache<>();
    }

    public boolean addStatistics(StatisticsRequest request, long timestamp) {
        if (request.getAmount() <= 0){
            throw new BadRequestException("Invalid amount value : " + request.getAmount());
        }
        long requestTime = request.getTimestamp();
        long delay = timestamp - requestTime;
        if (delay >= ONE_MINUTE_IN_MS) {
            return false;
        } else {
            Long key = getKeyFromTimestamp(requestTime);
            Statistics s = cache.get(key);
            if(s == null) {
                synchronized (cache) {
                    s = cache.get(key);
                    if (s == null) {
                        s = new Statistics();
                        cache.put(key, s);
                    }
                }
            }
            s.updateStatistics(request.getAmount());
        }
        return true;
    }

    public StatisticsResponse getStatistics(long timestamp) {
        Map<Long, Statistics> copy = cache.entrySet().parallelStream().collect(Collectors.toMap(e -> e.getKey(), e -> new Statistics(e.getValue())));
        return getStatisticsFromCache(copy, timestamp);
    }

    private StatisticsResponse getStatisticsFromCache(Map<Long, Statistics> copy, long timestamp) {
        double sum = 0;
        double avg = 0;
        double max = 0;
        double min = Double.MAX_VALUE;
        long count = 0;
        Long key = getKeyFromTimestamp(timestamp);

        for (Map.Entry<Long, Statistics> e : copy.entrySet()) {
            Long eKey = e.getKey();
            Long timeFrame = key - eKey;
            if(timeFrame >= 0 && timeFrame < 60) {
                Statistics eValue = e.getValue();
                sum += eValue.getSum();
                min = min < eValue.getMin() ? min : eValue.getMin();
                max = max > eValue.getMax() ? max : eValue.getMax();
                count += eValue.getCount();
            }
        }
        if(count == 0) {
            min = 0;
            avg = 0;
        } else {
            avg = sum / count;
        }

        return StatisticsResponseBuilder.createStatisticsResponse().withSum(sum).withAvg(avg).withMax(max).withMin(min).withCount(count).build();
    }

    private Long getKeyFromTimestamp(Long timestamp) {
        return (timestamp * STATISTICS_CACHE_SIZE) / ONE_MINUTE_IN_MS;
    }

    public void clearCache() {
        cache.clear();
    }
}
