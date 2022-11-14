package com.example.databasecustomfieldsencrypter.repository;

import com.example.databasecustomfieldsencrypter.annotation.EncryptedField;
import com.example.databasecustomfieldsencrypter.domain.Employee;
import com.mongodb.client.vault.ClientEncryption;

import org.bson.BsonValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterConvertEvent;
import org.springframework.data.mongodb.core.mapping.event.AfterLoadEvent;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.data.mongodb.core.mapping.event.BeforeSaveEvent;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class EmployeeMongoDbEventListener extends AbstractMongoEventListener<Employee> {

  @Autowired
  private ClientEncryption clientEncryption;

  @Autowired
  private BsonValue dataEncryptionKey;

  @Override
  public void onBeforeSave(BeforeSaveEvent<Employee> event) {
    List<Field> classMemberField = Arrays.asList(Employee.class.getDeclaredFields());

//    List<Field> encryptedFields = classMemberField.stream()
//        .filter(field -> field.isAnnotationPresent(EncryptedField.class))
//        .collect(Collectors.toList());
//    Document document = event.getDocument();
//
//    encryptedFields.forEach(field -> {
//      Object value = document.get(field.getName());
//      if(value!= null) {
//
//      }
//    });

    super.onBeforeSave(event);
  }

  @Override
  public void onBeforeConvert(BeforeConvertEvent<Employee> event) {
    List<Field> classMemberField = Arrays.asList(Employee.class.getDeclaredFields());

    List<Field> encryptedFields = classMemberField.stream()
        .filter(field -> field.isAnnotationPresent(EncryptedField.class))
        .collect(Collectors.toList());

    super.onBeforeConvert(event);
  }

  @Override
  public void onAfterConvert(AfterConvertEvent<Employee> event) {
    super.onAfterConvert(event);
  }


  @Override
  public void onAfterLoad(AfterLoadEvent<Employee> event) {
    super.onAfterLoad(event);
  }
}
