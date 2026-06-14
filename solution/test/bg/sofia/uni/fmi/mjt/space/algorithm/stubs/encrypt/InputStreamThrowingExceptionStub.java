package bg.sofia.uni.fmi.mjt.space.algorithm.stubs.encrypt;

import java.io.InputStream;

public class InputStreamThrowingExceptionStub extends InputStream {

    @Override
    public int read() {
        throw new RuntimeException("read failed");
    }

}
