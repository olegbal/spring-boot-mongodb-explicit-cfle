package com.example.databasecustomfieldsencrypter.config;

import com.mongodb.ClientEncryptionSettings;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoNamespace;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.vault.ClientEncryption;
import com.mongodb.client.vault.ClientEncryptions;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;

@Configuration
@ConditionalOnProperty(prefix = "spring.data.mongodb.encryption", name = "enabled", havingValue = "true")
public class MongoCryptConfig extends AbstractMongoClientConfiguration {

  @Value("${spring.data.mongodb.encryption.keyvault.database}")
  public String keyVaultDatabaseName;

  @Value("${spring.data.mongodb.encryption.keyvault.collection}")
  public String keyVaultCollectionName;

  @Value("${spring.data.mongodb.uri}")
  public String mongoConnectionString;

  private final MongoKmsProvider mongoKmsProvider;

  public MongoCryptConfig(
      MongoKmsProvider mongoKmsProvider) {
    this.mongoKmsProvider = mongoKmsProvider;
  }

  @Bean
  public MongoNamespace keyVaultMongoNameSpace() {
    return new MongoNamespace(keyVaultDatabaseName, keyVaultCollectionName);
  }

  @Bean
  public ClientEncryption clientEncryption(MongoNamespace keyVaultMongoNameSpace) {
    ClientEncryptionSettings clientEncryptionSettings = ClientEncryptionSettings.builder()
        .keyVaultMongoClientSettings(MongoClientSettings.builder()
            .applyConnectionString(new ConnectionString(mongoConnectionString))
            .build())
        .keyVaultNamespace(keyVaultMongoNameSpace.getFullName())
        .kmsProviders(mongoKmsProvider.getConfig())
        .build();

    return ClientEncryptions.create(clientEncryptionSettings);
  }

  @Override
  public MongoClient mongoClient() {
    MongoClient mongoClient = super.mongoClient();

    MongoCollection<Document> keyVaultCollection = mongoClient.getDatabase(
        keyVaultDatabaseName).getCollection(keyVaultCollectionName);

    // Ensure that two data keys cannot share the same keyAltName.
    keyVaultCollection.createIndex(Indexes.ascending("keyAltNames"),
        new IndexOptions().unique(true)
            .partialFilterExpression(Filters.exists("keyAltNames")));

    return mongoClient;
  }

  @Override
  protected String getDatabaseName() {
    return new ConnectionString(mongoConnectionString).getDatabase();
  }
}