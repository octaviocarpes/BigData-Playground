package com.example.BigQuery.service.bigquery.running.queries.model;

import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.bigquery.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class QueryRunner {


    public QueryRunner() {
    }

    public String executeQuery(String query){
        BigQuery bigQuery = null;

        try{
            bigQuery = BigQueryOptions.newBuilder().setProjectId("Porject Id")
                    .setCredentials(ServiceAccountCredentials.fromStream(new FileInputStream("key.json")))
                    .build()
                    .getService();
        }catch (IOException e){
            e.printStackTrace();
        }

        QueryJobConfiguration queryConfig =
                QueryJobConfiguration.newBuilder(query).setUseLegacySql(false).build();

        JobId jobId = JobId.of(UUID.randomUUID().toString());
        Job queryJob = bigQuery.create(JobInfo.newBuilder(queryConfig).setJobId(jobId).build());

        try{
            queryJob.waitFor();
        }catch (InterruptedException e){
            e.printStackTrace();
        }

        if(queryJob == null){
            throw new RuntimeException("Job no longer Exists");
        }else if(queryJob.getStatus().getError() != null){
            throw new RuntimeException(queryJob.getStatus().getError().toString());
        }

        QueryResponse response = bigQuery.getQueryResults(jobId);

        QueryResult result = response.getResult();

        StringBuilder stringBuilder = new StringBuilder();

        while(result != null){
            for (List<FieldValue> row : result.iterateAll()) {
                List<FieldValue> titles = row.get(0).getRepeatedValue();
                stringBuilder.append("Titles: \n");

                for (FieldValue titleValue : titles) {
                    List<FieldValue> titleRecord = titleValue.getRecordValue();
                    String title = titleRecord.get(0).getStringValue();
                    long uniqueWords = titleRecord.get(1).getLongValue();
                    stringBuilder.append(title + ": " + uniqueWords + "\n");
                }

                long uniqueWords = row.get(1).getLongValue();
                stringBuilder.append("\nTotal Unique Words: "+ uniqueWords);
            }

            result = result.getNextPage();
        }

        System.out.println(stringBuilder.toString());

       return stringBuilder.toString();
    }
}
