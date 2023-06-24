#include <MPU6050.h>
#include <TeensyThreads.h>  // creates global "threads" var
#include <Wire.h>
#include <sys/time.h>  // for timing functions

using namespace std;
// MPU GYRO is for regulating the angle or turn sent from java
MPU6050 mpu;

// necessary vars for MPU GYRO
int16_t accelerometer_x, accelerometer_y, accelerometer_z;  // variables for accelerometer raw data
int16_t gyro_x, gyro_y, gyro_z;                             // variables for gyro raw data
int16_t temperature;                                        // variables for temperature data

// Teensy 4.0 SW
#include <Servo.h>
Servo aileron_servos;  // global var
Servo elevator_servos;
const int MAX_SERVO_ANGLE = 170;
const int MIN_SERVO_ANGLE = 10;
const int DEFAULT_SERVO_ANGLE = 90;

#include <string>  // stoi
// include vector for non fixed size arr
#include <vector>
#include <pthread.h>  // thread for servo management during state based requests
// #include <ArduinoSTL.h>
#include <queue>  // queue data structure in the event more data is received from the Serial, and the current is not complete

const size_t bufferLength = 128;    // define the maximum buffer length
uint8_t buffer[bufferLength];       // create a buffer array of bytes
const size_t headerLength = 5;      // starts at 5, move to 1 after readys
uint8_t headerBytes[headerLength];  // create an array to store the header bytes | header bytes states -java ONLY
// test servo is TestServo
const uint8_t testServo[] = { 32, 84, 101, 115, 116, 83, 101, 114, 118, 111 };
// hello teensy is = -java Teensy, are you awake?
const uint8_t helloTeensy[] = { 32, 84, 101, 101, 110, 115, 121, 44, 32, 97, 114, 101, 32, 121, 111, 117, 32, 97, 119, 97, 107, 101, 63 };
const uint8_t PRE_JAVA[] = { 45, 106, 97, 118, 97 };  // states -java
bool commready = false;                               // initialize pr (comms Ready) boolean to false
bool peripheralcheck = false;                         // init periph. boolean to false

// aileron angle at the moment in testing!!
const int DEFAULT_AILERON_ANGLE = DEFAULT_SERVO_ANGLE;
int angle = DEFAULT_AILERON_ANGLE;

int STATE_ANGLE = 0;
int STATE_ELEVATION = 0;
int STATE_POWER = 0;
// Calculate tilt angles with offsets
const float AccErrorX = 0.58;
const float AccErrorY = -1.58;

float CURR_ROLL = 0;
float CURR_PITCH = 0;

// define shell cmd vars
const uint8_t AILERON = 97;    //a
const uint8_t ELEVATOR = 101;  //e
const uint8_t THRUST = 109;    //m
const uint8_t TALK = 116;      //t
const uint8_t STATE = 115;     //s
const uint8_t RESET = 114;     //r

// movement vectors as request by commands
vector<int> mvmt;

bool THREAD_WATCH = false;

// threads
const int MAX_THREADS = 3;
int SPAWNS[MAX_THREADS] = {0};  // Array to hold the thread objects

// Declare the argument structure to pass multiple arguments to the thread
struct ThreadArgs {
  Servo* myServo;
  int threadId;
};

// necessary functions for threads
void* Watch_My_aileron_servos(void* arg);
void* Watch_My_elevator_servos(void* arg);
void* Servo_Supervisor();
Threads aileronThread;
ThreadArgs aileronThreadArgs;
Threads elevatorThread;
ThreadArgs elevatorThreadArgs;
Threads SupervisorThread;

bool Sub_Threads_Created = false;
unsigned long lastAileronTime = 0;
unsigned long lastElevatorTime = 0;
unsigned long lastSupervisorTime = 0;
const unsigned long servoInterval = 30000;  // Adjust this interval as needed

mutex STATE_ANGLE_MUTEX;
mutex STATE_ELEVATION_MUTEX;

mutex SPAWN_ONE;
mutex SPAWN_TWO;

void setup() {
  // put your setup code here, to run once:
  Serial.begin(115200);
  Serial3.begin(115200);
  pinMode(23, OUTPUT);  // output to 23
  pinMode(22, OUTPUT);  // output to 22
  aileron_servos.attach(23);
  elevator_servos.attach(22);
  elevator_servos.write(DEFAULT_SERVO_ANGLE);
  aileron_servos.write(DEFAULT_SERVO_ANGLE);
  // Needed for MPU GYRO
  // Wire.begin();
  // Wire.beginTransmission(MPU_ADDR); // Begins a transmission to the I2C slave (GY-521 board)
  // Wire.write(0x6B); // PWR_MGMT_1 register
  // Wire.write(0); // set to zero (wakes up the MPU-6050)
  // Wire.endTransmission(true);
  // Initialize MPU6050
  Wire.begin();
  mpu.initialize();

  // Check initialization status
  if (mpu.testConnection()) {
    Serial3.println("MPU6050 initialization successful");
  } else {
    Serial3.println("MPU6050 initialization failed. Please check your connections.");
  }

  // calibrate sensor offsets
  // Calibrate MPU6050
  // Serial3.println("Calibrating MPU6050...");
  // mpu.CalibrateAccel();
  // mpu.CalibrateGyro();
  // Serial3.println("Calibration complete!");

  calculate_IMU_error();
  Serial3.println("MPU6050 is Ready!");

  // Create the thread arguments
  // !! GETSTATE meaning
  //     case 0:
  //         "EMPTY"
  //     case 1:
  //         "RUNNING"
  //     case 2:
  //         "ENDED"
  //     case 3:
  //        "ENDING"
  //     case 4:
  //        "SUSPENDED"

  SPAWNS[0] = SupervisorThread.id();
  // ThreadArgs aileronThreadArgs;
  // int ail_id = threads.addThread(Watch_My_aileron_servos, &aileronThreadArgs);
  SPAWNS[1] = aileronThread.id();  // thread count is init at 0, as the main process is 0, so we must iterate by 1 for each new thread!

  aileronThreadArgs.myServo = &aileron_servos;
  aileronThreadArgs.threadId = Thread_ID(1);  // set thread args to be the id of the first spawn!

  //ThreadArgs elevatorThreadArgs;

  // int elev_id = threads.addThread(Watch_My_elevator_servos, &elevatorThreadArgs);
  SPAWNS[2] = elevatorThread.id();  // thread count is init at 0, as the main process is 0, so we must iterate by 1 for each new thread!

  elevatorThreadArgs.myServo = &elevator_servos;
  elevatorThreadArgs.threadId = Thread_ID(2);  // set thread args to be the id of the first spawn!

  // Create the Aileron thread and add to spawns array holding the threads
  Suspend_All_Threads();

  // int Motor_Thread
};

void calculate_IMU_error() {
  // We can call this function in the setup section to calculate the accelerometer data error.
  // Place the IMU flat in order to get proper values for calibration.
  // Read accelerometer values 200 times
  int c = 0;
  float AccErrorX = 0.0;
  float AccErrorY = 0.0;

  while (c < 200) {
    mpu.getMotion6(&accelerometer_x, &accelerometer_y, &accelerometer_z, &gyro_x, &gyro_y, &gyro_z);
    // Sum all readings
    AccErrorX += atan2(accelerometer_y, sqrt(pow(accelerometer_x, 2) + pow(accelerometer_z, 2))) * (180.0 / PI);
    AccErrorY += atan2(-accelerometer_x, sqrt(pow(accelerometer_y, 2) + pow(accelerometer_z, 2))) * (180.0 / PI);
    c++;
  }

  // Divide the sum by 200 to get the error value
  AccErrorX /= 200.0;
  AccErrorY /= 200.0;

  // Print the error values
  Serial3.print("AccErrorX: ");
  Serial3.println(AccErrorX);
  Serial3.print("AccErrorY: ");
  Serial3.println(AccErrorY);
}


// get thread ID within the spawns array
int Thread_ID(int i) {
  return SPAWNS[i];
};

// get the thread status
int Thread_Status(int i) {
  return threads.getState(Thread_ID(i));
};

// restart thread! // make awake
int Thread_Restart(int i) {
  Serial3.print("RESTARTING THREAD: ");
  Serial3.println(String(Thread_ID(i)));
  return threads.restart(Thread_ID(i));
};

int Thread_Count_Running() {
  int count = 0;
  for (int i = 0; i < MAX_THREADS; i++) {
    if (Thread_Status(i) != 0) {
      if (Thread_Status(i) == 1) count++;
    };
  };
  return count;
};

// put thread in suspended state
void Thread_To_Suspend(int i) {
  int tmp = threads.suspend(Thread_ID(i));
};

void Suspend_All_Threads() {
  THREAD_WATCH = false;
  Serial3.println("---------------SUSPENDING ALL THREADS-----------------");
  for (int i = 0; i < MAX_THREADS; i++) {
    Thread_To_Suspend(i);
  };
};

// return true or false if the Supervisor thread has already created the 2 threads
bool Sub_Threads_Spawned() {
  return Sub_Threads_Created;
}

void CLEANUP() {
  CLEANBUF();
  CLEANMVMT();
}

void CLEANBUF() {
  memset(headerBytes, 0, sizeof(headerBytes));
  memset(buffer, 0, sizeof(buffer));
  Serial3.println("--------CLEANED THE BUFFER-----------");
};

void CLEANMVMT() {
  mvmt.clear();
  Serial3.println("--------CLEANED MOVEMENT VECTOR-----------");
};

void PRINT_BUF(size_t i) {
  // loop through the stored bytes
  for (size_t j = 0; j < i - headerLength; j++) {
    Serial3.print(buffer[j]);  // print each byte to the Serial Monitor
    Serial3.print(" ");        // add a space between each byte
  }
  Serial3.println();  // add a newline at the end of the printed bytes
  // print the header bytes to the Serial Monitor
  Serial3.print("Header: ");
  for (size_t j = 0; j < headerLength; j++) {
    Serial3.print(headerBytes[j]);  // print each byte to the Serial Monitor
    Serial3.print(" ");             // add a space between each byte
  }
};

void VERIFY(size_t i) {
  Serial3.println("Attempting to Verify Java message.");
  PRINT_BUF(i);
  if (memcmp(buffer, helloTeensy, sizeof(helloTeensy)) == 0) {
    Serial3.println("Verified COM w/ JAVA");
    commready = true;  // set commready to true if the message is expected one
    Serial.write("-teen Java, Im awake");
  } else if (memcmp(buffer, testServo, sizeof(testServo)) == 0) {
    int start = (sizeof(headerBytes) + sizeof(testServo)) - headerLength;
    string num;
    for (size_t j = start; j < sizeof(buffer); j++) {
      if (buffer[j] != 0 && buffer[j] != 44) {
        num.push_back(char(buffer[j]));  // push to num to concat -> 6,22 -> 6 | 2 <- 2 => 22
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
    Serial.write(three.c_str());  // write back to java for confirmation
    peripheralcheck = true;
  };
};

// char tmp_str[7]; // temporary variable used in convert function

// char* convert_int16_to_str(int16_t i) { // converts int16 to string. Moreover, resulting strings will have the same length in the debug monitor.
//   sprintf(tmp_str, "%6d", i);
//   return tmp_str;
// }

void GYRO_TRANSMISSION() {
  mpu.getMotion6(&accelerometer_x, &accelerometer_y, &accelerometer_z, &gyro_x, &gyro_y, &gyro_z);
  // temperature = mpu.getTemperature();

  // Convert accelerometer data to tilt angles
  CURR_ROLL = (atan2(accelerometer_y, sqrt(pow(accelerometer_x, 2) + pow(accelerometer_z, 2))) * (180.0 / PI)) - AccErrorX;
  CURR_PITCH = (atan2(accelerometer_x, sqrt(pow(accelerometer_y, 2) + pow(accelerometer_z, 2))) * (180.0 / PI)) + AccErrorY;
  delay(10);
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

  int index = 1;  // look for cmd at 1, as this will be after the first space!
  uint8_t cmd = buffer[index];

  switch (cmd) {
    case AILERON:
      Serial3.println("Moving AILERON(s)");
      // store instructions into mvmt vector, and then send to appropriate method
      index += 2;
      FILL_MVMT_VECTOR(index);  // fill vector with parameters
      return CMD_MOVE_SERVO(aileron_servos);
    case ELEVATOR:
      Serial3.println("Moving ELEVATORS(s)");
      // store instructions into mvmt vector, and then send to appropriate method
      index += 2;
      FILL_MVMT_VECTOR(index);  // fill vector with parameters
      return CMD_MOVE_SERVO(elevator_servos);
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
      Suspend_All_Threads();
      if (RESET_STATE()) {
        Serial3.println("--------------STATE RESET SUCCESSFULL--------------");
      } else Serial3.println("--------------FAILED TO RESET STATE--------------");
      delay(50);
      MoveServo(aileron_servos, DEFAULT_AILERON_ANGLE);
      MoveServo(elevator_servos, DEFAULT_SERVO_ANGLE);
      return true;
    default:
      return false;
  };
};

// returns a string of concat info from the location of the servo
string TestServo() {
  if (!mvmt.empty()) {  // we have things to read!
    string returnmessage;
    for (size_t j = 0; j < mvmt.size(); j++) {
      int ms = mvmt.at(j);
      // ms *= 100;
      // send to move for real feedback
      delay(200);
      MoveServo(aileron_servos, ms);
      MoveServo(elevator_servos, ms);
      returnmessage += to_string(aileron_servos.read());  // add for the return message
    }
    MoveServo(aileron_servos, DEFAULT_AILERON_ANGLE);
    MoveServo(elevator_servos, DEFAULT_SERVO_ANGLE);
    return returnmessage;
  }
  return "no mvmt";
};

void MoveServo(Servo s, int angle) {
  Serial3.print("Moving Servo to angle: ");
  Serial3.println(to_string(angle).c_str());
  s.write(angle);
};

void loop() {
  if (Serial.available()) {                           // if some data is available to read
    size_t i = 0;                                     // init pointer
    while (Serial.available() && i < bufferLength) {  // loop through the incoming bytes
      if (i < headerLength) {                         // if we're still reading the header bytes
        headerBytes[i] = Serial.read();               // store the byte in the header array
      } else {                                        // if we're reading the rest of the message
        buffer[i - headerLength] = Serial.read();     // store the byte in the buffer array
      }
      i++;  // increment the counter variable
    }

    // check to see if the message is from java "-java"
    if (check_for_java()) {
      if (commready && peripheralcheck) {
        bool attempt = TRANSLATE(i);  // if attempt is true, means, successful!
        if (attempt) Serial3.println("Successful Command!");
        else Serial3.println("Command was not succesful!");
      };
      if (!commready || !peripheralcheck) VERIFY(i);
    }
    // clean up
    CLEANUP();
  };

  if (!THREAD_WATCH) {
    delay(1000);
    Serial3.println("--------------------------------");
    Serial3.println(PRINT_STATE().c_str());
    Serial3.println("--------------------------------");
  };
};

// check for -java <- message from java!
bool check_for_java() {
  return (memcmp(headerBytes, PRE_JAVA, sizeof(PRE_JAVA)) == 0);
};

bool RESET_STATE() {
  STATE_ANGLE = 0;
  STATE_ELEVATION = 0;
  STATE_POWER = 0;
  return STATE_ANGLE == 0 && STATE_ELEVATION == 0 && STATE_POWER == 0;
};

// will look at the group and assess the movements to verify it was correct!
bool CHECK_CMD_POST_MOVE(string postmove) {
  if (!mvmt.empty()) {  // we have things to read!
    string checkmsg;
    for (size_t j = 0; j < mvmt.size(); j++) {
      int ms = mvmt.at(j);
      // ms *= 100;
      checkmsg += to_string(ms);  // add for the return message
    };

    // clean up
    CLEANUP();
    string one = "-teen ";
    string two = checkmsg;
    string three = one + two;
    Serial.write(three.c_str());
    double a = stod(checkmsg);  // convert checkmsg to integer
    double b = stod(postmove);  // convert postmove to integer
    return PERCENT_DIFF(a, b, 5);
  };
  // unable to check mvmt, as it was cleared!
  Serial3.println("Unable to check mvmt as it was cleared!");
  return false;
};

// should accept some array of params and family of aileron_servos to move
// WILL return true if post cmd check method returns true for the expected move verification!
bool CMD_MOVE_SERVO(Servo s) {
  if (!mvmt.empty()) {  // we have things to read!
    string returnmessage;
    for (size_t j = 0; j < mvmt.size(); j++) {
      int ms = mvmt.at(j);
      // ms *= 100;
      // send to move for real feedback
      delay(100);
      MoveServo(s, ms);
      returnmessage += to_string(s.read());  // add for the return message
    }
    return CHECK_CMD_POST_MOVE(returnmessage);
  }
  // unable to check mvmt, as it was cleared!
  Serial3.println("Unable to check mvmt as it was cleared!");
  return false;
};

// method for threads to adjust aileron_servos to manage the current angles
void* Watch_My_aileron_servos(void* arg) {
  struct ThreadArgs* threadArgs = (struct ThreadArgs*)arg;
  Servo* servo_to_adjust = threadArgs->myServo;

  while (true) {
    unsigned long currentTime = micros();
    if (currentTime - lastAileronTime >= servoInterval) {
      lastAileronTime = currentTime;

      // is the state resource are unlocked, go ahead and use in calc
      if (STATE_ANGLE_MUTEX.try_lock()) {
        int c_ms = servo_to_adjust->read();
        if (CURR_ROLL < STATE_ANGLE) {
          // angle is less, we need to move up!
          c_ms += 5;
        } else if (CURR_ROLL > STATE_ANGLE) {
          c_ms -= 5;
        }
        if (c_ms >= MIN_SERVO_ANGLE && c_ms <= MAX_SERVO_ANGLE) servo_to_adjust->write(c_ms);

        // since aileron could access both, now unlock for other thread
        STATE_ANGLE_MUTEX.unlock();
      } else {
        // At least one mutex is locked, skip accessing the shared resources
        // Sleep for a short duration to avoid unnecessary servo movement
        delay(10);
      };
    }
  };
  return NULL;
};

// method for threads to adjust aileron_servos to manage the current angles
void* Watch_My_elevator_servos(void* arg) {
  struct ThreadArgs* threadArgs = (struct ThreadArgs*)arg;
  Servo* servo_to_adjust = threadArgs->myServo;

  while (true) {
    unsigned long currentTime = micros();
    // if enough time has passed to begun trying to adjust
    if (currentTime - lastElevatorTime >= servoInterval) {
      lastElevatorTime = currentTime;

      // is the state resource are unlocked, go ahead and use in calc
      if (STATE_ELEVATION_MUTEX.try_lock()) {
        int c_ms = servo_to_adjust->read();
        if (CURR_PITCH < STATE_ELEVATION) {
          // angle is less, we need to move up!
          c_ms += 5;
        } else if (CURR_PITCH > STATE_ELEVATION) {
          c_ms -= 5;
        }
        if (c_ms >= MIN_SERVO_ANGLE && c_ms <= MAX_SERVO_ANGLE) servo_to_adjust->write(c_ms);

        // since elevator could access both, now unlock for other thread
        STATE_ELEVATION_MUTEX.unlock();
      } else {
        // At least one mutex is locked, skip accessing the shared resources
        // Sleep for a short duration to avoid unnecessary servo movement
        delay(10);
      };
    }
  };
  return NULL;
};

void Servo_Supervisor(void* arg) {
  // lock spawns resources, Supervisor is in control now
  SPAWN_ONE.lock();
  SPAWN_TWO.lock();

  // now, add threads to the process, but suspend them
  if (!Sub_Threads_Spawned()) {
    int ail_id = threads.addThread(Watch_My_aileron_servos, &aileronThreadArgs);
    SPAWNS[1] = ail_id;
    Thread_To_Suspend(1);

    int ele_id = threads.addThread(Watch_My_elevator_servos, &elevatorThreadArgs);
    SPAWNS[2] = ele_id;
    Thread_To_Suspend(2);

    Sub_Threads_Created = true;
  };

  // unlock spawns
  SPAWN_ONE.unlock();
  SPAWN_TWO.unlock();

  bool aileronRestarted = false;
  bool elevatorRestarted = false;

  while (THREAD_WATCH) {
    unsigned long currentTime = micros();
    GYRO_TRANSMISSION();

    if (currentTime - lastSupervisorTime >= servoInterval) {
      lastSupervisorTime = currentTime;

      bool roll_diff = false;
      bool pitch_diff = false;

      // lock state resources for checking here
      if (STATE_ANGLE_MUTEX.try_lock() && STATE_ELEVATION_MUTEX.try_lock()) {
        
        roll_diff = (abs(STATE_ANGLE - CURR_ROLL) > 10);
        pitch_diff = (abs(STATE_ELEVATION - CURR_PITCH) > 10);
        
        // unlock resources to access
        STATE_ELEVATION_MUTEX.unlock();
        STATE_ANGLE_MUTEX.unlock();
      };

      // lock spawns resources, Supervisor is in control now
      if (SPAWN_ONE.try_lock() && SPAWN_TWO.try_lock()) {

        // if our diff is off, we need to start the thread
        if (Thread_Status(1) == 4 && roll_diff && !aileronRestarted) {
          aileronRestarted = true;
          threads.restart(SPAWNS[1]);
        };

        if (Thread_Status(2) == 4 && pitch_diff && !elevatorRestarted) {
          elevatorRestarted = true;
          threads.restart(SPAWNS[2]);
        };

        if (!roll_diff) {
          aileronRestarted = false;
          Thread_To_Suspend(1);
        };

        if (!pitch_diff) {
          elevatorRestarted = false;
          Thread_To_Suspend(2);
        };
      };

      // unlock spawns
      SPAWN_ONE.unlock();
      SPAWN_TWO.unlock();
    };
  };
  return NULL;
};

//     case 0:
//         "EMPTY"
//     case 1:
//         "RUNNING"
//     case 2:
//         "ENDED"
//     case 3:
//        "ENDING"
//     case 4:
//        "SUSPENDED"

// mutex declarations
// mutex - SPAWN_ONE;
// nutex - SPAWN_TWO;
// mutex - STATE_ANGLE_MUTEX;
// mutex - STATE_ELEVATION_MUTEX;
bool CMD_SET_STATE() {
  if (!mvmt.empty()) {
    // lock threads from accessing until completed with assingnment
    STATE_ANGLE_MUTEX.lock();
    STATE_ELEVATION_MUTEX.lock();

    STATE_ANGLE = mvmt.at(0);
    STATE_ELEVATION = mvmt.at(1);
    STATE_POWER = mvmt.at(2);

    // unlock variables now
    STATE_ELEVATION_MUTEX.unlock();
    STATE_ANGLE_MUTEX.unlock();

    // Start the servo supervisor thread if it is not running
    if (!THREAD_WATCH) {
      int s_state = threads.getState(SPAWNS[0]);
      if (s_state != 1) {     // Supervisor is not running
        THREAD_WATCH = true;  // set thread watch to true
        int sup_id = threads.addThread(Servo_Supervisor, nullptr);
        SPAWNS[0] = sup_id;
      }
    };

    return THREAD_WATCH;
  };
  Serial3.println("Unable to check mvmt as it was cleared!");
  return false;
};

string PRINT_STATE() {
  string cs = "CURRENT STATE -> ";
  int running = Thread_Count_Running();
  int suspend = 3 - running;
  string msg = cs + "Angle: " + to_string(STATE_ANGLE) + " | Elevation: " + to_string(STATE_ELEVATION) + " | Power: " + to_string(STATE_POWER) + " | Spawn(s) Active: " + to_string(running) + " | Spawn(s) Suspended: " + to_string(suspend);
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
    if (buffer[j] != 0 && buffer[j] != 44) {  // if equal to comma, skip
      num.push_back(char(buffer[j]));  // push to num to concat -> 6,22 -> 6 | 2 <- 2 => 22
    } else {
      if (!num.empty()) {
        mvmt.push_back(stoi(num));
        num.clear();
      }
    }
  };
};

// returns buffer to string
String CONVERT_UI8(uint8_t* arr) {
  return String((char*)arr);
};

// c is the threshold for percent difference
bool PERCENT_DIFF(double a, double b, int c) {
  double pd = (abs(a - b) / max(a, b)) * 100;
  Serial3.print("PERCENT DIFF: ");
  Serial3.println(to_string(pd).c_str());
  return pd <= c;
};