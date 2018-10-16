package com.microsoft.azure.cosmosdb.sql;

import org.joda.time.DateTime;

import java.util.ArrayList;

public class SampleEvent5MinAggCollection {

    public String ClientId;
    public ArrayList<String> PartitionKeys;
    public DateTime StartDateTime;
    public DateTime EndDateTime;
    public ArrayList<SampleEvent5MinAgg> ClientDocs;


    public SampleEvent5MinAggCollection() {
        this.PartitionKeys = new ArrayList<>();
        this.ClientDocs= new ArrayList<>();
    }

    public void SetClientId(String clientId) {
        this.ClientId = clientId;
    }

    public void AddPartitionKey(String key) {
        if (!this.PartitionKeys.contains(key)) {
            this.PartitionKeys.add(key);
        }
    }

    public void SetStartDateTime(DateTime dateTime) {
        this.StartDateTime = dateTime;
    }

    public void SetEndDateTime(DateTime dateTime) {
        this.EndDateTime = dateTime;
    }

    public void AddSampleEventDoc(SampleEvent5MinAgg sampleEvent5MinAgg) {
        this.ClientDocs.add(sampleEvent5MinAgg);
    }
}
