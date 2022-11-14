package com.example.databasecustomfieldsencrypter.controller;

import com.example.databasecustomfieldsencrypter.domain.Employee;
import com.example.databasecustomfieldsencrypter.repository.EmployeeRepository;
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
import com.mongodb.client.model.vault.DataKeyOptions;
import com.mongodb.client.model.vault.EncryptOptions;
import com.mongodb.client.vault.ClientEncryption;
import com.mongodb.client.vault.ClientEncryptions;

import org.bson.BsonBinary;
import org.bson.BsonBinarySubType;
import org.bson.BsonString;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.types.Binary;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
public class EmployeeController {

  private final EmployeeRepository employeeRepository;
  private final ClientEncryption clientEncryption;

  public EmployeeController(
      EmployeeRepository employeeRepository,
      ClientEncryption clientEncryption) {
    this.employeeRepository = employeeRepository;
    this.clientEncryption = clientEncryption;
  }

  @GetMapping("/test")
  public ResponseEntity<String> getEmployee() throws IOException {

    // This would have to be the same master key as was used to create the encryption key
    final byte[] localMasterKey = new byte[96];
    new SecureRandom().nextBytes(localMasterKey);
    Map<String, Map<String, Object>> kmsProviders = new HashMap<String, Map<String, Object>>() {{
      put("local", new HashMap<String, Object>() {{
        put("key", localMasterKey);
      }});
    }};
    MongoClientSettings clientSettings = MongoClientSettings.builder().build();
    MongoClient mongoClient = MongoClients.create(clientSettings);
// Set up the key vault for this example
    MongoNamespace keyVaultNamespace = new MongoNamespace("encryption.testKeyVault");
    MongoCollection<Document> keyVaultCollection = mongoClient
        .getDatabase(keyVaultNamespace.getDatabaseName())
        .getCollection(keyVaultNamespace.getCollectionName());
//    keyVaultCollection.drop();
// Ensure that two data keys cannot share the same keyAltName.
    keyVaultCollection.createIndex(Indexes.ascending("keyAltNames"),
        new IndexOptions().unique(true)
            .partialFilterExpression(Filters.exists("keyAltNames")));
    MongoCollection<Document> collection = mongoClient.getDatabase("test").getCollection("coll");
//    collection.drop(); // Clear old data
// Create the ClientEncryption instance
    ClientEncryptionSettings clientEncryptionSettings = ClientEncryptionSettings.builder()
        .keyVaultMongoClientSettings(MongoClientSettings.builder()
            .applyConnectionString(new ConnectionString("mongodb://localhost"))
            .build())
        .keyVaultNamespace(keyVaultNamespace.getFullName())
        .kmsProviders(kmsProviders)
        .build();
    ClientEncryption clientEncryption = ClientEncryptions.create(clientEncryptionSettings);
    BsonBinary dataKeyId = clientEncryption.createDataKey("local", new DataKeyOptions());
// Explicitly encrypt a field
    BsonBinary encryptedFieldValue = clientEncryption.encrypt(new BsonString("123456789"),
        new EncryptOptions("AEAD_AES_256_CBC_HMAC_SHA_512-Deterministic").keyId(dataKeyId));

    collection.insertOne(new Document("encryptedField", encryptedFieldValue));
    Document doc = collection.find().first();
    System.out.println(doc.toJson());
// Explicitly decrypt the field
    BsonValue encryptedField = clientEncryption.decrypt(
        new BsonBinary(BsonBinarySubType.ENCRYPTED,
            doc.get("encryptedField", Binary.class).getData()));

    return new ResponseEntity<>(encryptedField.toString(), HttpStatus.OK);
  }

  @PostMapping("/create")
  public ResponseEntity<Employee> create() {

    Employee secretString = employeeRepository.save(new Employee("1", "SecretString"));

    return new ResponseEntity<>(secretString, HttpStatus.OK);
  }

  @GetMapping("/create")
  public ResponseEntity<Employee> getEmp() {

    Optional<Employee> byId = employeeRepository.findById("1");
    return new ResponseEntity<>(byId.get(), HttpStatus.OK);
  }
}
