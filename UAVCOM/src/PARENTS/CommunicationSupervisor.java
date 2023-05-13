package PARENTS;
import java.io.InputStream;
import java.io.OutputStream;

import com.fazecast.jSerialComm.SerialPort;

import EXCEPTIONS.SerialException;
import READERS.SerialReader;
import STATICS.GetClassName;

// communications class for storing properties based on the current state of the communications
public class CommunicationSupervisor {

    private boolean CommunicationSuccessful = false;
    private boolean PeripheralSuccessful = false;
    private boolean ActiveSerialReader = false; // states true if a reader is currently working on the line
    private SerialPort OpenSerialPort;
    private InputStream SerialPortInputStream;
    private OutputStream SerialPortOutputStream;
    
    // default constructor
    public CommunicationSupervisor() {
        System.out.println(GetClassName.THIS_CLASSNAME(this, "Communication Supervisor Initialized"));
    };
            
    public boolean GET_CommunicationIsSuccessful() {
        return this.CommunicationSuccessful;
    };

    private void SET_TRUE_CommunicationIsSuccessful() {
        this.CommunicationSuccessful = true;
    };

    private void SET_FALSE_CommunicationIsSuccessful() {
        this.CommunicationSuccessful = false;
    };

    public void CommuncationReportToSupervisor(boolean status) {
        if (status) SET_TRUE_CommunicationIsSuccessful();
        else SET_FALSE_CommunicationIsSuccessful();
    };

    private void SET_TRUE_PeripheralIsSuccessful() {
        this.PeripheralSuccessful = true;
    };

    private void SET_FALSE_PeripheralIsSuccessful() {
        this.PeripheralSuccessful = false;
    };

    public void PeripheralReportToSupervisor(boolean status) {
        if (status) SET_TRUE_PeripheralIsSuccessful();
        else SET_FALSE_PeripheralIsSuccessful();
    };

    public boolean GET_PeripheralIsSuccessful() {
        return this.PeripheralSuccessful;
    };

    public InputStream GET_InputStream() {
        try {
            if (SerialPortReportFromSupervisor()) {
                return this.SerialPortInputStream;
            }
        } catch (Exception e) {
            System.out.println(GetClassName.THIS_CLASSNAME(this, e.getMessage()));
        }
        return this.SerialPortInputStream; // can't get around that
    };

    public OutputStream GET_OutputStream() {
        try {
            if (SerialPortReportFromSupervisor()) {
                return this.SerialPortOutputStream;
            }
        } catch (Exception e) {
            System.out.println(GetClassName.THIS_CLASSNAME(this, e.getMessage()));
        }
        return this.SerialPortOutputStream; // can't get around that
    };

    public void IOStreamsReportToSuperVisor() {
        try {
            if (SerialPortReportFromSupervisor()) {
                SET_InputStream(GET_OpenSerialPort().getInputStream());
                SET_OutputStream(GET_OpenSerialPort().getOutputStream());
            }
        } catch (Exception e) {
            System.out.println(GetClassName.THIS_CLASSNAME(this, e.getMessage()));
        }
    };

    private void SET_InputStream(InputStream is) {
        this.SerialPortInputStream = is;
    };

    private void SET_OutputStream(OutputStream os) {
        this.SerialPortOutputStream = os;
    };

    public boolean SerialPortReportFromSupervisor() throws SerialException {
        if (this.OpenSerialPort.isOpen()) return true;
        throw new SerialException(); // error opening serial port
    };

    // overloaded method to set the serialport
    public void SerialPortReportToSupervisor(SerialPort sp) {
        SET_OpenSerialPort(sp);
        try {
            if (SerialPortReportFromSupervisor()) System.out.println(GetClassName.THIS_CLASSNAME(this, "Successful assignment of Serial Port!"));
        } catch (Exception e) {
            System.out.println(GetClassName.THIS_CLASSNAME(this, e.getMessage()));
        }
    };
    
    public SerialPort GET_OpenSerialPort() {
        try {
            if (SerialPortReportFromSupervisor()) return this.OpenSerialPort;
        } catch (SerialException e) {
            System.out.println(GetClassName.THIS_CLASSNAME(this, e.getMessage()));
        }
        return OpenSerialPort; // can't get around that :/ java, java
    };

    private void SET_OpenSerialPort(SerialPort sp) {
        sp.openPort();
        this.OpenSerialPort = sp;
    };

    // here, SerialSupervisor passed the verification of hiring the serialreader for a specific job, the Communicationsupervisor will check serialport
    // and verify that he can be added, if so, comm supervisor will add him to the serialport
    public boolean SerialReaderReportToSupervisor() {
        if (!this.ActiveSerialReader) return true; // worker is not in line
        return false; // worker is in line
    };

    // finally, add to the
    public void SerialReaderReportToSupervisor(SerialReader WorkerWaitingApproval) {
        this.GET_OpenSerialPort().addDataListener(WorkerWaitingApproval);
        this.ActiveSerialReader = true;
    };

    // remove the data listener
    public void RemoveSerialReaderFromLine() throws SerialException {
        if (!this.SerialReaderReportToSupervisor()) {
            this.GET_OpenSerialPort().removeDataListener();
            this.ActiveSerialReader = false;
        } else throw new SerialException("You attempted to remove a Worker from the line when one is currently not there!");
    };
};