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

#define SERVICE_UUID "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
#define CHARACTERISTIC_UUID "beb5483e-36e1-4688-b7f5-ea07361b26a8"


#define DT 4
#define SCK 5

float weight;

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
    weight = scale.get_units(20);
    pCharacteristic->setValue(value);
    pCharacteristic->notify();
    Serial.println(value);
  }
};


void setup() {

  pinMode(led,OUTPUT);
  Serial.begin(115200);

  scale.set_scale(489.35);
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
  if (deviceConnected) {
    digitalWrite(led, HIGH);
    delay(1000);
  } else {
    digitalWrite(led, millis() / 100 % 2 == 0 ? HIGH : LOW);
  }
}

