#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>
#include <HX711.h>

BLEServer* pServer = NULL;
BLECharacteristic* pCharacteristic = NULL;
bool deviceConnected = false;
#define led 2
BLEService *pService = NULL;
String value = "";
float weight = 0.00;

#define SERVICE_UUID "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
#define CHARACTERISTIC_UUID "beb5483e-36e1-4688-b7f5-ea07361b26a8"


#define DT 14
#define SCK 5


HX711 scale;

class MyServerCallbacks : public BLEServerCallbacks {
  void onConnect(BLEServer* pServer) {
    deviceConnected = true;
    Serial.println("connected");
  }

  void onDisconnect(BLEServer* pServer) {
  deviceConnected = false;
  Serial.println("disconnected");

  BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->start();
  Serial.println("Restarting advertising...");
}

};

class MyCallbacks : public BLECharacteristicCallbacks {
  void onWrite(BLECharacteristic *pCharacteristic) {
    value = pCharacteristic->getValue().c_str();
    if (value == "get") {
      Serial.print("Weight String: ");
      String weightStr = String(weight, 2);
      Serial.println(weightStr);
      pCharacteristic->setValue(weightStr.c_str());
      pCharacteristic->notify();
    }
  }
};

void setup() {

  pinMode(led,OUTPUT);
  Serial.begin(115200);

  scale.begin(DT, SCK);
  scale.set_scale(0.98);
  scale.tare();

  BLEDevice::init("Mera Vajan");
  pServer = BLEDevice::createServer();
  pServer->setCallbacks(new MyServerCallbacks());

  pService = pServer->createService(SERVICE_UUID);

  pCharacteristic = pService->createCharacteristic(
    CHARACTERISTIC_UUID,
    BLECharacteristic::PROPERTY_READ |
    BLECharacteristic::PROPERTY_WRITE |
    BLECharacteristic::PROPERTY_NOTIFY
  );

  pCharacteristic->addDescriptor(new BLE2902());
  pCharacteristic->setCallbacks(new MyCallbacks());
  pCharacteristic->setValue("Mera Vajan");

  pService->start();
  BLEDevice::startAdvertising();
}

void loop() {
  weight = scale.get_value(10);
  Serial.println(weight);
  if (deviceConnected) {
    digitalWrite(led, HIGH);
    delay(1000);
  } else {
    digitalWrite(led, millis() / 100 % 2 == 0 ? HIGH : LOW);
  }
}

