package PARENTS;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.fazecast.jSerialComm.SerialPort;

import STATICS.*;

// communications class for storing properties based on the current state of the communications
public class CommunicationSupervisor {

    private boolean CommunicationSuccessful = false;
    private SerialPort OpenSerialPort;
    private InputStream SerialPortInputStream;
    private OutputStream SerialPortOutputStream;
    
    // default constructor
    public CommunicationSupervisor() {
        System.out.println("Communication Supervisor Initialized");
    };

    public void SerialPortReportToSupervisor(SerialPort sp) {
        if (sp.isOpen()) {
            System.out.println("Serial Port is currently in use! Try Again.");
            System.exit(1);
        } else {
            sp.openPort();
        }
    } 
            
    public boolean GET_CommunicationIsSuccessful() {
        return this.CommunicationSuccessful;
    }

    public void SET_TRUE_CommunicationIsSuccessful() {
        this.CommunicationSuccessful = true;
    }

    public void SET_FALSE_CommunicationIsSuccessful() {
        this.CommunicationSuccessful = false;
    }

    public InputStream GET_InputStream() {
        return this.SerialPortInputStream;
    }

    public OutputStream GET_OutputStream() {
        return this.SerialPortOutputStream;
    }

    public void SET_InputStream(InputStream is) {
        this.SerialPortInputStream = is;
    }

    public void SET_OutputStream(OutputStream os) {
        this.SerialPortOutputStream = os;
    }

    public SerialPort GET_OpenSerialPort() {
        if (this.OpenSerialPort.isOpen()) return this.OpenSerialPort;
        else {
            System.out.println("SerialPort is not open! Attempted to GET");
            System.exit(1);
            return this.OpenSerialPort;
        }
    }

    public void SET_OpenSerialPort(SerialPort sp) {
        SerialPortReportToSupervisor(sp); // send to open by supervisor if not in use!
        this.OpenSerialPort = sp;
    }
    
};