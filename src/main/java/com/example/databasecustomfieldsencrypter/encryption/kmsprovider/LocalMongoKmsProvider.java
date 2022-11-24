package com.example.databasecustomfieldsencrypter.encryption.kmsprovider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ConditionalOnExpression("'${spring.data.mongodb.encryption.enabled:false}' and '${spring.data.mongodb.encryption.keyvault.provider: }' == 'local'")
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
