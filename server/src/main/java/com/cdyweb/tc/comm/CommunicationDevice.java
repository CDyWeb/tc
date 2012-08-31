package com.cdyweb.tc.comm;

import java.io.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class CommunicationDevice implements CommunicationInterface {

    protected final Log log = LogFactory.getLog(getClass());
    private Object myLock = null;

    public boolean isOpen() {
        return true;
    }

    public void open() throws IOException {
        // noop
    }
    public void close() throws IOException {
        // noop
    }

    public void send(byte[] data) throws IOException {
        open();
        getOutputStream().write(data);
    }
    public byte[] receive(long timeout) throws IOException {
        return receive(timeout, 0);
    }
    public byte[] receive(long timeout, int byteCount) throws IOException {
        open();
        if (timeout>0) {
            enableReceiveTimeout(timeout);
        } else {
            disableReceiveTimeout();
        }
        int l=byteCount>0?byteCount:getInputStream().available();
        byte[] buf=new byte[l];
        if (l==0) {
            return buf;
        }
        int bytesRead = getInputStream().read(buf);
        if (bytesRead<0) {
            return new byte[0];
        }
        if (bytesRead<l) {
            byte[] buf2=new byte[bytesRead];
            System.arraycopy(buf,0,buf2,0,bytesRead);
            buf=buf2;
        }
        return buf;
    }
    public byte[] sendAndReceive(byte[] data, long timeout) throws IOException {
        return sendAndReceive(data, timeout, 0);
    }
    public byte[] sendAndReceive(byte[] data, long timeout, int byteCount) throws IOException {
        send(data);
        return receive(timeout,byteCount);
    }

    public void enableReceiveTimeout(long timeout) throws IOException {
        // noop
    }
    public void disableReceiveTimeout() {
        //noop
    }
    public boolean isReceiveTimeoutEnabled() {
        return false;
    }
    public long getReceiveTimeout() {
        return 0;
    }
    public InputStream getInputStream() {
        return new ByteArrayInputStream(new byte[0]);
    }
    public OutputStream getOutputStream() {
        return new ByteArrayOutputStream();
    }

    protected Object getLockingObject() {
        return this;
    }
    public synchronized Object lockExclusive() throws IOException {
        while (myLock != null) {
            try {
                wait();
            } catch (InterruptedException ex) {
                throw new IOException(ex);
            }
        }
        myLock = getLockingObject();
        return myLock;
    }
    public synchronized void unlockExclusive() {
        myLock = null;
        notifyAll();
    }
}
