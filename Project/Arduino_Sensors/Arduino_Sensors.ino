//--------- Humidity ---------//
#include <DHT.h>
#define DHTPIN 3
#define DHTTYPE DHT22
DHT dht(DHTPIN, DHTTYPE);

//--------- Variable Declaration ---------//
float Hum;
float Temp;
String str;
String hh = "dd";
char val[16];
int Light = A0;int Lvalue;
int Radio = A1;int Rvalue;

//--------- Main ---------//
void setup() {
  Serial.begin(9600);
  dht.begin();         // Khởi động cảm biến
  pinMode(Light,INPUT);//pinMode nhận tín hiệu đầu vào cho cảm biến
  pinMode(Radio,INPUT);//pinMode nhận tín hiệu đầu vào cho cảm biên
}

void loop() {
  //====== Get value sensor
  Hum = dht.readHumidity();    //Đọc độ ẩm
  Temp = dht.readTemperature(); //Đọc nhiệt độ
  float Lvalue = analogRead(Light);//lưu giá trị cảm biến vào biến Lvalue
  float Rvalue = analogRead(Radio);//lưu giá trị cảm biến vào biến Rvalue

  // Call out all data which got from sensors 
  //Serial.print("Nhiet do: ");
  //Serial.print((int)Temp);               //Xuất nhiệt độ
  //Serial.println();
  
  //Serial.print("Do am: ");
  //Serial.print((int)Hum);               //Xuất độ ẩm
  //Serial.println();
  
  //Serial.print("Anh Sang: ");
  //Serial.print((int)Lvalue);               //Xuất anh sang
  //Serial.println();
  
  //Serial.print("Gas: ");
  //Serial.println((int)Rvalue);               //Xuất gas
  //Serial.println();

  //====== Delay Synchronize with Pi
  delay(7000);
  
  //str=(String)Temp+"|"+(String)Hum+"|"+(String)Lvalue+"|"+(String)Rvalue;
  //strcpy(val,str.c_str());
//  Serial.println((int)Temp);              
//  Serial.println();
//  Serial.println((int)Hum);              
//  Serial.println();
//  Serial.println((int)Lvalue);              
//  Serial.println();
//  Serial.println((int)Rvalue);              
//  Serial.println();
  //Temp=Temp/10;

  //====== Update value sensor before sending UART
  Lvalue = Lvalue/10;
  Rvalue = Rvalue/10;

  //====== Sending UART
  Serial.write((int)Temp);
  Serial.write((int)Hum);
  Serial.write((int)Lvalue);
  Serial.write((int)Rvalue);
//  Serial.write(11);
//  Serial.write(22);
//  Serial.write(33);
//  Serial.write(44);
  //Serial.write(200);
}
