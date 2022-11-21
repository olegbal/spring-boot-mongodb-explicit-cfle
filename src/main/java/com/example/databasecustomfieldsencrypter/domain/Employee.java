package com.example.databasecustomfieldsencrypter.domain;

import com.example.databasecustomfieldsencrypter.annotation.Encrypted;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Employee {

  @Id
  private String id;

  @Field
  @Encrypted
  private String fieldToEncrypt;
  
  @Field
  @Encrypted
  private SomeNestedClass someNestedClass;
  
  

}
