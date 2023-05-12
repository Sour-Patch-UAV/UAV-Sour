
# Welcome to Sour's ReadMe!
#### Below, I have broken down my project into manageable sections so that you can easily access and review each piece individually.

##### Feel free to [Email Me](mailto:woodwind.turbeville@gmail.com?subject=From%20ReadMe&body=Howdy,), I appreciate it!
--- 
### Class Organization

The classes are ordered based on their respective hierarchy and contribution.

##### **_Format_**
> #### Class Purpose
> 
> - **Class Name**
>   - Description

--- 

#### *Serial Communications between Java and Teensy 4.0*
- [Communication Supervisor](https://github.com/Sour-Patch-UAV/UAV-Sour/blob/main/UAVCOM/src/PARENTS/CommunicationSupervisor.java)
    - This is a parent class for the local communication class, it handles most of the set/get methods for storing the serialport information, in addition, it's also the main handler for verifying read/write functionality for local comms, I find it essential to separate this responsibility to a parent class for local comms, as it reduces two separate problems between some overseeing class and worker class.
- [Serial Reader Supervisor "ReaderSupervisor"](https://github.com/Sour-Patch-UAV/UAV-Sour/blob/main/UAVCOM/src/PARENTS/ReaderSupervisor.java)
    - This is a parent class which is intended to be the middle man between the serialreader and the communication supervisor. It's main purpose is to handle the assignment of "workers"(serial readers) to the inputstream and handle the verification of what they picked up. For instance, when I "hire" (assign) a verifier worker, I can pass the expected response from the teensy to the serial reader's supervisor, which will know the message expected, once verified, the reader supervisor will clock out the worker and relay to the communication supervisor that the communication is reported successful!
- [Serial Reader](https://github.com/Sour-Patch-UAV/UAV-Sour/blob/main/UAVCOM/src/READERS/SerialReader.java)
    - This class file is essential to how I am expecting and handling specific input from the Teensy during the initial communication verification and continued response management from the Teensy controller.
- [Local Communication "LOCALCOM"](https://github.com/Sour-Patch-UAV/UAV-Sour/blob/main/UAVCOM/src/COMS/LOCALCOM.java)
    -   This is a very essential class file as it handles the communication between the Teensy controller and the rest of the software. Additionally, this class contains methods that handle the initial functionality for verifying that Java can communicate with the Teensy controller.

#### *Setup Methods for SerialPort and Initial Verification*
- [MySerialPort](https://github.com/Sour-Patch-UAV/UAV-Sour/blob/main/UAVCOM/src/STATICS/StartUp.java#L58)
    - This static method reads input from the user and returns a SerialPort object specified with the name, the port should be the same as the Teensy's. The software is intelligent enough to verify that the port exists and is not already open by another process. The creation of SerialPort objects is provided by [jSerialComm](https://fazecast.github.io/jSerialComm/).
- [VerifyMyTeensy](https://github.com/Sour-Patch-UAV/UAV-Sour/blob/main/UAVCOM/src/STATICS/StartUp.java#L23)
    - This static method accepts the verified SerialPort from the user-specified port, as well as the outputstream respective to the port. With the O stream, the method sends a message to the Teensy through its port, which when the teensy responds, the [SerialReader](#serial-communications-between-java-and-teensy-40) object will complete its duties and delegate verification between its supervisor and the supervisor's boss, Communication supervisor.
- [VerifyPeripheralWorker](https://github.com/Sour-Patch-UAV/UAV-Sour/blob/main/UAVCOM/src/STATICS/StartUp.java#L34)
    - This static method will run operations to send a specific "movement" pattern for Teensy to follow, when Teensy does the action, it will return a message which contains the resulting combination of ms/angle pattern as recieved from VerifyPeripheralWorker's message. Doing this, allows the ReaderSupervisor to validate that the Teensy completed the correct actions, which was picked-up from the SerialWorker(Peripheral). 