
#include <Max3421e.h>
#include <Usb.h>
#include <AndroidAccessory.h>
/*
data format

pin
A0-A7 ... analog input

4-7 ... digital input
8-11 ... digital/analog(pwm) output

receive 
[
  'a' or 'd'
  pin,
  data,
]


send
[
  4 (=data_length(byte)),
  'a' 
  port,
  data(high),
  data(low)
]
or
[
  4 (=data_length(byte)),
  'd'
  data  (pin 0-7)
  data  (0x00)
  data  (0x00)
]

*/
#define analogInMax 8
#define digitalInMax 8
#define digitalOutMax 14
int analogIns[analogInMax];
int digitalIns[digitalInMax];
int digitalOuts[digitalOutMax];
int digitalVal;

AndroidAccessory acc("Google, Inc.",
		     "AdkWikiService3",
		     "DemoKit Arduino Board",
		     "1.0",
		     "http://www.android.com",
		     "0000000012345678");
void setup();
void loop();

void setup()
{
   Serial.begin(115200);
   Serial.print("\r\nStart");
   for(int i=0;i<digitalInMax;i++) digitalIns[i]=i;
   for(int i=0;i<digitalOutMax;i++) digitalOuts[i]=i;

   for(int i=0;i<digitalInMax;i++)
      pinMode(digitalIns[i],INPUT);
   for(int i=digitalInMax;i<digitalOutMax;i++)
      pinMode(digitalOuts[i],OUTPUT);
//   pinMode(ledPin, OUTPUT);
//   pinMode(b3Pin, INPUT);
   analogIns[0]=A0;
   analogIns[1]=A1;
   analogIns[2]=A2;
   analogIns[3]=A3;
   analogIns[4]=A4;
   analogIns[5]=A5;
   analogIns[6]=A6;
   analogIns[7]=A7;
   acc.powerOn();
}

void loop()
{
   byte inMsg[3];
   byte outMsg[4];
   if (acc.isConnected()) {
     int len = acc.read(inMsg, sizeof(inMsg), 1);
     int i;
     byte b;
     if(len>0){
       if(inMsg[0]=='a'){
         if(inMsg[1]<digitalOutMax)
            analogWrite(digitalOuts[inMsg[1]], inMsg[2]);
       }
       else
       if(inMsg[0]=='d'){
         if(inMsg[1]<digitalOutMax){
             if(inMsg[2]==1)
                digitalWrite(digitalOuts[inMsg[1]], HIGH);
             else
                digitalWrite(digitalOuts[inMsg[1]], LOW);            
         }
       }
     }
     
     digitalVal=0;
     for(int i=0;i<digitalInMax;i++){
       int b=0;
       if(digitalRead(digitalIns[i])==HIGH)
          b=1;
       digitalVal=digitalVal<<1 | b;
     }
     outMsg[0]='d';
     outMsg[1]=digitalVal & 0xff;
     outMsg[2]=0;
     outMsg[3]=0;
     acc.write(outMsg,4);
     int sensorValue;
     for(int i=0;i<analogInMax;i++){
          sensorValue = analogRead(analogIns[i]);
          outMsg[0]='a';
          outMsg[1]=i;
          outMsg[2]=(sensorValue>>8) & 0xff;
          outMsg[3]=sensorValue & 0xff;
          acc.write(outMsg,4);
     }
   }
   delay(100);
}

