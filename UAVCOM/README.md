
# Welcome to my Software's ReadMe!
#### Below, I have broken down my project into manageable sections so that you can easily access and review each piece individually.

##### Feel free to [Email Me](mailto:woodwind.turbeville@gmail.com?subject=From%20ReadMe&body=Howdy,), I appreciate it!
--- 
### Class Organization

The classes are ordered based on their respective hierarchy and contribution.

##### **_Format_**
> #### Function
> 
> - **Class Name**
>   - Description

--- 

#### *Serial Communications between Java and Teensy 4.0*
- [Serial Reader](https://github.com/Sour-Patch-UAV/UAV-Sour/blob/main/UAVCOM/src/READERS/SerialReader.java)
    - This class file is essential to how I am expecting and handling specific input from the Teensy during the initial communication verification and continued response management from the Teensy controller.
- [Communication Supervisor](https://github.com/Sour-Patch-UAV/UAV-Sour/blob/main/UAVCOM/src/PARENTS/CommunicationSupervisor.java)
    - This is a parent class for the local communication class, it handles most of the set/get methods for storing the serialport information, in addition, it's also the main handler for verifying read/write functionality for local comms, I find it essential to separate this responsibility to a parent class for local comms, as it reduces two separate problems between some overseeing class and worker class.
- [Local Communication "LOCALCOM"](https://github.com/Sour-Patch-UAV/UAV-Sour/blob/main/UAVCOM/src/COMS/LOCALCOM.java)
    -   This is a very essential class file as it handles the communication between the Teensy controller and the rest of the software. Additionally, this class contains methods that handle the initial functionality for verifying that Java can communicate with the Teensy controller.

#### *Setup Methods for SerialPort and Initial Verification*
- [MySerialPort](https://github.com/Sour-Patch-UAV/UAV-Sour/blob/main/UAVCOM/src/STATICS/StartUp.java#L66)
    - This static method reads input from the user and returns a SerialPort object specified with the name, the port should be the same as the Teensy's. The software is intelligent enough to verify that the port exists and is not already open by another process. The creation of SerialPort objects is provided by [jSerialComm](https://fazecast.github.io/jSerialComm/).
- [VerifyMyTeensy](https://github.com/Sour-Patch-UAV/UAV-Sour/blob/main/UAVCOM/src/STATICS/StartUp.java#L28)
    - This method accepts the verified SerialPort from the user-specified port, as well as the inputstream and outputstream respective to the port. With these I/O streams, the method sends a message to the Teensy through its port and handles the temporary assignment of a [SerialReader](#serial-communications-between-java-and-teensy-40) object, which expects the Teensy's response.