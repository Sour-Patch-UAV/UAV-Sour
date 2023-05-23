package PARENTS;

import EXCEPTIONS.SerialException;
import READERS.SerialReader;
import STATICS.*;

import INTERFACES.*;

// reader supervisor will be over the serialreader workers who have some specific task
// their tasks will consist of reading from the inputstream and passing their findings to the supervisor for verification of a successful "pickup"
// upon success, supervisor will remove the work from the line, as now, we do not expect that worker's specific "pickup" from the stream
public class ReaderSupervisor {
    private CommunicationSupervisor BossOfReaderSupervisor;
    private SerialReader SupervisedWorker; // serial reader on the line
    private String ExpectedWorkerPickUp; // store the worker's specific pick up here
    
    // default constructor
    public ReaderSupervisor(CommunicationSupervisor cs) {
        this.BossOfReaderSupervisor = cs; // must let readersupervisor know of communicationsupervisor
        System.out.println(GetClassName.THIS_CLASSNAME(this, "Reader Supervisor Hired!" + " | " + "Boss is: " + this.BossOfReaderSupervisor));
    };

    public SerialReader GET_WORKER() {
        return this.SupervisedWorker;
    };

    // this method will hire the initial verifier worker for the line and will have them look for their pickup
    public void HIRE_COMM_VERIFIER(String pickup, boolean persist, int timeout) {
        this.ExpectedWorkerPickUp = pickup; // teensy will respond with this on startup (expected, strict)
        this.SupervisedWorker = new SerialReader(this, this.BossOfReaderSupervisor.GET_InputStream(), new SerialResponseForCommunication() {
            @Override
            public void onSetupCommunicationWithTeensy(boolean isTeensyResponded) {
                if (isTeensyResponded) System.out.println(GetClassName.THIS_CLASSNAME(this, "RECIEVED COMMS VERIFICATION FROM SERIAL READER SUPERVISOR"));
            }
        }, persist, timeout, true);
        
        CHECK_WORKER_NEEDED();
    };

    // this method will hire the initial peripheral worker for the line and will have them look for their pickup
    public void HIRE_PERIP_VERIFIER(String pickup, boolean persist, int timeout) {
        this.ExpectedWorkerPickUp = pickup; // teensy will respond with this on startup (expected, strict)
        this.SupervisedWorker = new SerialReader(this, this.BossOfReaderSupervisor.GET_InputStream(), new SerialResponseForPerihperal() {
            @Override
            public void onSetupPeripheralWithTeensy(boolean isTeensyResponded) {
                if (isTeensyResponded) System.out.println(GetClassName.THIS_CLASSNAME(this, "RECIEVED PERIPHERAL VERIFICATION FROM SERIAL READER SUPERVISOR"));
            }
        }, persist, timeout, true);
        
        CHECK_WORKER_NEEDED();
    };

    // this will create a general worker for the line, will handle sending the information from the simulation to the teensy through the port
    public void HIRE_GENERAL(boolean persist, int timeout) {
        this.ExpectedWorkerPickUp = Definitions.TEENSYRESPONSE; // now, look for teensy's responses!
        this.SupervisedWorker = new SerialReader(this, this.BossOfReaderSupervisor.GET_InputStream(), new SerialResponseForGeneral() {
            @Override
            public void onResponseFromTeensy(boolean isTeensyResponded) {
                if (isTeensyResponded) System.out.println("RECIEVED COMMS FROM TEENSY");
            }
        }, persist, timeout, false);
        
        CHECK_WORKER_NEEDED();
    };

    // reader supervisor states, hey, I want to add this worker to the line for this job!
    // commsupervisor checks the serialport is open still
    // add to datalistener
    private void CHECK_WORKER_NEEDED() {
        if (this.BossOfReaderSupervisor.SerialReaderReportToSupervisor()) this.BossOfReaderSupervisor.SerialReaderReportToSupervisor(SupervisedWorker);
    };

    // worker for specific purpose
    public void CHECK_STRICT_PICKUP(SerialResponseForCommunication ListenerResponse, String pickup) {
        if (ResponseManagement.CheckResponse_STRICT(pickup, this.ExpectedWorkerPickUp)) {
            this.BossOfReaderSupervisor.CommuncationReportToSupervisor(true);
            ListenerResponse.onSetupCommunicationWithTeensy(this.CHECK_PICKUP());
            REMOVE_WORKER(); // clock worker out
        }
    };

    // worker for specific purpose
    public void CHECK_STRICT_PICKUP(SerialResponseForPerihperal ListenerResponse, String pickup) {
        if (ResponseManagement.CheckPeripheralResponse(pickup, this.ExpectedWorkerPickUp)) {
            this.BossOfReaderSupervisor.PeripheralReportToSupervisor(true);
            ListenerResponse.onSetupPeripheralWithTeensy(this.CHECK_PICKUP());
            REMOVE_WORKER(); // clock worker out
        }
    };
    
    // general worker pickup check
    public void CHECK_LOOSE_PICKUP(SerialResponseForGeneral ListenerResponse, String pickup) {
        if (ResponseManagement.CheckResponse_LOOSE(pickup, this.ExpectedWorkerPickUp)) {
            this.BossOfReaderSupervisor.CommuncationReportToSupervisor(true);
        }
        // System.out.print("> ");
    };

    public boolean CHECK_PICKUP() {
        return this.BossOfReaderSupervisor.GET_CommunicationIsSuccessful();
    };

    // this will remove the worker from the line upon successful pickup
    private void REMOVE_WORKER() {
        try {
            this.BossOfReaderSupervisor.RemoveSerialReaderFromLine();
            this.OPEN_POSITION();
        } catch (SerialException e) {
            System.out.println(GetClassName.THIS_CLASSNAME(this, e.getMessage()));
        }
    };

    // reset worker position for cleanup of this class!
    private void OPEN_POSITION() {
        this.ExpectedWorkerPickUp = "";
        this.SupervisedWorker = null;
    };
};