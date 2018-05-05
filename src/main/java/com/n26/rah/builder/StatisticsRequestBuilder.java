package com.n26.rah.builder;

import com.n26.rah.model.StatisticsRequest;

public class StatisticsRequestBuilder {

    private StatisticsRequest request;

    private StatisticsRequestBuilder() {
        request = new StatisticsRequest();
    }

    public static StatisticsRequestBuilder createStatisticsRequest(){
        return new StatisticsRequestBuilder();
    }

    public StatisticsRequestBuilder withAmount(double amount){
        request.setAmount(amount);
        return this;
    }

    public StatisticsRequestBuilder withTimestamp(long timestamp){
        request.setTimestamp(timestamp);
        return this;
    }

    public StatisticsRequest build(){
        return request;
    }
}
