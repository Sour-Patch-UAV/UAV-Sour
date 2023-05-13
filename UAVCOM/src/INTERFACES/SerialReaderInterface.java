package INTERFACES;

import com.fazecast.jSerialComm.SerialPortEvent;

public interface SerialReaderInterface {
    void setPersist(boolean persist);
    void setTimeout(long timeout);
    void serialEvent(SerialPortEvent event);
}