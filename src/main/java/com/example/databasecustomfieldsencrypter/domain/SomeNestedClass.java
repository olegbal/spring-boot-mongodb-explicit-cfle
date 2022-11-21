package com.example.databasecustomfieldsencrypter.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SomeNestedClass implements Serializable {
  public String someNestedString;
  public Long someNestedLong;
}
