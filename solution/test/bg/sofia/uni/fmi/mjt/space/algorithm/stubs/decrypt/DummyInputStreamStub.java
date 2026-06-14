package bg.sofia.uni.fmi.mjt.space.algorithm.stubs.decrypt;

import java.io.InputStream;

public class DummyInputStreamStub extends InputStream {

    @Override
    public int read() {
        return 0; // do nothing
    }

}
