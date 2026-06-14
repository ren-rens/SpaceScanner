package bg.sofia.uni.fmi.mjt.space.algorithm.stubs.decrypt;

import java.io.OutputStream;

public class OutputStreamThrowingExceptionStub extends OutputStream {

    @Override
    public void write(int b) {
        throw new RuntimeException("Faild while writing");
    }

}
