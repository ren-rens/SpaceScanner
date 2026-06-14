package bg.sofia.uni.fmi.mjt.space.loader;

import java.io.Reader;

public class ReaderThrowingExceptionStub extends Reader {

    @Override
    public int read(char[] cbuf, int off, int len) {
        throw new RuntimeException("could not read file");
    }

    @Override
    public void close() {}

}