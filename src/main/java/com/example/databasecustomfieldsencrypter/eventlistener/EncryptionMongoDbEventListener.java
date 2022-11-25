package com.example.databasecustomfieldsencrypter.eventlistener;

import com.example.databasecustomfieldsencrypter.annotation.Encrypted;
import com.example.databasecustomfieldsencrypter.encryption.SimplifiedClientEncryption;

import org.bson.BsonBinary;
import org.bson.BsonBinarySubType;
import org.bson.Document;
import org.bson.types.Binary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterLoadEvent;
import org.springframework.data.mongodb.core.mapping.event.BeforeSaveEvent;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(prefix = "spring.data.mongodb.encryption", name = "enabled", havingValue = "true")
public class EncryptionMongoDbEventListener extends AbstractMongoEventListener<Object> {

  @Autowired
  private SimplifiedClientEncryption clientEncryption;

  @Override
  public void onAfterLoad(AfterLoadEvent<Object> event) {
    List<Field> classMemberField = Arrays.asList(event.getType().getDeclaredFields());

    List<Field> encryptedFields = classMemberField.stream()
        .filter(field -> field.isAnnotationPresent(Encrypted.class)).collect(Collectors.toList());

    Document loadedDocument = event.getDocument();

    if (!Objects.isNull(loadedDocument)) {

      encryptedFields.forEach(field -> {
        if (loadedDocument.containsKey(field.getName())) {

          Optional.ofNullable(loadedDocument.get(field.getName())).ifPresent((value -> {
            byte[] encryptedBytes = loadedDocument.get(field.getName(), Binary.class).getData();

            BsonBinary decryptedField = clientEncryption.decrypt(
                new BsonBinary(BsonBinarySubType.ENCRYPTED, encryptedBytes)).asBinary();

            loadedDocument.put(field.getName(),
                SerializationUtils.deserialize(decryptedField.getData()));
          }));
        }
      });
    }
    super.onAfterLoad(event);
  }

  @Override
  public void onBeforeSave(BeforeSaveEvent<Object> event) {
    List<Field> classMemberField = Arrays.asList(event.getSource().getClass().getDeclaredFields());
    List<Field> encryptedFields = classMemberField.stream()
        .filter(field -> field.isAnnotationPresent(Encrypted.class))
        .collect(Collectors.toList());

    Document document = event.getDocument();

    if (!Objects.isNull(document)) {

      encryptedFields.forEach(field -> {
        if (document.containsKey(field.getName())) {
          Optional.ofNullable(document.get(field.getName())).ifPresent((value -> {

            BsonBinary valueAsBsonBinary = new BsonBinary(SerializationUtils.serialize(value));
            BsonBinary encryptedFieldValue = clientEncryption.encrypt(valueAsBsonBinary);
            document.put(field.getName(), encryptedFieldValue);
          }));
        }
      });
    }

    super.onBeforeSave(event);
  }
}
