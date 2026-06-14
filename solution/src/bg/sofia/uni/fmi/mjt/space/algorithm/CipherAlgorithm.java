package bg.sofia.uni.fmi.mjt.space.algorithm;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;

public class CipherAlgorithm {

    public CipherAlgorithm(SecretKey secretKey) {
        this.secretKey = secretKey;
    }

    public void encryptData(OutputStream file, InputStream data) throws Exception {
        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, this.secretKey);

        var inputStream = new BufferedInputStream(data);
        var outputStream = new CipherOutputStream(file, cipher);

        try (outputStream; inputStream) {
            byte[] buffer = new byte[KILOBYTE];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0 , bytesRead);
                outputStream.flush();
            }
        }
    }

    public void decryptData(InputStream inputStream, OutputStream outputStream) throws Exception {
        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, this.secretKey);

        try (var cipherInputStream = new CipherInputStream(inputStream, cipher);
             OutputStream decryptedOutputStream = outputStream) {

            byte[] buffer = new byte[KILOBYTE];
            int bytesRead;

            while ((bytesRead = cipherInputStream.read(buffer)) != -1) {
                decryptedOutputStream.write(buffer, 0, bytesRead);
                decryptedOutputStream.flush();
            }
        }
    }

    private final SecretKey secretKey;
    private static final int KILOBYTE = 1024;
    private static final String ENCRYPTION_ALGORITHM = "AES"; // //  Advanced Encryption Standard

}