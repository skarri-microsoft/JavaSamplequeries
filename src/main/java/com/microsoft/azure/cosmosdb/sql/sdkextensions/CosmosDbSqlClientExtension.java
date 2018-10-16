package com.microsoft.azure.cosmosdb.sql.sdkextensions;

import com.microsoft.azure.documentdb.*;
import org.apache.http.HttpStatus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;

import static com.microsoft.azure.documentdb.ConnectionPolicy.GetDefault;

public class CosmosDbSqlClientExtension {
    private final transient Logger logger = Logger.getLogger(CosmosDbSqlClientExtension.class);
    private final String endPoint;
    private final String masterKey;
    private final String databaseName;
    private final String collectionName;
    private final Integer throughput;
    private DocumentClient documentClient;

    public CosmosDbSqlClientExtension(
            String endPoint,
            String masterKey,
            String databaseName,
            String collectionName,
            Integer throughput,
            ConsistencyLevel consistencyLevel,
            ConnectionPolicy connectionPolicy) {
        this.endPoint = endPoint;
        this.masterKey = masterKey;
        this.databaseName = databaseName;
        this.collectionName = collectionName;
        this.throughput = throughput;
        this.initClient(consistencyLevel, connectionPolicy);
    }

    private void initClient(ConsistencyLevel consistencyLevel, ConnectionPolicy connectionPolicy) {
        if (connectionPolicy == null) {
            connectionPolicy = GetDefault();
        }

        if (consistencyLevel == null) {
            consistencyLevel = ConsistencyLevel.Session;
        }

        this.documentClient =
                new DocumentClient(endPoint, masterKey, connectionPolicy, consistencyLevel);
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public DocumentClient getDocumentClient() {
        return documentClient;
    }

    public int getOfferThroughput() throws Exception {
        FeedResponse<Offer> offers =
                this.documentClient.queryOffers(
                        String.format(
                                "SELECT * FROM c where c.offerResourceId = '%s'", getCollection().getResourceId()),
                        null);

        List<Offer> offerAsList = offers.getQueryIterable().toList();
        if (offerAsList.isEmpty()) {
            throw new IllegalStateException("Cannot find Collection's corresponding offer");
        }

        Offer offer = offerAsList.get(0);
        return offer.getContent().getInt("offerThroughput");
    }

    public DocumentCollection getCollection() throws DocumentClientException {
        String collectionLink =
                String.format("/dbs/%s/colls/%s", this.databaseName, this.collectionName);
        ResourceResponse<DocumentCollection> resourceResponse;
        resourceResponse = documentClient.readCollection(collectionLink, null);
        return resourceResponse.getResource();
    }

    public void createDatabaseIfNotExists() throws DocumentClientException {
        try {
            String dbLink = String.format("/dbs/%s", this.databaseName);
            ResourceResponse<Database> resourceResponse;
            resourceResponse = documentClient.readDatabase(dbLink, null);

            if (resourceResponse.getResource().getId().equals(this.databaseName)) {
                logger.info("Found database " + this.databaseName);
            }
        } catch (DocumentClientException de) {
            // If the database does not exist, create a new database
            if (de.getStatusCode() == 404) {
                logger.info("Creating database " + this.databaseName);
                Database database = new Database();
                database.setId(this.databaseName);
                this.documentClient.createDatabase(database, null);
            } else {
                throw de;
            }
        }
    }

    public DocumentCollection createCollectionIfNotExists(String partitionKey)
            throws DocumentClientException {
        for (DocumentCollection coll :
                this.documentClient
                        .readCollections("/dbs/" + this.databaseName, null)
                        .getQueryIterable()
                        .toList()) {
            if (coll.getId().equals(this.collectionName)) {
                logger.info("Found collection " + this.collectionName);
                return coll;
            }
        }

        logger.info("Creating collection " + this.collectionName);

        DocumentCollection collection = new DocumentCollection();
        collection.setId(this.collectionName);

        if (!partitionKey.equals("")) {
            PartitionKeyDefinition partitionKeyDefinition = new PartitionKeyDefinition();
            Collection<String> paths = new ArrayList<>();
            paths.add(partitionKey);
            partitionKeyDefinition.setPaths(paths);
            collection.setPartitionKey(partitionKeyDefinition);
        }

        RequestOptions options = new RequestOptions();
        options.setOfferThroughput(this.throughput);

        return this.documentClient
                .createCollection("/dbs/" + this.databaseName, collection, options)
                .getResource();
    }

    public Document createDocument(Document doc) throws DocumentClientException {

        try {
            return this.documentClient
                    .createDocument(
                            "dbs/" + this.databaseName + "/colls/" + this.collectionName, doc, null, false)
                    .getResource();
        } catch (DocumentClientException e) {
            if (e.getStatusCode() == HttpStatus.SC_CONFLICT) {
                return null;
            }

            throw e;
        }
    }

    public List<Document> queryDocs(String query) throws DocumentClientException {

        List<Document> docs = new ArrayList<Document>();
        for (Document document :
                this.documentClient
                        .queryDocuments(
                                "dbs/" + this.databaseName + "/colls/" + this.collectionName, query, null)
                        .getQueryIterable()
                        .toList()) {
            docs.add(document);
        }

        return docs;
    }

    public List<Document> queryDocs(String query, String partitionKey) throws DocumentClientException {

        List<Document> docs = new ArrayList<Document>();
        FeedOptions options = new FeedOptions();
        options.setPartitionKey(new PartitionKey(partitionKey));

        Iterator<Document> it =  this.documentClient.queryDocuments(
                "dbs/" + this.databaseName + "/colls/" + this.collectionName,
                query,
                options).getQueryIterator();

        int i = 0;
        while(it.hasNext()) {
            Document d = it.next();
            docs.add(d);
            i++;
        }
        return docs;
    }

    // Note this is demo purpose, should not be used in production
    // there are efficient ways to clean up the entire collection
    public void DeleteAllDocs(String partitionKeyProperity) throws DocumentClientException {
        FeedOptions options = new FeedOptions();
        options.setEnableCrossPartitionQuery(true);

        Iterator<Document> it =  this.documentClient.queryDocuments(
                "dbs/" + this.databaseName + "/colls/" + this.collectionName,
                "select * from c",
                options).getQueryIterator();

        int i = 0;
        while(it.hasNext()) {
            Document d = it.next();
            RequestOptions  docOptions = new RequestOptions();
            docOptions.setPartitionKey(new PartitionKey(d.get(partitionKeyProperity)));
            this.documentClient.deleteDocument(d.getSelfLink(),docOptions);
            i++;
        }
        System.out.println("Deletion Completed. Total documents deleted: "+i);
    }
    public Document updateItem(Document doc, String jsonDoc) throws DocumentClientException {
        AccessCondition condition = new AccessCondition();
        condition.setType(AccessConditionType.IfMatch);
        condition.setCondition(doc.getETag());

        RequestOptions options = new RequestOptions();
        options.setAccessCondition(condition);
        ResourceResponse<Document> response =
                this.documentClient.replaceDocument(doc.getSelfLink(), new Document(jsonDoc), options);
        return response.getResource();
    }

    public void safeClose() {
        if (documentClient != null) {
            try {
                documentClient.close();
            } catch (Exception e) {
                logger.info(
                        String.format(
                                "closing client failed with %s. Will re-initialize later" + e.getMessage()));
            }
        }
        documentClient = null;
    }
}
