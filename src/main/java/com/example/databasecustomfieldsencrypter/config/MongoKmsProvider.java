package com.example.databasecustomfieldsencrypter.config;

import java.util.Map;

public interface MongoKmsProvider {

  Map<String, Map<String, Object>> getConfig();

  String getName();
}
