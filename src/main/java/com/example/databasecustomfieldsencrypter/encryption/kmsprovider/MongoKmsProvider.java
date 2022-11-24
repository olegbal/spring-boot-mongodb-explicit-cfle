package com.example.databasecustomfieldsencrypter.encryption.kmsprovider;

import java.util.Map;

public interface MongoKmsProvider {

  Map<String, Map<String, Object>> getConfig();

  String getName();
}
