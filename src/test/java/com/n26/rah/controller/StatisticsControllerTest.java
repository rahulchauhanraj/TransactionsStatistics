package com.n26.rah.controller;

import com.n26.rah.Application;
import com.n26.rah.builder.StatisticsRequestBuilder;
import com.n26.rah.controller.StatisticsController;
import com.n26.rah.model.StatisticsRequest;
import com.n26.rah.model.StatisticsResponse;
import com.n26.rah.service.IStatisticsService;
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
public class StatisticsControllerTest {

    @Inject
    private StatisticsController controller;

    @Inject
    private IStatisticsService service;

    @Before
    public void init(){
        service.clearCache();
    }

    @Test
    public void testAddStatistics_withValidStats_created(){
        StatisticsRequest request = StatisticsRequestBuilder.createStatisticsRequest().withAmount(1.1).withTimestamp(Instant.now().toEpochMilli()).build();
        ResponseEntity responseEntity = controller.addStatistics(request);
        Assert.assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
    }

    @Test
    public void testAddStatistics_withNegativeAmount_created(){
        StatisticsRequest request = StatisticsRequestBuilder.createStatisticsRequest().withAmount(-1.1).withTimestamp(Instant.now().toEpochMilli()).build();
        ResponseEntity responseEntity = controller.addStatistics(request);
        Assert.assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
    }

    @Test
    public void testAddStatistics_withInPastTimestampMoreThanAMinute_noContent(){
        StatisticsRequest request = StatisticsRequestBuilder.createStatisticsRequest().withAmount(2.1).withTimestamp(Instant.now().toEpochMilli()-60000).build();
        ResponseEntity responseEntity = controller.addStatistics(request);
        Assert.assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
    }

    @Test
    public void testAddStatistics_withInPastTimestampWithinAMinute_created(){
        StatisticsRequest request = StatisticsRequestBuilder.createStatisticsRequest().withAmount(2.1).withTimestamp(Instant.now().toEpochMilli()-50000).build();
        ResponseEntity responseEntity = controller.addStatistics(request);
        Assert.assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
    }

    @Test
    public void testAddAndGetStatistics_withInValidTimestampWithinAMinuteWithSameTime_success() throws Exception{
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        int n = 0;
        double amount = 1.0;
        int count = 50000;
        while(n<count) {
            StatisticsRequest request = StatisticsRequestBuilder.createStatisticsRequest().withAmount(amount).withTimestamp(Instant.now().toEpochMilli()).build();
            executorService.submit(() -> controller.addStatistics(request));
            n++;
            amount += 1;
        }

        executorService.shutdown();
        Thread.sleep(1000);
        ResponseEntity response = controller.getStatistics();
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals(count, ((StatisticsResponse)response.getBody()).getCount());
    }

    @Test
    public void testAddAndGetStatistics_withInValidTimestampWithinAMinuteWithDifferentTime_success() throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        int n = 0;
        double amount = 1.0;
        int count = 50000;
        long timestamp = Instant.now().toEpochMilli();
        while(n<count) {
            StatisticsRequest request = StatisticsRequestBuilder.createStatisticsRequest().withAmount(amount).withTimestamp(timestamp).build();
            executorService.submit(() -> controller.addStatistics(request));
            n++;
            amount += 1;
            timestamp -= 1;
        }

        executorService.shutdown();
        Thread.sleep(1000);
        ResponseEntity response = controller.getStatistics();
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals(count, ((StatisticsResponse)response.getBody()).getCount());
    }

    @Test
    public void testAddAndGetStatistics_withInValidAndOutdatedTimestamp_success() throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        int n = 0;
        double amount = 1.0;
        int count = 500;
        long timestamp = Instant.now().toEpochMilli();
        while(n<count) {
            StatisticsRequest request = StatisticsRequestBuilder.createStatisticsRequest().withAmount(amount).withTimestamp(timestamp).build();
            executorService.submit(() -> controller.addStatistics(request));
            n++;
            amount += 1;
            timestamp -= 1;
        }

        Thread.sleep(1000);
        timestamp -= 60000;
        n = 0;
        while(n<count) {
            StatisticsRequest request = StatisticsRequestBuilder.createStatisticsRequest().withAmount(amount).withTimestamp(timestamp).build();
            executorService.submit(() -> controller.addStatistics(request));
            n++;
            amount += 1;
            timestamp -= 60000;
        }

        executorService.shutdown();
        Thread.sleep(1000);
        ResponseEntity response = controller.getStatistics();
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals(count, ((StatisticsResponse)response.getBody()).getCount());
    }

}
