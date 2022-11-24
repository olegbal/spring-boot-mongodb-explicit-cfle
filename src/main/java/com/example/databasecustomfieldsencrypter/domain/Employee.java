package com.example.databasecustomfieldsencrypter.domain;

import com.example.databasecustomfieldsencrypter.annotation.Encrypted;
//
//import com.bol.secure.Encrypted;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

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

  @Field
  @Encrypted
  private List<SomeNestedClass> someNestedClassList;

  @Field
  @Encrypted
  private List<String> someStringList;

}
