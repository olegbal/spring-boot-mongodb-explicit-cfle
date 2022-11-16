package com.example.databasecustomfieldsencrypter.service;

import org.bson.BsonBinary;

public interface DataEncryptionKeyManager {

  BsonBinary getKey();
}
