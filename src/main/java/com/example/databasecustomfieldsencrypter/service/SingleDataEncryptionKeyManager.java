package com.example.databasecustomfieldsencrypter.service;

import com.example.databasecustomfieldsencrypter.config.MongoKmsProvider;
import com.mongodb.MongoNamespace;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.vault.ClientEncryption;

import org.bson.BsonBinary;
import org.bson.Document;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@ConditionalOnProperty(prefix = "spring.data.mongodb.encryption", name = "enabled", havingValue = "true")
public class SingleDataEncryptionKeyManager implements DataEncryptionKeyManager {

  private final ClientEncryption clientEncryption;
  private final MongoKmsProvider mongoKmsProvider;
  private final MongoNamespace keyVaultMongoNameSpace;
  private final MongoTemplate mongoTemplate;

  private BsonBinary dataEncryptionKey;

  public SingleDataEncryptionKeyManager(ClientEncryption clientEncryption,
      MongoKmsProvider mongoKmsProvider, MongoNamespace keyVaultMongoNameSpace,
      MongoTemplate mongoTemplate) {
    this.clientEncryption = clientEncryption;
    this.mongoKmsProvider = mongoKmsProvider;
    this.keyVaultMongoNameSpace = keyVaultMongoNameSpace;
    this.mongoTemplate = mongoTemplate;
  }


  @Override
  public BsonBinary getKey() {
    if (Objects.isNull(dataEncryptionKey)) {
      //    TODO check if key already exists

      MongoCollection<Document> collection = mongoTemplate.getMongoDatabaseFactory()
          .getMongoDatabase(keyVaultMongoNameSpace.getDatabaseName())
          .getCollection(keyVaultMongoNameSpace.getCollectionName());
      Document first = collection.find().first();

      if (!Objects.isNull(first)) {
        dataEncryptionKey = first.get("keyMaterial", BsonBinary.class);
      } else {
        dataEncryptionKey = clientEncryption.createDataKey(mongoKmsProvider.getName());
      }
    }

    return dataEncryptionKey;

  }
}
