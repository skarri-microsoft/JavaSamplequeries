package com.microsoft.azure.cosmosdb.sql;

import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

public class SampleEvent5MinAgg {
    @com.google.gson.annotations.SerializedName("id")
    public String Id;

    @com.google.gson.annotations.SerializedName("client_id")
    public String ClientId;

    @com.google.gson.annotations.SerializedName("source")
    public String Source;

    @com.google.gson.annotations.SerializedName("schema_type")
    public String SchemaType;

    @com.google.gson.annotations.SerializedName("datastream_id")
    public String DataStreamId;

    @com.google.gson.annotations.SerializedName("timpstamp")
    public String TimeStamp;

    @com.google.gson.annotations.SerializedName("count")
    public int Count;

    @com.google.gson.annotations.SerializedName("is_valid")
    public boolean IsValid;

    @com.google.gson.annotations.SerializedName("pk")
    public String Pk;

    public static ArrayList<SampleEvent5MinAggCollection> GetSampleValidDocs(
            boolean isValid,
            int numberOfClients,
            int clientBatchSize,
            int aggregationInterval) throws ParseException {
        ArrayList<SampleEvent5MinAggCollection> clients = new ArrayList<>();
        for (int i = 0; i < numberOfClients; i++) {
            String sourceId = "Source_" + i;
            String clientId = "Client_" + i;
            String schemaTypeId = "SchemaTypeId_" + i;
            String dataSteamId = "DataStreamId_" + i;

            // Set time to number of documents per client * increasing interval
            DateTime startDateTime =
                    DateTimeUtil.GetCurrentUtcDateTime().minusMinutes(clientBatchSize*aggregationInterval);

            SampleEvent5MinAggCollection sampleEvent5MinAggCollection=new SampleEvent5MinAggCollection();
            sampleEvent5MinAggCollection.SetClientId(clientId);
            sampleEvent5MinAggCollection.SetStartDateTime(startDateTime);

            for (int j = 0; j < clientBatchSize; j++) {
               startDateTime= startDateTime.plusMinutes(aggregationInterval);
                String id = UUID.randomUUID().toString();

                SampleEvent5MinAgg sampleEvent5MinAgg = new SampleEvent5MinAgg();
                sampleEvent5MinAgg.Id = id;
                sampleEvent5MinAgg.Source = sourceId;
                sampleEvent5MinAgg.ClientId = clientId;
                sampleEvent5MinAgg.SchemaType = schemaTypeId;
                sampleEvent5MinAgg.DataStreamId = dataSteamId;

                sampleEvent5MinAgg.TimeStamp =
                        DateTimeUtil.GetCosmosDBIso8601Format(
                        DateTimeUtil.GetDateTimeWithMinsIntervalStamp(
                                startDateTime,
                                aggregationInterval));

                sampleEvent5MinAgg.Count = 1;
                sampleEvent5MinAgg.IsValid = isValid;

                sampleEvent5MinAgg.Pk=clientId+"_"+String.format(
                        "%02d_%04d",
                        startDateTime.getMonthOfYear(),
                        startDateTime.getYear());

                sampleEvent5MinAggCollection.AddSampleEventDoc(sampleEvent5MinAgg);
                sampleEvent5MinAggCollection.AddPartitionKey(sampleEvent5MinAgg.Pk);
            }
            sampleEvent5MinAggCollection.SetEndDateTime(startDateTime);
            clients.add(sampleEvent5MinAggCollection);
        }
        return clients;
    }
}
