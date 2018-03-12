import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.bigquery.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

public class Main {
    public static void main(String[] args) {

        BigQuery bigquery = null;
        try {
            bigquery = BigQueryOptions.newBuilder().setProjectId("sunny-atrium-197413")
                    .setCredentials(
                            ServiceAccountCredentials.fromStream(new FileInputStream("/Users/ilegra0365/Downloads/My First Project-3fe600b99ea3.json"))
                    ).build().getService();
        } catch (IOException e) {
            e.printStackTrace();
        }


        QueryJobConfiguration queryConfig =
                QueryJobConfiguration.newBuilder(
                        "SELECT "
                                + "CONCAT('https://stackoverflow.com/questions/', CAST(id as STRING)) as url, "
                                + "view_count "
                                + "FROM `bigquery-public-data.stackoverflow.posts_questions` "
                                + "WHERE tags like '%google-bigquery%' "
                                + "ORDER BY favorite_count DESC LIMIT 10")
                        // Use standard SQL syntax for queries.
                        // See: https://cloud.google.com/bigquery/sql-reference/
                        .setUseLegacySql(false)
                        .build();

// Create a job ID so that we can safely retry.
        JobId jobId = JobId.of(UUID.randomUUID().toString());
        System.out.println(jobId.toString());
        Job queryJob = bigquery.create(JobInfo.newBuilder(queryConfig).setJobId(jobId).build());

// Wait for the query to complete.
        try {
            queryJob = queryJob.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

// Check for errors
        if (queryJob == null) {
            throw new RuntimeException("Job no longer exists");
        } else if (queryJob.getStatus().getError() != null) {
            // You can also look at queryJob.getStatus().getExecutionErrors() for all
            // errors, not just the latest one.
            throw new RuntimeException(queryJob.getStatus().getError().toString());
        }

        // Get the results
        QueryResponse response = bigquery.getQueryResults(jobId);

        QueryResult result = response.getResult();

        for (FieldValueList row:result.iterateAll()) {
            String url = row.get("url").getStringValue();
            long viewCount = row.get("view_count").getLongValue();
            System.out.println("url: " + url + "\nviews: "+ viewCount);
        }


    }
}
