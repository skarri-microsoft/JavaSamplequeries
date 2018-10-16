package com.microsoft.azure.cosmosdb.sql;

import com.google.gson.Gson;
import com.microsoft.azure.cosmosdb.sql.sdkextensions.CosmosDbSqlClientExtension;
import com.microsoft.azure.documentdb.*;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class App 
{
    private static Gson gson = new Gson();
    private static CosmosDbSqlClientExtension ingestionClient;
    private static ConnectionPolicy connectionPolicy;

    // https://docs.microsoft.com/en-us/azure/cosmos-db/working-with-dates
    public static void main( String[] args ) throws DocumentClientException, IOException, ParseException {

        ArrayList<SampleEvent5MinAggCollection> clients=
                GenerateSampleDocs();

        Settings.initSettings();

        CreateCosmosDBClient();

        CreateDocuments(clients);

        QueryDocs(clients);

        // Clean up, demo purpose should not be used in production
        ingestionClient.DeleteAllDocs("pk");

    }

    private static ArrayList<SampleEvent5MinAggCollection> GenerateSampleDocs() throws ParseException {
        return SampleEvent5MinAgg.GetSampleValidDocs(true,3,15,5);
    }

    private static void CreateCosmosDBClient()
    {
        connectionPolicy = new ConnectionPolicy();
        connectionPolicy.setConnectionMode(ConnectionMode.DirectHttps);
        connectionPolicy.setMaxPoolSize(100);

        ingestionClient=new CosmosDbSqlClientExtension(
                Settings.getCosmosDbEndPoint().trim(),
                Settings.getCosmosDbMasterKey().trim(),
                Settings.getCosmosDbDatabase().trim(),
                Settings.getCosmosDbDataCollection().trim(),
                Settings.getCosmosDbDataCollectionThroughput(),
                ConsistencyLevel.Session,
                connectionPolicy);
    }

    private static void CreateDocuments(ArrayList<SampleEvent5MinAggCollection> clients) throws DocumentClientException {
        for(int i=0;i<clients.size();i++)
        {
            for(int j=0;j<clients.get(i).ClientDocs.size();j++) {
                String jsonString = gson.toJson(clients.get(i).ClientDocs.get(j));
                ingestionClient.createDocument(new Document(jsonString));
            }
        }
    }

    private static void QueryDocs(ArrayList<SampleEvent5MinAggCollection> clients) throws DocumentClientException {

        for(int i=0;i<clients.size();i++) {

            System.out.println("Total inserted documents: "+clients.get(i).ClientDocs.size());

            int receivedCount=0;
            for(int j=0;j<clients.get(i).PartitionKeys.size();j++) {

                System.out.println(
                        "Querying documents for Client Id: "
                                +clients.get(i).ClientId
                                + "Partition key: "
                                +clients.get(i).PartitionKeys.get(j));

                String query="SELECT * FROM root WHERE (root['timpstamp']) >= '"
                        +DateTimeUtil.GetCosmosDBIso8601Format(clients.get(i).StartDateTime)+
                        "'";
               List<Document> docs= ingestionClient.queryDocs(query,clients.get(i).PartitionKeys.get(j));

               receivedCount=receivedCount+ docs.size();
            }

            System.out.println("Total documents queried: "+receivedCount);
        }
    }
}
