package com.cdyweb.tc.comm;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;

public class RS232Device extends CommunicationDevice {

    private SerialPort serialPort = null;
    private InputStream inputStream = null;
    private OutputStream outputStream = null;
    private String portName = "COM1";
    private int baudRate = 9600;
    private int dataBits = SerialPort.DATABITS_8;
    private int stopBits = SerialPort.STOPBITS_1;
    private int parity = SerialPort.PARITY_NONE;

    public String getPortName() {
        return portName;
    }

    public void setPortName(String portName) {
        this.portName = portName;
    }

    public int getBaudRate() {
        return baudRate;
    }

    public void setBaudRate(int baudRate) {
        this.baudRate = baudRate;
    }

    public int getDataBits() {
        return dataBits;
    }

    public void setDataBits(int dataBits) {
        this.dataBits = dataBits;
    }

    public int getStopBits() {
        return stopBits;
    }

    public void setStopBits(int stopBits) {
        this.stopBits = stopBits;
    }

    public int getParity() {
        return parity;
    }

    public void setParity(int parity) {
        this.parity = parity;
    }

    public SerialPort getSerialPort() {
        return serialPort;
    }

    @Override
    public InputStream getInputStream() {
        return inputStream;
    }

    @Override
    public OutputStream getOutputStream() {
        return outputStream;
    }

    @Override
    public synchronized void open() throws IOException {
        if (isOpen()) {
            return;
        }

        CommPortIdentifier portIdentifier;
        CommPort comPort;

        Enumeration e = CommPortIdentifier.getPortIdentifiers();
        while (e.hasMoreElements()) {
            CommPortIdentifier c = (CommPortIdentifier)e.nextElement();
            log.info("found port: "+c.getName());
        }

        try {
            log.info("opening: "+portName);
            portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
        } catch (NoSuchPortException ex) {
            throw new IOException(ex);
        }

        if (portIdentifier.isCurrentlyOwned()) {
            throw new IOException("isCurrentlyOwned");
        }
        
        try {
            comPort = portIdentifier.open(this.getClass().getName(), 2000);
        } catch (PortInUseException ex) {
            throw new IOException(ex);
        }

        if (comPort instanceof SerialPort) {
            {
                try {
                    this.serialPort = (SerialPort) comPort;
                    serialPort.setSerialPortParams(getBaudRate(), getDataBits(), getStopBits(), getParity());
                    inputStream = serialPort.getInputStream();
                    outputStream = serialPort.getOutputStream();
                } catch (UnsupportedCommOperationException ex) {
                    throw new IOException(ex);
                }
            }
        }
        else {
            throw new IOException("Unknow com port");
        }
    }

    @Override
    public boolean isOpen() {
        return inputStream != null && outputStream != null && serialPort != null;
    }

    @Override
    public synchronized void close() throws IOException {
        if (!isOpen()) {
            return;
        }
        try {
            inputStream.close();
            outputStream.close();
            serialPort.close();
        } finally {
            inputStream = null;
            outputStream = null;
            serialPort = null;
        }
    }

    @Override
    protected Object getLockingObject() {
        if (isOpen()) {
            return serialPort;
        }
        return this;
    }

    @Override
    public void enableReceiveTimeout(long timeout) throws IOException {
        try { getSerialPort().enableReceiveTimeout((int)timeout); } catch (UnsupportedCommOperationException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public void disableReceiveTimeout() {
         getSerialPort().disableReceiveTimeout();
    }

    @Override
    public boolean isReceiveTimeoutEnabled() {
        return getSerialPort().isReceiveTimeoutEnabled();
    }

    @Override
    public long getReceiveTimeout() {
        return getSerialPort().getReceiveTimeout();
    }

    @Override
    public byte[] receive(long timeout, int byteCount) throws IOException {
        open();
        SerialPort p = getSerialPort();
        if (byteCount>0) {
            try {
                p.enableReceiveThreshold(byteCount);
            } catch (UnsupportedCommOperationException ex) {
                throw new IOException(ex);
            }
        } else {
            if (p.isReceiveThresholdEnabled()) {
                p.disableReceiveThreshold();
            }
        }
        return super.receive(timeout,byteCount);
    }
}
