package com.example.databasecustomfieldsencrypter.encryption;

import com.mongodb.ClientEncryptionSettings;
import com.mongodb.client.internal.ClientEncryptionImpl;
import com.mongodb.client.model.vault.EncryptOptions;

import org.bson.BsonBinary;
import org.bson.BsonValue;

public class ImplicitOptionsClientEncryption extends ClientEncryptionImpl implements
    SimplifiedClientEncryption {

  private final EncryptOptions encryptOptions;

  public ImplicitOptionsClientEncryption(
      ClientEncryptionSettings mongoClientEncryptionSettings,
      EncryptOptions explicitEncryptionOptions) {
    super(mongoClientEncryptionSettings);

    this.encryptOptions = explicitEncryptionOptions;
  }

  @Override
  public BsonBinary encrypt(BsonValue value) {
    return super.encrypt(value, encryptOptions);
  }
}
