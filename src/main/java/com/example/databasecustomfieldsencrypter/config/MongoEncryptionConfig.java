package com.example.databasecustomfieldsencrypter.config;

import com.example.databasecustomfieldsencrypter.encryption.ImplicitOptionsClientEncryption;
import com.example.databasecustomfieldsencrypter.encryption.SimplifiedClientEncryption;
import com.example.databasecustomfieldsencrypter.encryption.kmsprovider.MongoKmsProvider;
import com.mongodb.ClientEncryptionSettings;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoNamespace;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.vault.EncryptOptions;
import com.mongodb.client.vault.ClientEncryption;
import com.mongodb.client.vault.ClientEncryptions;

import org.bson.BsonBinary;
import org.bson.Document;
import org.bson.types.Binary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;
import javax.annotation.PostConstruct;

@Configuration
@ConditionalOnProperty(prefix = "spring.data.mongodb.encryption", name = "enabled", havingValue = "true")
public class MongoEncryptionConfig {

  @Value("${spring.data.mongodb.encryption.keyvault.database}")
  public String keyVaultDatabaseName;

  @Value("${spring.data.mongodb.encryption.keyvault.collection}")
  public String keyVaultCollectionName;

  @Value("${spring.data.mongodb.encryption.algorithm}")
  public String mongoEncryptionAlgorithm;

  @Value("${spring.data.mongodb.uri}")
  public String mongoConnectionString;

  private final MongoKmsProvider mongoKmsProvider;
  private final MongoClient mongoClient;

  public MongoEncryptionConfig(MongoKmsProvider mongoKmsProvider, MongoClient mongoClient) {
    this.mongoKmsProvider = mongoKmsProvider;
    this.mongoClient = mongoClient;
  }

  @Bean
  public SimplifiedClientEncryption clientEncryption() {
    MongoNamespace keyVaultMongoNameSpace = new MongoNamespace(keyVaultDatabaseName,
        keyVaultCollectionName);
    EncryptOptions encryptOptions = new EncryptOptions(mongoEncryptionAlgorithm);

    ClientEncryptionSettings clientEncryptionSettings = createClientEncryptionSettings(
        keyVaultMongoNameSpace);

    getExistingEncryptionKeyOrCreate(keyVaultMongoNameSpace, encryptOptions,
        ClientEncryptions.create(clientEncryptionSettings));

    return new ImplicitOptionsClientEncryption(clientEncryptionSettings, encryptOptions);
  }

  //  @Override
  @PostConstruct
  public void initializeEncryptionIndexes() {
    MongoCollection<Document> keyVaultCollection =
        mongoClient.getDatabase(keyVaultDatabaseName).getCollection(keyVaultCollectionName);

    keyVaultCollection.createIndex(Indexes.ascending("keyAltNames"),
        new IndexOptions().unique(true)
            .partialFilterExpression(Filters.exists("keyAltNames")));
  }

  private ClientEncryptionSettings createClientEncryptionSettings(
      MongoNamespace keyVaultMongoNameSpace) {
    return ClientEncryptionSettings.builder()
        .keyVaultMongoClientSettings(
            MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(mongoConnectionString))
                .build()
        )
        .keyVaultNamespace(keyVaultMongoNameSpace.getFullName())
        .kmsProviders(mongoKmsProvider.getConfig())
        .build();
  }

  private void getExistingEncryptionKeyOrCreate(MongoNamespace keyVaultMongoNameSpace,
      EncryptOptions encryptOptions, ClientEncryption defaultClientEncryption) {

    MongoCollection<Document> collection = mongoClient
        .getDatabase(keyVaultMongoNameSpace.getDatabaseName())
        .getCollection(keyVaultMongoNameSpace.getCollectionName());
    Document first = collection.find().first();

    if (!Objects.isNull(first)) {
      encryptOptions.keyId(new BsonBinary(first.get("_id", Binary.class).getData()));
    } else {
      encryptOptions.keyId(defaultClientEncryption.createDataKey(mongoKmsProvider.getName()));
    }
  }
}