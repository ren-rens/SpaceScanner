package bg.sofia.uni.fmi.mjt.space.algorithm;

import bg.sofia.uni.fmi.mjt.space.algorithm.stubs.decrypt.DummyInputStreamStub;
import bg.sofia.uni.fmi.mjt.space.algorithm.stubs.decrypt.OutputStreamThrowingExceptionStub;
import bg.sofia.uni.fmi.mjt.space.algorithm.stubs.encrypt.DummyOutputStreamStub;
import bg.sofia.uni.fmi.mjt.space.algorithm.stubs.encrypt.InputStreamThrowingExceptionStub;
import bg.sofia.uni.fmi.mjt.space.exception.CipherException;
import org.junit.jupiter.api.Test;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.*;

class RijndaelTest {

    @Test
    void testEncryptWhenOperationEndsUnsuccessfully() throws Exception {
        SecretKey key = KeyGenerator.getInstance("AES").generateKey();
        Rijndael rijndael = new Rijndael(key);


        assertThrows(CipherException.class,
            () -> rijndael.encrypt(new InputStreamThrowingExceptionStub(), new DummyOutputStreamStub()),
            "When testing encrypt with failing input stream" +
                "should throw CipherException");
    }

    @Test
    void testDecryptWhenOperationEndsUnsuccessfully() throws NoSuchAlgorithmException {
        SecretKey key = KeyGenerator.getInstance("AES").generateKey();
        Rijndael rijndael = new Rijndael(key);

        assertThrows(CipherException.class,
            () -> rijndael.decrypt(new DummyInputStreamStub(), new OutputStreamThrowingExceptionStub()),
            "When testing decrypt with failing input stream" +
                "should throw CipherException");
    }

    @Test
    void testInitializeCipherWithNullSecretKey() {
        assertThrows(IllegalArgumentException.class, () -> new Rijndael(null),
            "When trying to initialize Rijndael cipher with null" +
                "should throw IllegalArgumentException");
    }

}