package com.example.BigQuery.service.bigquery.running.queries.controller;

import com.example.BigQuery.service.bigquery.running.queries.model.QueryRunner;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@RestController
public class RunningQueriesController {



    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ModelAndView getHomePage(){
        return new ModelAndView("home");
    }

    @RequestMapping(value = "/query", method = RequestMethod.POST)
    @ResponseBody
    public String runQuery(@RequestBody String query){
        System.out.println(query);

        QueryRunner queryRunner = new QueryRunner();

        return queryRunner.executeQuery(query);
    }
}
