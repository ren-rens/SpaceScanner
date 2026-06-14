package bg.sofia.uni.fmi.mjt.space.algorithm;

import bg.sofia.uni.fmi.mjt.space.exception.CipherException;

import javax.crypto.SecretKey;
import java.io.InputStream;
import java.io.OutputStream;

public class Rijndael implements SymmetricBlockCipher {

    /**
     * Encrypts/decrypts data using AES (Rijndael) algorithm with the provided secret key.
     *
     * @param secretKey the encryption/decryption key
     * @throws IllegalArgumentException if secretKey is null
     */
    public Rijndael(SecretKey secretKey) {
        if (secretKey == null) {
            throw new IllegalArgumentException("Invalid secret key: NULL");
        }

        algorithm = new CipherAlgorithm(secretKey);
    }

    @Override
    public void encrypt(InputStream inputStream, OutputStream outputStream) throws CipherException {
        try {
            algorithm.encryptData(outputStream, inputStream);
        } catch (Exception e) {
            throw new CipherException("Encrypt operation did not end successfully", e);
        }
    }

    @Override
    public void decrypt(InputStream inputStream, OutputStream outputStream) throws CipherException {
        try {
            algorithm.decryptData(inputStream, outputStream);
        } catch (Exception e) {
            throw new CipherException("Decrypt operation did not end successfully", e);
        }
    }

    private final CipherAlgorithm algorithm;

}
