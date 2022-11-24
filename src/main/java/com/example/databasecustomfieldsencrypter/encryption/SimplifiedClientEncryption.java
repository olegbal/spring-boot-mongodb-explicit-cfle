package com.example.databasecustomfieldsencrypter.encryption;

import com.mongodb.client.vault.ClientEncryption;

import org.bson.BsonBinary;
import org.bson.BsonValue;

public interface SimplifiedClientEncryption extends ClientEncryption {

  BsonBinary encrypt(BsonValue value);
}
