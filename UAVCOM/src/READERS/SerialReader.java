package READERS;

import com.fazecast.jSerialComm.*;

import PARENTS.ReaderSupervisor;
import STATICS.GetClassName;

import java.io.IOException;
import java.io.InputStream;
import INTERFACES.*;

public class SerialReader implements SerialPortDataListenerWithExceptions, SerialReaderInterface  {
    private String JobTitle = "Worker";
    private boolean PickUpStrict;
    private ReaderSupervisor SerialReaderBoss;
    private InputStream inputStream;
    private SerialResponseForCommunication communicationListener;
    private SerialResponseForPerihperal peripheralListener;
    private SerialResponseForGeneral generalListener;
    private boolean persist = false;
    private long timeout = 0;
    private long startTime = 0;

    public SerialReader(ReaderSupervisor rs, InputStream inputstream, SerialResponseForCommunication serialResponseListener, boolean strict) {
        this.SerialReaderBoss = rs;
        this.inputStream = inputstream;
        this.communicationListener = serialResponseListener;
        this.PickUpStrict = strict;
    }

    
    public SerialReader(ReaderSupervisor rs, InputStream inputstream, SerialResponseForPerihperal serialResponseListener, boolean strict) {
        this.SerialReaderBoss = rs;
        this.inputStream = inputstream;
        this.peripheralListener = serialResponseListener;
        this.PickUpStrict = strict;
    }

    public SerialReader(ReaderSupervisor rs, InputStream inputstream, SerialResponseForGeneral serialResponseListener, boolean strict) {
        this.SerialReaderBoss = rs;
        this.inputStream = inputstream;
        this.generalListener = serialResponseListener;
        this.PickUpStrict = strict;
    }

    public SerialReader(ReaderSupervisor rs, InputStream inputstream, SerialResponseForCommunication serialResponseListener, boolean persist, long timeout, boolean strict) {
        this.SerialReaderBoss = rs;
        this.inputStream = inputstream;
        this.communicationListener = serialResponseListener;
        this.persist = persist;
        this.timeout = timeout;
        this.PickUpStrict = strict;
    };

    
    public SerialReader(ReaderSupervisor rs, InputStream inputstream, SerialResponseForPerihperal serialResponseListener, boolean persist, long timeout, boolean strict) {
        this.SerialReaderBoss = rs;
        this.inputStream = inputstream;
        this.peripheralListener = serialResponseListener;
        this.persist = persist;
        this.timeout = timeout;
        this.PickUpStrict = strict;
    };

    public SerialReader(ReaderSupervisor rs, InputStream inputstream, SerialResponseForGeneral serialResponseListener, boolean persist, long timeout, boolean strict) {
        this.SerialReaderBoss = rs;
        this.inputStream = inputstream;
        this.generalListener = serialResponseListener;
        this.persist = persist;
        this.timeout = timeout;
        this.PickUpStrict = strict;
    };


    public void set_optional_title(String t) {
        this.JobTitle = t.trim();
    };

    @Override
    public void setPersist(boolean persist) {
        this.persist = persist;
    };

    @Override
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    };

    @Override
    public void serialEvent(SerialPortEvent event) {
        if (event.getEventType() == getListeningEvents()) {
            try {
                byte[] buffer = new byte[this.inputStream.available()];
                int length = this.inputStream.read(buffer);
                String message = new String(buffer, 0, length);
                System.out.println(GetClassName.THIS_CLASSNAME(this,"(JAVA) " + this.JobTitle + " Found Something! Sending to Supervisor: " + message));

                // send msg to supervisor for supervisor to verify, if so, supervisor will remove the listener 
                if (this.PickUpStrict) {
                    if (this.peripheralListener != null) this.SerialReaderBoss.CHECK_STRICT_PICKUP(this.peripheralListener, message); // instructions for strict checking is set by new serialreader object, check goes from trim to contain
                    else this.SerialReaderBoss.CHECK_STRICT_PICKUP(this.communicationListener, message);
                } else this.SerialReaderBoss.CHECK_LOOSE_PICKUP(this.generalListener, message);

                if (!persist) {
                    System.out.println(this.JobTitle + " is done.");
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
            };
        };
    };
    
    @Override
    public int getListeningEvents() {
        return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
    };

    @Override
    public void catchException(Exception arg0) {
        System.out.println("ERROR: " + arg0);
    };
};