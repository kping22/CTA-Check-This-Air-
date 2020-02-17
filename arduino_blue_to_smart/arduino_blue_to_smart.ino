#include <SoftwareSerial.h>
// SoftwareSerial(RX, TX)
SoftwareSerial BTSerial(2, 3);

int dustPin = A0;
float dustVal = 0;
float dustDensity = 0;
float dustDensityug = 0;
float voMeasured = 0;
float calcVoltage = 0;
float sumVoltage =0;
byte *dustbyte = (byte *) &dustDensityug;

int index = 0;
int ledPower = 4;
int delayTime = 280;
int delayTime2 = 40;
float offTime = 9680;

// 아두이노 기본 9600 속도 셋팅 밎 센서 pin 설정
void setup(){
  Serial.begin(9600);
  BTSerial.begin(9600);
  pinMode(ledPower,OUTPUT);
  pinMode(4, OUTPUT);
}

void loop(){
  // delay time과 delay time 2만큼의 시간동안 측정 그후 9600만큼 장치 딜레이
  digitalWrite(ledPower, LOW); // power on the LED
  delayMicroseconds(delayTime);

  dustVal=analogRead(dustPin);
  calcVoltage = dustVal * (5.0 / 1024); 
  
  delayMicroseconds(delayTime2);

  digitalWrite(ledPower, HIGH); // turn the LED off
  delayMicroseconds(offTime);
  
  delay(1000);
  
  // 약 10번을 측정하여 평균값을 매긴후 블루투스 스트림에 써줌 그후 
  if(index == 10) {
    dustDensityug = dustDensityug/10;
    sumVoltage = sumVoltage/10;
    BTSerial.write(dustbyte, 4);
    BTSerial.write(".");
    Serial.print("Dust Density [ug/m3]: ");
    Serial.println(dustDensityug);
    Serial.print("votage : ");
    Serial.println(sumVoltage);
    dustDensityug = 0;
    sumVoltage = 0;
    index = 0;
  }
  else {
    sumVoltage = sumVoltage + calcVoltage;
    dustDensity = (0.17 * calcVoltage - 0.11)*1000;
    if(dustDensity < 0 || dustDensity > 200 ) {
      dustDensityug = dustDensityug + 0;
    }
    else {
      dustDensityug = dustDensityug + dustDensity;
    }
    index++; 
  }
}
