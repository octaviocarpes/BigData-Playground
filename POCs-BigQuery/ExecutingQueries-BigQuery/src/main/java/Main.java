import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.bigquery.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Created by octaviocarpes on 3/11/18.
 */
public class Main {
    public static void main(String[] args) {

        BigQuery bigquery = null;
        try {
            bigquery = BigQueryOptions.newBuilder().setProjectId("Insert project id here")
                    .setCredentials(
                            ServiceAccountCredentials.fromStream(new FileInputStream("insert key path  here"))
                    ).build().getService();
        } catch (IOException e) {
            e.printStackTrace();
        }

        QueryJobConfiguration queryConfig =
                QueryJobConfiguration.newBuilder(
                        "SELECT "
                                + "APPROX_TOP_COUNT(corpus, 10) as title, "
                                + "COUNT(*) as unique_words "
                                + "FROM `publicdata.samples.shakespeare`;")
                        // Use standard SQL syntax for queries.
                        // See: https://cloud.google.com/bigquery/sql-reference/
                        .setUseLegacySql(false)
                        .build();

// Create a job ID so that we can safely retry.
        JobId jobId = JobId.of(UUID.randomUUID().toString());
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

// Get the results.
        QueryResponse response = bigquery.getQueryResults(jobId);

        QueryResult result = response.getResult();

// Print all pages of the results.
        while (result != null) {
            for (List<FieldValue> row : result.iterateAll()) {
                List<FieldValue> titles = row.get(0).getRepeatedValue();
                System.out.println("titles:");

                for (FieldValue titleValue : titles) {
                    List<FieldValue> titleRecord = titleValue.getRecordValue();
                    String title = titleRecord.get(0).getStringValue();
                    long uniqueWords = titleRecord.get(1).getLongValue();
                    System.out.printf("\t%s: %d\n", title, uniqueWords);
                }

                long uniqueWords = row.get(1).getLongValue();
                System.out.printf("total unique words: %d\n", uniqueWords);
            }

            result = result.getNextPage();
        }
    }
}
