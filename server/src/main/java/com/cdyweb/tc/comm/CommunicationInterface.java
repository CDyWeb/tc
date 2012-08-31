package com.cdyweb.tc.comm;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface CommunicationInterface {

    boolean isOpen();
    void open() throws IOException;
    void close() throws IOException;

    void send(byte[] data) throws IOException;
    byte[] receive(long timeout, int byteCount) throws IOException;
    byte[] sendAndReceive(byte[] data, long timeout, int byteCount) throws IOException;

    void enableReceiveTimeout(long timeout) throws IOException;
    void disableReceiveTimeout();
    boolean isReceiveTimeoutEnabled();
    long getReceiveTimeout();
    InputStream getInputStream();
    OutputStream getOutputStream();

    Object lockExclusive() throws IOException;
    void unlockExclusive() throws IOException;
}
