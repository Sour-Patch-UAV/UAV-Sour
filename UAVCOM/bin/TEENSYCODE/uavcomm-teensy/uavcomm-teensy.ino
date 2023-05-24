#include <Wire.h>
// MPU GYRO is for regulating the angle or turn sent from java
const int MPU_ADDR = 0x68; // MPU GYRO ADDRESS

// necessary vars for MPU GYRO
int16_t accelerometer_x, accelerometer_y, accelerometer_z; // variables for accelerometer raw data
int16_t gyro_x, gyro_y, gyro_z; // variables for gyro raw data
int16_t temperature; // variables for temperature data

// Teensy 4.0 SW
#include <Servo.h>
Servo servos; // global var

#include <string> // stoi
// include vector for non fixed size arr
#include <vector>
#include <thread> // thread for servo management during state based requests
#include <queue> // queue data structure in the event more data is received from the Serial, and the current is not complete

using namespace std;
const size_t bufferLength = 128; // define the maximum buffer length
uint8_t buffer[bufferLength]; // create a buffer array of bytes
const size_t headerLength = 5; // starts at 5, move to 1 after readys
uint8_t headerBytes[headerLength]; // create an array to store the header bytes | header bytes states -java ONLY
// test servo is TestServo
const uint8_t testServo[] = {32, 84, 101, 115, 116, 83, 101, 114, 118, 111};
// hello teensy is = -java Teensy, are you awake?
const uint8_t helloTeensy[] = {32, 84, 101, 101, 110, 115, 121, 44, 32, 97, 114, 101, 32, 121, 111, 117, 32, 97, 119, 97, 107, 101, 63};
const uint8_t PRE_JAVA[] = {45, 106, 97, 118, 97}; // states -java
bool commready = false; // initialize pr (comms Ready) boolean to false
bool peripheralcheck = false; // init periph. boolean to false

// aileron angle at the moment in testing!!
const int DEFAULT_AILERON_ANGLE = 0;
int angle = DEFAULT_AILERON_ANGLE;

int STATE_ANGLE = 0;
int STATE_ELEVATION = 0;
int STATE_POWER = 0;

// define shell cmd vars
const uint8_t AILERON = 97; //a
const uint8_t ELEVATOR = 101; //e
const uint8_t THRUST = 109; //m
const uint8_t TALK = 116; //t
const uint8_t STATE = 115; //s
const uint8_t RESET = 114; //r

// servo headers as vector
vector<int> mvmt;
vector<thread> SPAWNS;

void setup() {
  // put your setup code here, to run once:
  Serial.begin(115200);
  Serial3.begin(115200);
  pinMode(23, OUTPUT); // output to 23
  servos.attach(23);
  // Needed for MPU GYRO
  Wire.begin();
  Wire.beginTransmission(MPU_ADDR); // Begins a transmission to the I2C slave (GY-521 board)
  Wire.write(0x6B); // PWR_MGMT_1 register
  Wire.write(0); // set to zero (wakes up the MPU-6050)
  Wire.endTransmission(true);
};

void CLEANUP() {
  CLEANBUF();
  CLEANMVMT();
}

void CLEANBUF() {
  memset(headerBytes, 0, sizeof(headerBytes));
  memset(buffer, 0, sizeof(buffer));
};

void CLEANMVMT() {
  mvmt.clear();
};

void PRINT_BUF(size_t i) {
  // loop through the stored bytes
  for (size_t j = 0; j < i-headerLength; j++) {
    Serial3.print(buffer[j]); // print each byte to the Serial Monitor
    Serial3.print(" "); // add a space between each byte
  }
  Serial3.println(); // add a newline at the end of the printed bytes
  // print the header bytes to the Serial Monitor
  Serial3.print("Header: ");
  for (size_t j = 0; j < headerLength; j++) {
    Serial3.print(headerBytes[j]); // print each byte to the Serial Monitor
    Serial3.print(" "); // add a space between each byte
  }
};

void VERIFY(size_t i) {
  Serial3.println("Attempting to Verify Java message.");
  PRINT_BUF(i);
  if (memcmp(buffer, helloTeensy, sizeof(helloTeensy)) == 0) {
    Serial3.println("Verified COM w/ JAVA");
    commready = true; // set commready to true if the message is expected one
    Serial.write("-teen Java, Im awake");
  }
  else if (memcmp(buffer, testServo, sizeof(testServo)) == 0) {
    int start = (sizeof(headerBytes) + sizeof(testServo)) - headerLength;
    string num;
    for (size_t j = start; j < sizeof(buffer); j++) {
      if (buffer[j] != 0 && buffer[j] != 44) {
        num.push_back(char(buffer[j])); // push to num to concat -> 6,22 -> 6 | 2 <- 2 => 22
      } else {
        if (!num.empty()) {
          mvmt.push_back(stoi(num));
          num.clear();
        }
      }
    };

    for (size_t j = 0; j < mvmt.size(); j++) {
      Serial3.print("MVMT VEC INDEX: ");
      Serial3.print(j);
      Serial3.print(" VAL: ");
      Serial3.println(mvmt.at(j));
    }

    // write back to java the successul positions of the servos
    string one = "-teen ";
    string two = TestServo();
    string three = one + two;
    Serial.write(three.c_str()); // write back to java for confirmation
    peripheralcheck = true;

    // clean up
    CLEANUP();
  };
};

char tmp_str[7]; // temporary variable used in convert function

char* convert_int16_to_str(int16_t i) { // converts int16 to string. Moreover, resulting strings will have the same length in the debug monitor.
  sprintf(tmp_str, "%6d", i);
  return tmp_str;
}

void GYRO_TRANSMISSION() {
  Wire.beginTransmission(MPU_ADDR);
  Wire.write(0x3B); // starting with register 0x3B (ACCEL_XOUT_H) [MPU-6000 and MPU-6050 Register Map and Descriptions Revision 4.2, p.40]
  Wire.endTransmission(false); // the parameter indicates that the Arduino will send a restart. As a result, the connection is kept active.
  Wire.requestFrom(MPU_ADDR, 14, true); // request a total of 7*2=14 registers
  
  // "Wire.read()<<8 | Wire.read();" means two registers are read and stored in the same variable
  accelerometer_x = Wire.read()<<8 | Wire.read(); // reading registers: 0x3B (ACCEL_XOUT_H) and 0x3C (ACCEL_XOUT_L)
  accelerometer_y = Wire.read()<<8 | Wire.read(); // reading registers: 0x3D (ACCEL_YOUT_H) and 0x3E (ACCEL_YOUT_L)
  accelerometer_z = Wire.read()<<8 | Wire.read(); // reading registers: 0x3F (ACCEL_ZOUT_H) and 0x40 (ACCEL_ZOUT_L)
  temperature = Wire.read()<<8 | Wire.read(); // reading registers: 0x41 (TEMP_OUT_H) and 0x42 (TEMP_OUT_L)
  gyro_x = Wire.read()<<8 | Wire.read(); // reading registers: 0x43 (GYRO_XOUT_H) and 0x44 (GYRO_XOUT_L)
  gyro_y = Wire.read()<<8 | Wire.read(); // reading registers: 0x45 (GYRO_YOUT_H) and 0x46 (GYRO_YOUT_L)
  gyro_z = Wire.read()<<8 | Wire.read(); // reading registers: 0x47 (GYRO_ZOUT_H) and 0x48 (GYRO_ZOUT_L)
  
  // print out data
  Serial3.print("aX = "); Serial3.print(convert_int16_to_str(accelerometer_x));
  Serial3.print(" | aY = "); Serial3.print(convert_int16_to_str(accelerometer_y));
  Serial3.print(" | aZ = "); Serial3.print(convert_int16_to_str(accelerometer_z));
  // the following equation was taken from the documentation [MPU-6000/MPU-6050 Register Map and Description, p.30]
  Serial3.print(" | tmp = "); Serial3.print(temperature/340.00+36.53);
  Serial3.print(" | gX = "); Serial3.print(convert_int16_to_str(gyro_x));
  Serial3.print(" | gY = "); Serial3.print(convert_int16_to_str(gyro_y));
  Serial3.print(" | gZ = "); Serial3.print(convert_int16_to_str(gyro_z));
  Serial3.println();
};

// here, I will send to appropriate command and fulfill with the provided instruction
bool TRANSLATE(size_t i) {
  Serial3.println("Incoming Transmission...");
  PRINT_BUF(i);
  // look at first letter: a = aileron, m = motor, e = elevator, t - transmitter
  // once letter is acknowledged, run the specific method :)

  // note that once we have seen the pre-java verification, we will see a space, skip, then read the first char
  // that char IS the transmission case, and will require some parameters after one more space ex: -java a 20, now let " " be "_" -> "-java_a_20", this is HOW we should see it
  // understand too that a case with multiple parameters is possible, as follows: "-java a 6,8,9,10,11,12" where the comma will be our greatest ally!

  int index = 1;// look for cmd at 1, as this will be after the first space!
  uint8_t cmd = buffer[index];

  switch(cmd) {
    case AILERON:
      Serial3.println("Moving AILERON(s)");
      // store instructions into mvmt vector, and then send to appropriate method
      index += 2;
      FILL_MVMT_VECTOR(index); // fill vector with parameters
      return CMD_MOVE_SERVO(servos);
    case STATE:
      Serial3.println("Setting New State!");
      index += 2;
      FILL_MVMT_VECTOR(index);
      return CMD_SET_STATE();
    case TALK:
      Serial3.println("Talking Now");
      index += 2;
      return CMD_TALK(index);
    case RESET:
      Serial3.println("Reset Command");
      // store instructions into mvmt vector, and then send to appropriate method
      MoveServo(servos, DEFAULT_AILERON_ANGLE);
      return true;
    default:
      return false;
  };
};

// returns a string of concat info from the location of the servo
string TestServo() {
  if (!mvmt.empty()) { // we have things to read!
    string returnmessage;
    for (size_t j = 0; j < mvmt.size(); j++) {
      int ms = mvmt.at(j);
      ms *= 100;
      // send to move for real feedback
      delay(200);
      MoveServo(servos, ms);
      returnmessage += to_string(servos.readMicroseconds()); // add for the return message
    }
    return returnmessage;
  }
  return "no mvmt";
};

void MoveServo(Servo s, int ms) {
  Serial3.print("Moving Servo to ms: "); Serial3.println(to_string(ms).c_str());
  s.writeMicroseconds(ms);
};

void loop() {
  if (Serial.available()) { // if some data is available to read
    size_t i = 0; // init pointer
    while (Serial.available() && i < bufferLength) { // loop through the incoming bytes
      if (i < headerLength) { // if we're still reading the header bytes
        headerBytes[i] = Serial.read(); // store the byte in the header array
      } else { // if we're reading the rest of the message
        buffer[i-headerLength] = Serial.read(); // store the byte in the buffer array
      }
      i++; // increment the counter variable
    }

    // check to see if the message is from java "-java"
    if (check_for_java()) {
      if (commready && peripheralcheck) {
        bool attempt = TRANSLATE(i); // if attempt is true, means, successful!
        if (attempt) Serial3.println("Successful Command!");
        else Serial3.println("Command was not succesful!");
      };
      if (!commready || !peripheralcheck) VERIFY(i);
    }
    // clean up
    CLEANUP();
  }
  delay(1000);

  Serial3.println("--------------------------------");
  Serial3.print("Comms Ready? " ); Serial3.println(commready);
  Serial3.println("--------------------------------");
  Serial3.print("Peripheral Ready? " ); Serial3.println(peripheralcheck);
  Serial3.println("--------------------------------");
  Serial3.println(PRINT_STATE().c_str());
  Serial3.println("--------------------------------");

  // code for send information back to java for reading!
  // if (commready && peripheralcheck) {
  //   // write to java for most current readings!
  //   string one = "-teen ";
  //   string msg = one + PRINT_STATE();
  //   Serial.write(msg.c_str());
  // };
 
  GYRO_TRANSMISSION();
};

// check for -java <- message from java!
bool check_for_java() {
  return (memcmp(headerBytes, PRE_JAVA, sizeof(PRE_JAVA)) == 0);
};

// will look at the group and assess the movements to verify it was correct!
bool CHECK_CMD_POST_MOVE(string postmove) {
  if (!mvmt.empty()) { // we have things to read!
    string checkmsg;
    for (size_t j = 0; j < mvmt.size(); j++) {
      int ms = mvmt.at(j);
      ms *= 100;
      checkmsg += to_string(ms); // add for the return message
    }
    
    // clean up
    CLEANUP();
    string one = "-teen ";
    string two = checkmsg;
    string three = one + two;
    Serial.write(three.c_str());
    return checkmsg == postmove;
  };
  // unable to check mvmt, as it was cleared!
  Serial3.println("Unable to check mvmt as it was cleared!");
  return false;
};

// should accept some array of params and family of servos to move
// WILL return true if post cmd check method returns true for the expected move verification!
bool CMD_MOVE_SERVO(Servo group) {
  if (!mvmt.empty()) { // we have things to read!
    string returnmessage;
    for (size_t j = 0; j < mvmt.size(); j++) {
      int ms = mvmt.at(j);
      ms *= 100;
      // send to move for real feedback
      delay(100);
      MoveServo(servos, ms);
      returnmessage += to_string(servos.readMicroseconds()); // add for the return message
    }
    return CHECK_CMD_POST_MOVE(returnmessage);
  }
  // unable to check mvmt, as it was cleared!
  Serial3.println("Unable to check mvmt as it was cleared!");
  return false;
};

bool CMD_SET_STATE() {
  if (!mvmt.empty()) {
    STATE_ANGLE = mvmt.at(0);
    STATE_ELEVATION = mvmt.at(1);
    STATE_POWER = mvmt.at(2);
    CLEANUP();
    return true;
  };
  Serial3.println("Unable to check mvmt as it was cleared!");
  return false;
};

string PRINT_STATE() {
  string cs = "CURRENT STATE -> ";
  string msg = cs + "Angle: " + to_string(STATE_ANGLE) + " | Elevation: " + to_string(STATE_ELEVATION) + " | Power: " + to_string(STATE_POWER) + " | Spawn Count: " + to_string(SPAWNS.size());
  return msg;
};

bool CMD_TALK(int index) {
  // get msg from buffer
  if (buffer[0] != 0) {
    int start = (sizeof(headerBytes) + index) - headerLength;
    string message;
    for (size_t j = start; j < sizeof(buffer); j++) {
      message += static_cast<char>(buffer[j]);
    }
    string one = "-teen ";
    string finalmsg = one + message;
    CLEANUP();
    Serial.write(finalmsg.c_str(), finalmsg.length());
    return true;
  }
  // buffer was emptied before able to read
  Serial3.println("Buffer was emptied before able to read!");
  return false;
};

// fill mvmt vector for commands
void FILL_MVMT_VECTOR(int index) {
  // -java1a2(3->1,2,3)
  int start = (sizeof(headerBytes) + index) - headerLength;
  string num;
  for (size_t j = start; j < sizeof(buffer); j++) {
    if (buffer[j] != 0 && buffer[j] != 44) { // it equal to comma, skip
      num.push_back(char(buffer[j])); // push to num to concat -> 6,22 -> 6 | 2 <- 2 => 22
    } else {
      if (!num.empty()) {
        mvmt.push_back(stoi(num));
        num.clear();
      }
    }
  };
};

// returns buffer to string
String CONVERT_UI8(uint8_t *arr) {
  return String((char *)arr);
};