package com.n26.rah.service;

import com.n26.rah.Application;
import com.n26.rah.builder.StatisticsRequestBuilder;
import com.n26.rah.controller.StatisticsController;
import com.n26.rah.model.StatisticsRequest;
import com.n26.rah.model.StatisticsResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {Application.class})
public class StatisticsServiceTest {

    @Inject
    private IStatisticsService service;

    @Before
    public void init(){
        service.clearCache();
    }

    @Test
    public void testAddStatistics_withValidStats_added(){
        long current = Instant.now().toEpochMilli();
        StatisticsRequest request = StatisticsRequestBuilder.createStatisticsRequest().withAmount(1.1).withTimestamp(current).build();
        boolean added = service.addStatistics(request, current);
        Assert.assertEquals(true, added);
    }

    @Test
    public void testAddStatistics_withNegativeAmount_added(){
        long current = Instant.now().toEpochMilli();
        StatisticsRequest request = StatisticsRequestBuilder.createStatisticsRequest().withAmount(-1.1).withTimestamp(current).build();
        boolean added = service.addStatistics(request, current);
        Assert.assertEquals(true, added);
    }

    @Test
    public void testAddStatistics_withInPastTimestampMoreThanAMinute_notAdded(){
        long current = Instant.now().toEpochMilli();
        StatisticsRequest request = StatisticsRequestBuilder.createStatisticsRequest().withAmount(2.1).withTimestamp(current-60000).build();
        boolean added = service.addStatistics(request, current);
        Assert.assertEquals(false, added);
    }

    @Test
    public void testAddStatistics_withInPastTimestampWithinAMinute_created(){
        long current = Instant.now().toEpochMilli();
        StatisticsRequest request = StatisticsRequestBuilder.createStatisticsRequest().withAmount(2.1).withTimestamp(current-50000).build();
        boolean added = service.addStatistics(request, current);
        Assert.assertEquals(true, added);
    }

    @Test
    public void testGetStatistics_withAnyData_success() throws Exception{
        long timestamp = Instant.now().toEpochMilli();
        StatisticsResponse response = service.getStatistics(timestamp);
        Assert.assertEquals(0, response.getCount());
        Assert.assertEquals(0, response.getMax(), 0);
        Assert.assertEquals(0, response.getMin(), 0);
        Assert.assertEquals(0, response.getAvg(), 0);
    }

    @Test
    public void testAddAndGetStatistics_withValidTimestampMultipleThread_success() throws Exception{
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        int n = 0;
        double amount = 1.0;
        int count = 1000;
        long timestamp = Instant.now().toEpochMilli();
        long requestTime = timestamp;
        while(n<count) {
            StatisticsRequest request = StatisticsRequestBuilder.createStatisticsRequest().withAmount(amount).withTimestamp(requestTime).build();
            executorService.submit(() -> service.addStatistics(request, timestamp));
            n++;
            amount += 1;
            requestTime -= 10;
        }

        executorService.shutdown();
        Thread.sleep(1000);
        StatisticsResponse response = service.getStatistics(timestamp);
        Assert.assertEquals(count, response.getCount());
        Assert.assertEquals(1000, response.getMax(), 0);
        Assert.assertEquals(1, response.getMin(), 0);
        Assert.assertEquals(500.5, response.getAvg(), 0);
    }
}
