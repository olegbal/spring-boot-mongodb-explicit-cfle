package com.example.databasecustomfieldsencrypter.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ConditionalOnProperty(prefix = "spring.data.mongodb.encryption.keyvault", name = "provider", havingValue = "local")
public class LocalMongoKmsProvider implements MongoKmsProvider {

  @Value("${spring.data.mongodb.encryption.master-key}")
  public String masterKey;

  @Override
  public Map<String, Map<String, Object>> getConfig() {
    byte[] masterKeyBytes = masterKey.getBytes();

    return new HashMap<String, Map<String, Object>>() {{
      put("local", new HashMap<String, Object>() {{
        put("key", masterKeyBytes);
      }});
    }};
  }

  @Override
  public String getName() {
    return "local";
  }
}
