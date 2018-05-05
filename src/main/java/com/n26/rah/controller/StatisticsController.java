package com.n26.rah.controller;

import com.n26.rah.model.StatisticsRequest;
import com.n26.rah.model.StatisticsResponse;
import com.n26.rah.service.IStatisticsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.inject.Inject;
import java.time.Instant;

@Controller
public class StatisticsController {

    @Inject
    private IStatisticsService statisticsService;

    @RequestMapping(value = "/transactions", method = RequestMethod.GET)
    public ResponseEntity getStatistics(){
        long current = Instant.now().toEpochMilli();
        return new ResponseEntity<>(statisticsService.getStatistics(current), HttpStatus.OK);
    }

    @RequestMapping(value = "/transactions", method = RequestMethod.POST)
    public ResponseEntity addStatistics(@RequestBody StatisticsRequest request){
        long current = Instant.now().toEpochMilli();
        boolean added = statisticsService.addStatistics(request, current);
        if(added) {
            return new ResponseEntity(HttpStatus.CREATED);
        } else {
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        }
    }
}
