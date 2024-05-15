#include <Preferences.h>
#include <random>
#include <string>
//base
#include <pitch.h>
#include <Arduino.h>
#include <WiFi.h>
#include <FirebaseClient.h>
//wifi
#define WIFI_SSID "flexbox"
#define WIFI_PASSWORD "123456789"
//fingerprint
#include <Adafruit_Fingerprint.h>
#define FINGERPRINT_SENSOR_BAUDRATE 57600
HardwareSerial mySerial(2);
Adafruit_Fingerprint finger = Adafruit_Fingerprint(&mySerial);
//firebase
#define USER_EMAIL "hawari.yassine.yh@gmail.com"
#define USER_PASSWORD "123456789"
#define API_KEY "AIzaSyBbYnSb_vUoj29uWPXxzoSWKB7smP5Vu6s"
#define DATABASE_URL "https://office-f66f6-default-rtdb.firebaseio.com/"

#define FIREBASE_PROJECT_ID "office-f66f6"
#define FIREBASE_CLIENT_EMAIL "firebase-adminsdk-e3jhu@office-f66f6.iam.gserviceaccount.com"
const char PRIVATE_KEY[] PROGMEM ="-----BEGIN PRIVATE KEY-----\nMIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCMJXTVr0YyuD4q\ncC/nPU+OZIagn4lMMaLKVeMC4DC0Ui5uzgkbufGsEk6gs9iyNEk5Iq3BJE0rl9fj\nfQAId/rqusCsF9Ykk3yUaCEhyPX/yr84gyO/uAyIEg2hR9MysBAmxBc6KPerql1G\nuui1D06HcVh7IlvwvdNlcwt3CQXS4yRTJOD1fu/6EJ6wK7Y1a9jRlGVW8VErpQKd\nsVtbhVb00MrsmrRJyvABOPm07KnYc7TkLwa7LWLA+r4dwYqV2GuGxLosqXmhIKNC\n3/TKW6FoHs4NYOuGg9TChcTKfbR24Anb0XLXKXTRn5YDip+87fNi158EIixnUdss\nvZnWIgdvAgMBAAECggEAJTM8FAx2YbguKC2nSYYou+9LHQ+77hGu2G5716OSygqW\ncKsYF9f/omEppdaXSpY+aYAfBwmPwmaH08a2X4kZfX0c75FUMllD1FDfmowxYDh9\nEY8dyiIckXHZVFowYWACqYebcsC1HUgXglLSulzZx3H6vpScOwboOiGThYFLnnJv\n7xdB9CQaVI5eQsQ6RJCDoN77sxCvEPoDwh00epBEBpJrH4TQfNhaZqXG+/CTM4Rv\nEOotR5chqarKRb3yciLoE+JwwPK0B5Fjk/Z9w8MsrN5grdJPbuaRhw5XrCH/XSki\ni9VdXgRKhAmLGX329I7Z/7fjQPDRvqsOixNRG0h8QQKBgQDESuXnxwK+fpzdMYLU\n09fXZJ1JXcLQt9G8eqr6YARq5tyRXJIknu8+X1zl7m+0vDyB4aOCSCEzEi6kQC6U\nPw/q5IQEyc6CQHT76PT+Qrcj+RoK1bioLfofrFfQDgmkwz1SqaOHNVVrRnzwSMVM\nS2+dHNORbQFOcFZSmdQybruF5wKBgQC2xoG1u2808g2VzQex++D/Oi+cRFhwxSja\nAGYNwILEg9KlufITNEsifU1s7d/rsMtDacY7QVw5FtzCrhFIKKw3EksAF/z5suH/\nc280ihnbys07HuPuzJamsywIPhs+KGomycF5nxibKyCKfVwhE9BqMMwHkvKtlgWF\nxqA7sEAxOQKBgCL+AxwdcxTId2hLIjqUhT7FlfB1QXx5uG70IzS9FyyDeUCEkxzq\nJFYnI+VtawZ6JAM3WTGpcBmtnRj0xBjzYQALuqkr3J+FpBUgQ0RsWj+UcZsK3H9G\nqvcofwL87aUslOr+iMNas56LE0y2fE2MiRomAGVFXF1CU3EoMRbIfK9zAoGAcIr2\nn5JUPYO1/cGLtXpk5oNLgatL0dR0/Rc1v84EN0D8WfOPLWgLw6boH+Sw9o2b+1yM\nJTxvru9266EHHyl8MUTgTaR3rsEJC6RmmzFWDtJnYLn6m7X6JZc5y7GCUNnN0yh0\nK5FdM15w4RWjvvJWaKy3VQqUx3IS4HaXf2ldfXECgYAVCneqH2iELAIO+tSNV6ac\n9oHaDyGzQJSvNxTK81p1+GScBOKI2otcP79j4ZA1CralR45GuJeRs1zepNIxDqrT\noF4rcLjU9BjZix6btaw55OrUN0g61V87YF+qG49tHpFW1lTPbg3hM3v8HDeHiZNn\nDY6iJqYBmInTLZr9Fc8VRw==\n-----END PRIVATE KEY-----\n";

//time
#include <NTPClient.h>
#include <WiFiUdp.h>
WiFiUDP ntpUDP;
NTPClient timeClient(ntpUDP, "europe.pool.ntp.org", 3600, 60000);
//sensors
#include <DHT.h>
#include <DallasTemperature.h>
#define LIGHT1_PIN 21 
#define LIGHT2_PIN 22 
#define LIGHT3_PIN 23
#define BUZZER_PIN 32
#define VOICE_PIN 33
#define DHTPIN 25        
#define DHTTYPE DHT11      
DHT dht(DHTPIN, DHTTYPE);
#define PIR_PIN 19 
#define LDR_PIN 34
#define TEMPERATURE_THRESHOLD 30.0 
#define LDR_THRESHOLD 30
//variable
unsigned long lockDownprevMillis = 0;
bool isInLockDownMode = false;
bool buttonLockdown = false;
bool timeLockdown = false;
bool dirty = false;
String token="dCboOKdLSyGergtHaTsvkr:APA91bEYQs6Ng1FJx2HRI_qm8PDlDafzKiDI7oZG6EILdR3u6e777BczKLfjBhr_7uuO5W2NzzObNCX9Wa_ojHid2q45quAjrG61zQefFHDjDff6rU8PDBx98VGXalT_AwHLonkRUkXM";
String addName = "";
int removeId = 0;
bool taskCompleted = false;
// fonctions
uint8_t getFingerprintID();
void updateFirebase(uint8_t fingerID, String time);
void playWarningTone();
void playFailureTone();
void playSuccessTone();
void playWelcomeMelody();
void lockDownMode();
uint8_t getFingerprintEnroll(int8_t id);
int addWorker(String workerName);
bool removeWorker(int id);
void playNotificationTone();
void playChampionsLeagueAnthem();
void asyncCB(AsyncResult &aResult);
void printResult(AsyncResult &aResult);
void timeStatusCB(uint32_t &ts);
void sendMessage(float temp,int ldrValue,bool pir);
uint8_t deleteFingerprint(uint8_t id);

ServiceAuth sa_auth(timeStatusCB, FIREBASE_CLIENT_EMAIL, FIREBASE_PROJECT_ID, PRIVATE_KEY, 3000 /* expire period in seconds (<= 3600) */);

DefaultNetwork network(true);

UserAuth user_auth(API_KEY, USER_EMAIL, USER_PASSWORD);

FirebaseApp app;

WiFiClient basic_client1, basic_client2, basic_client3,basic_client4,basic_client5;

//espClient
ESP_SSLClient ssl_client1, ssl_client2, ssl_client3,ssl_client4,ssl_client5;

using AsyncClient = AsyncClientClass;

AsyncClient aClient1(ssl_client1, getNetwork(network));

AsyncClient aClient2(ssl_client2, getNetwork(network));

AsyncClient aClient3(ssl_client3, getNetwork(network));

AsyncClient aClient4(ssl_client4, getNetwork(network));

AsyncClient aClient5(ssl_client5, getNetwork(network));

RealtimeDatabase Database;

Messaging messaging;

AsyncResult aResult_no_callback;

unsigned long ms = 0;

void setup()
{
  Serial.begin(115200);
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);

  Serial.print("Connecting to Wi-Fi");
  unsigned long ms = millis();
  while (WiFi.status() != WL_CONNECTED)
  {
    Serial.print(".");
    delay(300);
    }

    Serial.println();
    Serial.print("Connected with IP: ");
    Serial.println(WiFi.localIP());
    Serial.println();

    Firebase.printf("Firebase Client v%s\n", FIREBASE_CLIENT_VERSION);

    Serial.println("Initializing app...");

    ssl_client1.setClient(&basic_client1);
    ssl_client2.setClient(&basic_client2);
    ssl_client3.setClient(&basic_client3);
    ssl_client4.setClient(&basic_client4);
    ssl_client5.setClient(&basic_client5);

    ssl_client1.setInsecure();
    ssl_client2.setInsecure();
    ssl_client3.setInsecure();
    ssl_client4.setInsecure();
    ssl_client5.setInsecure();

    ssl_client1.setBufferSizes(2048, 1024);
    ssl_client2.setBufferSizes(2048, 1024);
    ssl_client3.setBufferSizes(2048, 1024);
    ssl_client4.setBufferSizes(2048, 1024);
    ssl_client5.setBufferSizes(2048, 1024);

    ssl_client1.setDebugLevel(1);
    ssl_client2.setDebugLevel(1);
    ssl_client3.setDebugLevel(1);
    ssl_client4.setDebugLevel(1);
    ssl_client5.setDebugLevel(1);

    app.setCallback(asyncCB);

    initializeApp(aClient3, app, getAuth(user_auth));
    //sensor declaration
    pinMode(LIGHT1_PIN, OUTPUT);
    pinMode(LIGHT2_PIN, OUTPUT);
    pinMode(LIGHT3_PIN, OUTPUT);
    pinMode(BUZZER_PIN, OUTPUT);
    dht.begin();
    pinMode(VOICE_PIN, INPUT);
    pinMode(PIR_PIN, INPUT);
    pinMode(LDR_PIN, INPUT);

    ms = millis();
    while (app.isInitialized() && !app.ready() && millis() - ms < 120 * 1000)
    {
       JWT.loop(app.getAuth());
    }

    app.getApp<RealtimeDatabase>(Database);

    app.getApp<Messaging>(messaging);

    Database.url(DATABASE_URL);

    //firebase Streams

    //Database.get(aClient1, "/test/stream/path1", asyncCB, true /* SSE mode (HTTP Streaming) */, "streamTask1");

    Database.get(aClient2, "/smartOffice/finger", asyncCB, true /* SSE mode (HTTP Streaming) */, "streamTask2");
    Database.get(aClient4, "/smartOffice/lights", asyncCB, true /* SSE mode (HTTP Streaming) */, "streamTask4");
    Database.get(aClient5, "/smartOffice/lockDown/value", asyncCB, true /* SSE mode (HTTP Streaming) */, "streamTask5");

    //init time
    timeClient.begin();

    //finger init
    finger.begin(FINGERPRINT_SENSOR_BAUDRATE);
    if (finger.verifyPassword()) {
        Serial.println("Found fingerprint sensor!");
    } else {
        Serial.println("Did not find fingerprint sensor :(");
    }
    playWelcomeMelody();
}

void loop()
{
    app.loop();

    JWT.loop(app.getAuth());

    Database.loop();

    timeClient.update();

    messaging.loop();

    if(addName!=""){
      Serial.println("adding");
      int id = addWorker(addName);
      Preferences nfs;
      bool stat=nfs.begin("workerData", false);
      if(stat)
        Serial.println("succes begin");
      else
        Serial.println("failed begin");

        bool adding =nfs.putString(String(id).c_str(),addName.c_str());
        if(adding)
          Serial.println("succes adding");
          else
            Serial.println("failed adding");
          String idP = nfs.getString(String(id).c_str());
          Serial.println(idP);
          nfs.end();
          bool status = Database.set<int>(aClient3, "/smartOffice/finger/add/id", id);
          if (status)
          {
            Serial.println("Set id un rtdb is ok");
            addName = "";
          }
          else
            Serial.println(aClient3.lastError().message());
          }
    if(removeId!=0){
      bool r=removeWorker(removeId);
      if(r){
        bool status = Database.set<int>(aClient3, "/smartOffice/finger/remove/id",0);
        if (status){
          Preferences preferences;
          preferences.begin("workerData", false);
          preferences.remove(String(removeId).c_str());
          preferences.end();
          Serial.println("delete name is ok");
          deleteFingerprint(removeId);
          removeId=0;
          playNotificationTone();
          Database.set<bool>(aClient3, "/smartOffice/finger/remove/ok",true);
        }
        else
          Serial.println(aClient3.lastError().message());
      }
    else
      Serial.println("error deleting id from fingerprint");
    }
    if(buttonLockdown){
      lockDownMode();
    }
    else{
      uint8_t p =FINGERPRINT_NOFINGER ;
      int limite = 14;
      int current = 0;
      while (p==FINGERPRINT_NOFINGER && current < limite){
        p = getFingerprintID();
        current++;
      }
      if (millis() - ms > 5000 && app.ready())
    {
        ms = millis();

        float temp = dht.readTemperature();

        int rawValue = analogRead(LDR_PIN);
        int scaledValue = map(rawValue, 0, 4095, 0, 100);
        int ldrValue = 100 - scaledValue;
        String pir = String((digitalRead(PIR_PIN) == 1) ? 1 : 0);
        Serial.print(temp);
        Serial.println("degree");
        Serial.print(ldrValue );
        Serial.println("light");
        Serial.println(pir);

        JsonWriter writer;

        object_t json, obj1, obj2,obj3,obj4;

        writer.create(obj1, "ldrSensor/value", String(ldrValue));
        writer.create(obj2, "pirSensor/value", pir);
        writer.create(obj3, "temperatureSensor/value", String(temp));
        writer.create(obj4, "time/value", timeClient.getFormattedTime());
        writer.join(json, 4, obj1, obj2,obj3,obj4);

        bool status = Database.set<object_t>(aClient1, "/smartOffice/sensors", json);
        if (status)
          Serial.println("Set values is ok");
        else
          Serial.println(aClient3.lastError().message());
    }
    }
}
void asyncCB(AsyncResult &aResult)
{
    printResult(aResult);
}

void printResult(AsyncResult &aResult)
{
    if (aResult.appEvent().code() > 0)
    {
        Firebase.printf("Event task: %s, msg: %s, code: %d\n", aResult.uid().c_str(), aResult.appEvent().message().c_str(), aResult.appEvent().code());
    }
    if (aResult.isDebug())
    {
        Firebase.printf("Debug task: %s, msg: %s\n", aResult.uid().c_str(), aResult.debug().c_str());
    }

    if (aResult.isError())
    {
        Firebase.printf("Error task: %s, msg: %s, code: %d\n", aResult.uid().c_str(), aResult.error().message().c_str(), aResult.error().code());
    }

    if (aResult.available())
    {
        RealtimeDatabaseResult &RTDB = aResult.to<RealtimeDatabaseResult>();
        if (RTDB.isStream())
        {
          if (aResult.uid() == "streamTask2"){
            if(RTDB.dataPath()=="/add/name"){
              Serial.println("callbakc adding");
              String workerName= RTDB.to<String>();
              addName = workerName;
            }
              if(RTDB.dataPath()=="/remove/id"){
                Serial.println("call back removing");
                int workerId = RTDB.to<int>();
                removeId = workerId;
            }
          }
          else if (aResult.uid() == "streamTask4"){
            if (RTDB.dataPath() == "/light1/status") {
              digitalWrite(LIGHT1_PIN, RTDB.to<bool>() ? HIGH : LOW);
            } else if (RTDB.dataPath() == "/light2/status") {
              digitalWrite(LIGHT2_PIN, RTDB.to<bool>() ? HIGH : LOW);
            } else if (RTDB.dataPath() == "/light3/status") {
              digitalWrite(LIGHT3_PIN, RTDB.to<bool>() ? HIGH : LOW);
            }
          }
          else if (aResult.uid() == "streamTask5"){
              if (RTDB.to<bool>()) {
                dirty= true;
                Serial.println("entering lockdown...");
                buttonLockdown = true;
                } else {
                  //Database.set<bool>(setData, "/smartOffice/lockDown/warning",false, asyncCB, "unableWarnning");
                  Serial.println("exiting lockdown mode");
                  buttonLockdown = false;
                }
          }
            Serial.println("----------------------------");
            Firebase.printf("task: %s\n", aResult.uid().c_str());
            Firebase.printf("event: %s\n", RTDB.event().c_str());
            Firebase.printf("path: %s\n", RTDB.dataPath().c_str());
            Firebase.printf("data: %s\n", RTDB.to<const char *>());
            Firebase.printf("type: %d\n", RTDB.type());
        }
        else
        {
            Serial.println("----------------------------");
            Firebase.printf("task: %s, payload: %s\n", aResult.uid().c_str(), aResult.c_str());
        }
        Firebase.printf("Free Heap: %d\n", ESP.getFreeHeap());
    }
}

uint8_t getFingerprintID() {
  uint8_t p = finger.getImage();
  switch (p) {
    case FINGERPRINT_OK:
      Serial.println("Image taken");
      break;
    case FINGERPRINT_NOFINGER:
      Serial.println("No finger detected");
      return p;
    case FINGERPRINT_PACKETRECIEVEERR:
      Serial.println("Communication error");
      return p;
    case FINGERPRINT_IMAGEFAIL:
      Serial.println("Imaging error");
      return p;
    default:
      Serial.println("Unknown error");
      return p;
  }
  p = finger.image2Tz();
  switch (p) {
    case FINGERPRINT_OK:
      Serial.println("Image converted");
      break;
    default:
      Serial.println("Unknown error");
      return p;
  }
  p = finger.fingerFastSearch();
  if (p == FINGERPRINT_OK) {
    Serial.print("Found a fingerprint match!");
    Serial.println(finger.fingerID);
    uint8_t fingerID = finger.fingerID;
    Serial.print("found ") ;
    Serial.print(fingerID);
    updateFirebase(fingerID,timeClient.getFormattedTime() );
    playSuccessTone();
  } else {
    Serial.println("No match found");
    playFailureTone();
  } 
  return finger.fingerID;
}

void updateFirebase(uint8_t fingerID, String time) {
    Serial.println("Updating Firebase...");
    String workerPath = "/smartOffice/workers/worker"+String(fingerID);
    Serial.print("Geting Worker... ");

    String workerTimeIn = Database.get<String>(aClient1,workerPath+"/timeIn");
    if (aClient1.lastError().code() == 0){
        if(workerTimeIn==""){
            bool status = Database.set<String>(aClient1, workerPath+"/timeIn", time);
            if (status)
                Serial.println("Set timeIn is ok");
            else
                Serial.println("error set timeIn");
                Serial.println(String(aClient1.lastError().code())+"," +aClient1.lastError().message());
        }
        else{
            bool status = Database.set<String>(aClient1, workerPath+"/timeOut", time);
            if (status)
                Serial.println("Set timeOut is ok");
            else{
                Serial.println("error set timeOut");
                Serial.println(String(aClient1.lastError().code())+"," +aClient1.lastError().message());
            }
        }
    }
    else
      Serial.println(String(aClient1.lastError().code())+"," +aClient1.lastError().message());
}

bool removeWorker(int workerId) {
  Serial.println("removing!");

  uint8_t id = static_cast<uint8_t>(workerId);
  // Call deleteFingerprint with uint8_t parameter
  uint8_t p=deleteFingerprint(id);
  if(p==FINGERPRINT_OK)
    return true;
  else
    return false;
}

uint8_t deleteFingerprint(uint8_t id) {
  uint8_t p = -1;
  p = finger.deleteModel(id);

  if (p == FINGERPRINT_OK) {
    Serial.println("Deleted!");
  } else if (p == FINGERPRINT_PACKETRECIEVEERR) {
    Serial.println("Communication error");
  } else if (p == FINGERPRINT_BADLOCATION) {
    Serial.println("Could not delete in that location");
  } else if (p == FINGERPRINT_FLASHERR) {
    Serial.println("Error writing to flash");
  } else {
    Serial.print("Unknown error: 0x"); Serial.println(p, HEX);
  }
  return p;
}

int addWorker(String workerName) {
  Serial.println("adding!");
  std::random_device rd;
  std::mt19937 gen(rd());
  std::uniform_int_distribution<> dis(1, 99);
  int randomNum;
  Preferences preferences;
  preferences.begin("workerData", false);
  do
  {
    Serial.println(String(randomNum).c_str());
    randomNum = dis(gen); 
    } while (preferences.isKey(String(randomNum).c_str())); 
    while (!getFingerprintEnroll(randomNum));
    return randomNum;
}

uint8_t getFingerprintEnroll(int8_t id) {
  Serial.println("waiting for an fingerprint");
  playNotificationTone();
  int p = -1;
  Serial.print("Waiting for valid finger to enroll as #"); Serial.println(id);
  while (p != FINGERPRINT_OK) {
    p = finger.getImage();
    switch (p) {
    case FINGERPRINT_OK:
      Serial.println("Image taken");
      break;
    case FINGERPRINT_NOFINGER:
      Serial.print(".");
      break;
    case FINGERPRINT_PACKETRECIEVEERR:
      Serial.println("Communication error");
      break;
    case FINGERPRINT_IMAGEFAIL:
      Serial.println("Imaging error");
      break;
    default:
      Serial.println("Unknown error");
      break;
    }
  }

  // OK success!

  p = finger.image2Tz(1);
  switch (p) {
    case FINGERPRINT_OK:
      Serial.println("Image converted");
      break;
    case FINGERPRINT_IMAGEMESS:
      Serial.println("Image too messy");
      return p;
    case FINGERPRINT_PACKETRECIEVEERR:
      Serial.println("Communication error");
      return p;
    case FINGERPRINT_FEATUREFAIL:
      Serial.println("Could not find fingerprint features");
      return p;
    case FINGERPRINT_INVALIDIMAGE:
      Serial.println("Could not find fingerprint features");
      return p;
    default:
      Serial.println("Unknown error");
      return p;
  }

  Serial.println("Remove finger");
  delay(2000);
  p = 0;
  while (p != FINGERPRINT_NOFINGER) {
    p = finger.getImage();
  }
  Serial.print("ID "); Serial.println(id);
  p = -1;
  Serial.println("Place same finger again");
  while (p != FINGERPRINT_OK) {
    p = finger.getImage();
    switch (p) {
    case FINGERPRINT_OK:
      Serial.println("Image taken");
      break;
    case FINGERPRINT_NOFINGER:
      Serial.print(".");
      break;
    case FINGERPRINT_PACKETRECIEVEERR:
      Serial.println("Communication error");
      break;
    case FINGERPRINT_IMAGEFAIL:
      Serial.println("Imaging error");
      break;
    default:
      Serial.println("Unknown error");
      break;
    }
  }

  // OK success!

  p = finger.image2Tz(2);
  switch (p) {
    case FINGERPRINT_OK:
      Serial.println("Image converted");
      break;
    case FINGERPRINT_IMAGEMESS:
      Serial.println("Image too messy");
      return p;
    case FINGERPRINT_PACKETRECIEVEERR:
      Serial.println("Communication error");
      return p;
    case FINGERPRINT_FEATUREFAIL:
      Serial.println("Could not find fingerprint features");
      return p;
    case FINGERPRINT_INVALIDIMAGE:
      Serial.println("Could not find fingerprint features");
      return p;
    default:
      Serial.println("Unknown error");
      return p;
  }

  // OK converted!
  Serial.print("Creating model for #");  Serial.println(id);

  p = finger.createModel();
  if (p == FINGERPRINT_OK) {
    Serial.println("Prints matched!");
  } else if (p == FINGERPRINT_PACKETRECIEVEERR) {
    Serial.println("Communication error");
    return p;
  } else if (p == FINGERPRINT_ENROLLMISMATCH) {
    Serial.println("Fingerprints did not match");
    return p;
  } else {
    Serial.println("Unknown error");
    return p;
  }

  Serial.print("ID "); Serial.println(id);
  p = finger.storeModel(id);
  if (p == FINGERPRINT_OK) {
    Serial.println("Stored!");
  } else if (p == FINGERPRINT_PACKETRECIEVEERR) {
    Serial.println("Communication error");
    return p;
  } else if (p == FINGERPRINT_BADLOCATION) {
    Serial.println("Could not store in that location");
    return p;
  } else if (p == FINGERPRINT_FLASHERR) {
    Serial.println("Error writing to flash");
    return p;
  } else {
    Serial.println("Unknown error");
    return p;
  }

  return true;
}

void lockDownMode() {
    if (millis() - lockDownprevMillis > 3000) {
      lockDownprevMillis = millis();
      float temp = dht.readTemperature();
      int rawValue = analogRead(LDR_PIN);
      int scaledValue = map(rawValue, 0, 4095, 0, 100);
      int ldrValue = 100 - scaledValue;
      bool pir = digitalRead(PIR_PIN) == 1;

      if (pir || temp > TEMPERATURE_THRESHOLD || ldrValue > LDR_THRESHOLD) {
        //Database.set<object_t>(setData, "/smartOffice/lockDown/warning",true, asyncCB, "unableWarnning");
        playWarningTone();
        sendMessage(temp,ldrValue,pir);
      }
    else{
        //Database.set<object_t>(setData, "/smartOffice/lockDown/warning",false, asyncCB, "disableWarnning");
        Serial.println("everything ok!");
      }
    }
  }

  void sendMessage(float temp, int ldrValue, bool pir)
  {
    Serial.print("Send Firebase Cloud Messaging... ");

    Messages::Message msg;
    msg.topic("Warrning");
    msg.token(token);

    Messages::Notification notification;

    String body = "";

    if(temp>TEMPERATURE_THRESHOLD)
      body = body + "There is too much heat in the office,Check it out!\n";
    if(ldrValue>LDR_THRESHOLD)
      body = body + "There is too much light in your office,Check it out!\n";
    if(pir)
      body = body + "There is a movement in your office,Check it out!\n";

    notification.title("Warning!").body(body);

    // Library does not provide JSON parser library, the following JSON writer class will be used with
    // object_t for simple demonstration.

    /*Messages::AndroidConfig androidConfig;

    androidConfig.priority(Messages::AndroidMessagePriority::_HIGH);

    Messages::AndroidNotification androidNotification;

    androidNotification.notification_priority(Messages::NotificationPriority::PRIORITY_HIGH);

    androidConfig.notification(androidNotification);

    msg.android(androidConfig);*/

    messaging.send(aClient1, Messages::Parent(FIREBASE_PROJECT_ID), msg, asyncCB, "messaging");

}

void playSuccessTone() {
  int melody[] = {NOTE_E5, NOTE_D5, NOTE_C5, NOTE_B4, NOTE_A4};
  int noteDurations[] = {4, 4, 4, 4, 4};

  for (int i = 0; i < 5; i++) {
    tone(BUZZER_PIN, melody[i], 1000 / noteDurations[i]);
    delay(50); // Add a small delay between notes
  }
  noTone(BUZZER_PIN); // Stop the buzzer
}
void playFailureTone() {
  int melody[] = {NOTE_A4, NOTE_B4, NOTE_C5, NOTE_D5, NOTE_E5};
  int noteDurations[] = {4, 4, 4, 4, 4};
  for (int i = 0; i < 5; i++) {
    tone(BUZZER_PIN, melody[i], 1000 / noteDurations[i]);
    delay(50); // Add a small delay between notes
  }

  noTone(BUZZER_PIN); // Stop the buzzer
}
void playWarningTone() {
  // Play a warning melody
  int melody[] = {NOTE_A3, NOTE_E4, NOTE_F4, NOTE_G4, NOTE_A4};
  int noteDurations[] = {4, 4, 4, 4, 4};
  for (int i = 0; i < 5; i++) {
    tone(BUZZER_PIN, melody[i], 1000 / noteDurations[i]);
    delay(50); // Add a small delay between notes
  }
  noTone(BUZZER_PIN); // Stop the buzzer
}
void playWelcomeMelody() {
  int melody[] = {NOTE_E5, NOTE_D5, NOTE_C5, NOTE_D5};
  int noteDurations[] = {4, 4, 4, 4};
  for (int i = 0; i < sizeof(melody) / sizeof(melody[0]); i++) {
    int noteDuration = 1000 / noteDurations[i];
    tone(BUZZER_PIN, melody[i], noteDuration);
    delay(noteDuration * 1.30); // Add a slight delay between notes
    noTone(BUZZER_PIN);
    delay(50); // Add a pause between notes
  }
}
void playNotificationTone() {
  // Define the melody and note durations for the notification tone
  int melody[] = {NOTE_C5, NOTE_E5, NOTE_G5};
  int noteDurations[] = {4, 4, 4}; // Each note is a quarter note (duration of 4)

  // Iterate over each note in the melody
  for (int i = 0; i < 3; i++) {
    // Play the current note with the specified duration
    tone(BUZZER_PIN, melody[i], 1000 / noteDurations[i]);
    delay(50); // Add a small delay between notes
  }
  noTone(BUZZER_PIN); // Stop the buzzer after playing the melody
}
void playChampionsLeagueAnthem() {
  int melody[] = {
    NOTE_A4, NOTE_AS4, NOTE_C5, NOTE_D5, NOTE_DS5, NOTE_G5, NOTE_AS5, NOTE_A5
  };
  
  int noteDurations[] = {
    4, 4, 4, 4, 4, 4, 4, 2
  };

  for (int i = 0; i < sizeof(melody) / sizeof(melody[0]); i++) {
    tone(BUZZER_PIN, melody[i], 1000 / noteDurations[i]);
    delay(1000 / noteDurations[i] + 10); // Delay between notes
  }
  noTone(BUZZER_PIN); // Stop the buzzer
}

void timeStatusCB(uint32_t &ts)
{
#if defined(ESP8266) || defined(ESP32) || defined(CORE_ARDUINO_PICO)
    if (time(nullptr) < FIREBASE_DEFAULT_TS)
    {

        configTime(3 * 3600, 0, "pool.ntp.org");
        while (time(nullptr) < FIREBASE_DEFAULT_TS)
        {
            delay(100);
        }
    }
    ts = time(nullptr);
#elif __has_include(<WiFiNINA.h>) || __has_include(<WiFi101.h>)
    ts = WiFi.getTime();
#endif
}

  