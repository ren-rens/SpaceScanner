package bg.sofia.uni.fmi.mjt.space;

import bg.sofia.uni.fmi.mjt.space.algorithm.SymmetricBlockCipher;
import bg.sofia.uni.fmi.mjt.space.exception.CipherException;

import java.io.InputStream;
import java.io.OutputStream;

public class CipherThrowingExceptionStub implements SymmetricBlockCipher {

    public CipherThrowingExceptionStub() {

    }

    @Override
    public void encrypt(InputStream inputStream, OutputStream outputStream) throws CipherException {
        throw new CipherException("Cypher exception when encrypt");
    }

    @Override
    public void decrypt(InputStream inputStream, OutputStream outputStream) throws CipherException {
        throw new CipherException("Cypher exception when decrypt");
    }

}
