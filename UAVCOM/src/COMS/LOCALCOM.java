package COMS;
import java.io.IOException;

import com.fazecast.jSerialComm.SerialPortIOException;

import EXCEPTIONS.SerialException;
import PARENTS.CommunicationSupervisor;
import PARENTS.ReaderSupervisor;
import STATICS.*;

// LOCAL COM is an extension of Communication Supervisor. Communication Supervisor will maintain most
// verification and requests for getting the current serialport and streams in use and will also handle verification that the line is still working! (open)
public class LOCALCOM extends CommunicationSupervisor {
    private ReaderSupervisor readerSupervisor;
    
    // default constructor
    public LOCALCOM() {
        super();
        System.out.println(GetClassName.THIS_CLASSNAME(this,"Local Communications Initialized"));
        this.readerSupervisor = new ReaderSupervisor(this);
    }

    // set serialport from user input for comm supervisor
    public void SET_SerialPortFromUser() throws SerialPortIOException {
        // set serialport from user input into comm supervisor from statics
        this.SerialPortReportToSupervisor(StartUp.MySerialPort());
    }

    // verify connections via teensy and java by sending initial messages (view code Startup.java and TeensyCode for messages)
    public void Verify_Communication() throws SerialException, IOException, InterruptedException {
        monitorTraffic();
        this.readerSupervisor.HIRE_VERIFIER(false, 10); // args: persist, timeout (int) seconds
        this.readerSupervisor.GET_WORKER().set_optional_title("John, the verifier");
        StartUp.VerifyMyTeensy(this.GET_OutputStream()); // send message to input stream from startup message
    };

    // monitor traffic method will call to supervisor to begin monitoring traffic through the serial port
    private void monitorTraffic() throws IOException {
        try {
            if (this.SerialPortReportFromSupervisor()) this.IOStreamsReportToSuperVisor();
        } catch (SerialException e) {
            System.out.println(GetClassName.THIS_CLASSNAME(this, e.getMessage()));
        }
    };
};