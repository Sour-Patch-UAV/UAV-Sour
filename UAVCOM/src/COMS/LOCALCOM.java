package COMS;
import java.io.IOException;

import com.fazecast.jSerialComm.SerialPortIOException;

import EXCEPTIONS.SerialException;
import PARENTS.CommunicationSupervisor;
import STATICS.*;

// UAV COM is an extension of Communication Supervisor, ComSuper. will maintain most
// properties for the current state of the communications between the Teensy and this java
public class LOCALCOM extends CommunicationSupervisor {
    
    // default constructor
    public LOCALCOM() {
        super();
        System.out.println("Local Communications Initialized");
    }

    // set serialport from user input for comm supervisor
    public void SET_SerialPortFromUser() throws SerialPortIOException {
        // set serialport from user input into comm supervisor from statics
        this.SET_OpenSerialPort(StartUp.MySerialPort());
    }

    // verify connections via teensy and java by sending initial messages (view code Startup.java and TeensyCode for messages)
    public void Verify_Communication() throws SerialException, IOException, InterruptedException {
        monitorTraffic();
        if (StartUp.VerifyMyTeensy(this.GET_OpenSerialPort(), this.GET_InputStream(), this.GET_OutputStream())) this.SET_TRUE_CommunicationIsSuccessful();
    };

    // monitor traffic method will call to supervisor to begin monitoring traffic through the serial port
    public void monitorTraffic() throws IOException {
        if (this.GET_OpenSerialPort() != null && this.GET_OpenSerialPort().isOpen()) {
            this.SET_InputStream(this.GET_OpenSerialPort().getInputStream());
            this.SET_OutputStream(this.GET_OpenSerialPort().getOutputStream());
        } else {
            System.out.println("Supervisor states the SerialPort is closed! Try Again.");
            System.exit(1);
        }
    };
};