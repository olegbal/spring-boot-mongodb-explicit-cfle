package com.example.databasecustomfieldsencrypter.converter;

import org.bson.types.Binary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class MongoBinaryToStringConverter implements Converter<Binary, String> {

  @Override
  public String convert(Binary source) {

    return source.toString();
  }
}
