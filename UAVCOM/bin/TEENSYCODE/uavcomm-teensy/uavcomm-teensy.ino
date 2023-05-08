const size_t bufferLength = 32; // define the maximum buffer length
uint8_t buffer[bufferLength]; // create a buffer array of bytes
uint8_t actionBytes[5]; // create an array to store the action bytes
// hello teensy is = -java Teensy, are you awake?
const uint8_t helloTeensy[] = {32, 84, 101, 101, 110, 115, 121, 44, 32, 97, 114, 101, 32, 121, 111, 117, 32, 97, 119, 97, 107, 101, 63};
const uint8_t PRE_JAVA[] = {45, 106, 97, 118, 97}; // states -java
bool programready = false; // initialize pr (Program Ready) boolean to false

void setup() {
  // put your setup code here, to run once:
  Serial.begin(115200);
  Serial3.begin(115200);
}

void CLEANBUF() {
  memset(actionBytes, 0, sizeof(actionBytes));
  memset(buffer, 0, sizeof(buffer));
}

void VERIFY(size_t i) {
  Serial3.println("Got a msg from Java!");
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

  // -java, init com to verify communcation :)
  if (memcmp(buffer, helloTeensy, sizeof(helloTeensy)) == 0) {
    Serial3.println("Verified COM w/ JAVA");
    programready = true; // set programReady to true if the message is "Hello, Teensy"
    Serial.write("-teen Java, Im awake");
  }
  CLEANBUF();
}

void TRANSLATE(size_t i) {
  Serial.println("Translating... PR IS TRUE...");
  // loop through the stored bytes
  for (size_t j = 0; j < i-5; j++) {
    Serial.print(buffer[j]); // print each byte to the Serial Monitor
    Serial.print(" "); // add a space between each byte
  }
  Serial.println(); // add a newline at the end of the printed bytes
  // print the action bytes to the Serial Monitor
  Serial.print("Action: ");
  for (size_t j = 0; j < 5; j++) {
    Serial.print(actionBytes[j]); // print each byte to the Serial Monitor
    Serial.print(" "); // add a space between each byte
  }
  CLEANBUF();
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
    // this is a message from java, don't look at teensy!
    if (memcmp(actionBytes, PRE_JAVA, sizeof(PRE_JAVA)) == 0) {
      if (programready) TRANSLATE(i);
      if (!programready) VERIFY(i);
    }
    
    // clear the buffer array
    CLEANBUF();
  }
  delay(500);
  Serial3.println("Teensy is bored...");
}