package com.example.databasecustomfieldsencrypter.config;

import com.mongodb.ClientEncryptionSettings;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoNamespace;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.vault.ClientEncryption;
import com.mongodb.client.vault.ClientEncryptions;

import org.bson.BsonBinary;
import org.bson.Document;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class MongoCryptConfig {

  @Bean
  public ClientEncryption clientEncryption() throws IOException {
    //    TODO READ FROM VAULT LOCAL CMS 
//    Read local CMS 
    FileInputStream fileInputStream = new FileInputStream("master-key.txt");
    byte[] localMasterKey = fileInputStream.readAllBytes();

    Map<String, Map<String, Object>> kmsProviders = new HashMap<>() {{
      put("local", new HashMap<>() {{
        put("key", localMasterKey);
      }});
    }};

    MongoClientSettings clientSettings = MongoClientSettings.builder().build();
    MongoClient mongoClient = MongoClients.create(clientSettings);

    // Set up the key vault for this example
    MongoNamespace keyVaultNamespace = new MongoNamespace("encryption.keyVault");

    MongoCollection<Document> keyVaultCollection = mongoClient.getDatabase(
            keyVaultNamespace.getDatabaseName())
        .getCollection(keyVaultNamespace.getCollectionName());
//    keyVaultCollection.drop();

    // Ensure that two data keys cannot share the same keyAltName.
    keyVaultCollection.createIndex(Indexes.ascending("keyAltNames"),
        new IndexOptions().unique(true)
            .partialFilterExpression(Filters.exists("keyAltNames")));

    // Create the ClientEncryption instance
    ClientEncryptionSettings clientEncryptionSettings = ClientEncryptionSettings.builder()
        .keyVaultMongoClientSettings(MongoClientSettings.builder()
            .applyConnectionString(new ConnectionString("mongodb://localhost"))
            .build())
        .keyVaultNamespace(keyVaultNamespace.getFullName())
        .kmsProviders(kmsProviders)
        .build();

    return ClientEncryptions.create(clientEncryptionSettings);
  }

  @Bean
  public BsonBinary dataEncryptionKey(ClientEncryption clientEncryption) {
    return clientEncryption.createDataKey("local");
  }
}
