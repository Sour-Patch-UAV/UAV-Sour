package READERS;

import com.fazecast.jSerialComm.*;

import PARENTS.ReaderSupervisor;
import STATICS.GetClassName;

import java.io.IOException;
import java.io.InputStream;

public class SerialReader implements SerialPortDataListenerWithExceptions, SerialReaderInterface  {
    private String JobTitle = "Worker";
    private ReaderSupervisor SerialReaderBoss;
    private InputStream inputStream;
    private SerialResponseInitialize listener;
    private boolean persist = false;
    private long timeout = 0;
    private long startTime = 0;

    public SerialReader(ReaderSupervisor rs, InputStream inputstream, SerialResponseInitialize serialResponseListener) {
        this.SerialReaderBoss = rs;
        this.inputStream = inputstream;
        this.listener = serialResponseListener;
    }

    public SerialReader(ReaderSupervisor rs, InputStream inputstream, SerialResponseInitialize serialResponseListener, boolean persist, long timeout) {
        this.SerialReaderBoss = rs;
        this.inputStream = inputstream;
        this.listener = serialResponseListener;
        this.persist = persist;
        this.timeout = timeout;
    }

    public void set_optional_title(String t) {
        this.JobTitle = t.trim();
    };

    @Override
    public void setPersist(boolean persist) {
        this.persist = persist;
    }
    @Override
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    @Override
    public void serialEvent(SerialPortEvent event) {
        if (event.getEventType() == getListeningEvents()) {
            try {
                byte[] buffer = new byte[this.inputStream.available()];
                int length = this.inputStream.read(buffer);
                String message = new String(buffer, 0, length);
                System.out.println(GetClassName.THIS_CLASSNAME(this,"(JAVA) Line Worker Found Something! Sending to Supervisor: " + message));
                // send msg to supervisor for supervisor to verify, if so, supervisor will remove the listener 
                this.SerialReaderBoss.CHECK_PICKUP(listener, message);
                if (!persist) {
                    System.out.println(this.JobTitle + " is done.");
                } else {
                    System.out.println(GetClassName.THIS_CLASSNAME(this, "This listener did not see the intended response."));
                }
            } catch (IOException e) {
                System.err.println("Error reading from serial port: " + e);
            }
            if (timeout > 0 && !persist) {
                long currentTime = System.currentTimeMillis();
                if (startTime == 0) {
                    startTime = currentTime;
                } else if (currentTime - startTime > timeout) {
                    System.out.println(this.JobTitle + " is in overtime! Did not find pickup within their permitted time.");
                }
            }
        }
    }
    
    @Override
    public int getListeningEvents() {
        return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
    }

    @Override
    public void catchException(Exception arg0) {
        System.out.println("ERROR: " + arg0);
    }
}