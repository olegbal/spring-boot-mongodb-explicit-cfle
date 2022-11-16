package com.example.databasecustomfieldsencrypter.eventlistener;

import com.example.databasecustomfieldsencrypter.annotation.Encrypted;
import com.example.databasecustomfieldsencrypter.domain.Employee;
import com.example.databasecustomfieldsencrypter.service.DataEncryptionKeyManager;
import com.mongodb.client.model.vault.EncryptOptions;
import com.mongodb.client.vault.ClientEncryption;

import org.bson.BsonBinary;
import org.bson.BsonBinarySubType;
import org.bson.BsonString;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.types.Binary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterLoadEvent;
import org.springframework.data.mongodb.core.mapping.event.BeforeSaveEvent;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(prefix = "spring.data.mongodb.encryption", name = "enabled", havingValue = "true")
public class EncryptionMongoDbEventListener extends AbstractMongoEventListener<Object> {

  @Autowired
  private ClientEncryption clientEncryption;

  @Autowired
  private DataEncryptionKeyManager dataEncryptionKeyManager;

  @Override
  public void onBeforeSave(BeforeSaveEvent<Object> event) {
    List<Field> classMemberField = Arrays.asList(Employee.class.getDeclaredFields());

    List<Field> encryptedFields = classMemberField.stream()
        .filter(field -> field.isAnnotationPresent(Encrypted.class))
        .collect(Collectors.toList());

    Document convertedEntity = event.getDocument();

    encryptedFields.forEach(field -> {

      String value = (String) convertedEntity.get(field.getName());

      BsonBinary encryptedFieldValue = clientEncryption.encrypt(new BsonString(value),
          new EncryptOptions("AEAD_AES_256_CBC_HMAC_SHA_512-Deterministic").keyId(
              dataEncryptionKeyManager.getKey()));

      convertedEntity.put(field.getName(), encryptedFieldValue);
    });

    super.onBeforeSave(event);
  }

  @Override
  public void onAfterLoad(AfterLoadEvent<Object> event) {
    List<Field> classMemberField = Arrays.asList(Employee.class.getDeclaredFields());

    List<Field> encryptedFields = classMemberField.stream()
        .filter(field -> field.isAnnotationPresent(Encrypted.class))
        .collect(Collectors.toList());

    Document loadedDocument = event.getDocument();

    encryptedFields.forEach(field -> {

      Binary value = loadedDocument.get(field.getName(), Binary.class);

      BsonValue decryptedField = clientEncryption.decrypt(
          new BsonBinary(BsonBinarySubType.ENCRYPTED, value.getData()));

      loadedDocument.put(field.getName(), decryptedField.asString().getValue());
    });

    super.onAfterLoad(event);
  }
}
