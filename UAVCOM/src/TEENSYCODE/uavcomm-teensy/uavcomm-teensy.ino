#include <Servo.h>
Servo servos; // global var

#include <string> // stoi
// include vector for non fixed size arr
#include <vector>

using namespace std;
const size_t bufferLength = 32; // define the maximum buffer length
uint8_t buffer[bufferLength]; // create a buffer array of bytes
uint8_t actionBytes[5]; // create an array to store the action bytes
// test servo is TestServo
const uint8_t testServo[] = {32, 84, 101, 115, 116, 83, 101, 114, 118, 111};
// hello teensy is = -java Teensy, are you awake?
const uint8_t helloTeensy[] = {32, 84, 101, 101, 110, 115, 121, 44, 32, 97, 114, 101, 32, 121, 111, 117, 32, 97, 119, 97, 107, 101, 63};
const uint8_t PRE_JAVA[] = {45, 106, 97, 118, 97}; // states -java
bool commready = false; // initialize pr (comms Ready) boolean to false
bool peripheralready = false; // init periph. boolean to false

const int DEFAULT_SERVO_ANGLE = 500;
int angle = DEFAULT_SERVO_ANGLE;

// servo actions as vector
vector<int> mvmt;

void setup() {
  // put your setup code here, to run once:
  Serial.begin(115200);
  Serial3.begin(115200);
  pinMode(23, OUTPUT); // output to 23
  servos.attach(23);
}

void CLEANBUF() {
  memset(actionBytes, 0, sizeof(actionBytes));
  memset(buffer, 0, sizeof(buffer));
}

void CLEANMVMT() {
  mvmt.clear();
}

void PRINT_BUF(size_t i) {
  // loop through the stored bytes
  for (size_t j = 0; j < i-5; j++) {
    Serial3.print(buffer[j]); // print each byte to the Serial Monitor
    Serial3.print(" "); // add a space between each byte
  }
  Serial3.println(); // add a newline at the end of the printed bytes
  // print the action bytes to the Serial Monitor
  Serial3.print("Action: ");
  for (size_t j = 0; j < 5; j++) {
    Serial3.print(actionBytes[j]); // print each byte to the Serial Monitor
    Serial3.print(" "); // add a space between each byte
  }
}

void VERIFY(size_t i) {
  Serial3.println("Attempting to Verify Java message.");
  PRINT_BUF(i);
  // -java, init com to verify communcation :)
  if (memcmp(buffer, helloTeensy, sizeof(helloTeensy)) == 0) {
    Serial3.println("Verified COM w/ JAVA");
    commready = true; // set commready to true if the message is "Hello, Teensy"
    Serial.write("-teen Java, Im awake");
  }
  else if (memcmp(buffer, testServo, sizeof(testServo)) == 0) {
    Serial3.println("Got a command to test out Servos!");
    int start = (sizeof(actionBytes) + sizeof(testServo)) - 5;
    string num;
    for (size_t j = start; j < sizeof(buffer); j++) {
      if (buffer[j] != 0 && buffer[j] != 44) {
        num.push_back(char(buffer[j])); // print each byte to the Serial Monitor
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
    peripheralready = true;
    CLEANBUF();
    CLEANMVMT();
  };
}

// here, I will send to appropriate command and fulfill with the provided instruction
void TRANSLATE(size_t i) {
  Serial3.println("Translating... PR IS TRUE...");
  PRINT_BUF(i);
  CLEANBUF();
}

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
}

void MoveServo(Servo s, int ms) {
  Serial3.print("Moving Servo to ms: "); Serial3.println(to_string(ms).c_str());
  s.writeMicroseconds(ms);
}

void loop() {
  // put your main code here, to run repeatedly:
  if (Serial.available()) { // if some data is available to read
    size_t i = 0; // init pointer
    while (Serial.available() && i < bufferLength) { // loop through the incoming bytes
      if (i < 5) { // if we're still reading the action bytes
        actionBytes[i] = Serial.read(); // store the byte in the action array
      } else { // if we're reading the rest of the message
        buffer[i-5] = Serial.read(); // store the byte in the buffer array
      }
      i++; // increment the counter variable
    }

    // check to see if the message is from java "-java"
    if (check_for_java()) {
      if (commready && peripheralready) TRANSLATE(i);
      if (!commready || !peripheralready) VERIFY(i);
    }
    CLEANBUF();
  }
  delay(1000);
  if (commready) commready = false;
  if (peripheralready) peripheralready = false;
  Serial3.println("--------------------------------");
  Serial3.print("Comms Ready? " ); Serial3.println(commready);
  Serial3.println("--------------------------------");
  Serial3.print("Peripheral Ready? " ); Serial3.println(peripheralready);
  Serial3.println("--------------------------------");
}

// check for -java <- message from java!
bool check_for_java() {
  return (memcmp(actionBytes, PRE_JAVA, sizeof(PRE_JAVA)) == 0);
};