package com.n26.rah.builder;

import com.n26.rah.model.StatisticsResponse;

public class StatisticsResponseBuilder {

    private StatisticsResponse response;

    private StatisticsResponseBuilder() {
        response = new StatisticsResponse();
    }

    public static StatisticsResponseBuilder createStatisticsResponse(){
        return new StatisticsResponseBuilder();
    }

    public StatisticsResponseBuilder withSum(double sum){
        response.setSum(sum);
        return this;
    }

    public StatisticsResponseBuilder withAvg(double avg){
        response.setAvg(avg);
        return this;
    }

    public StatisticsResponseBuilder withMax(double max){
        response.setMax(max);
        return this;
    }

    public StatisticsResponseBuilder withMin(double min){
        response.setMin(min);
        return this;
    }

    public StatisticsResponseBuilder withCount(long count){
        response.setCount(count);
        return this;
    }

    public StatisticsResponse build(){
        return response;
    }
}
