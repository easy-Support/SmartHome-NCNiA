#include <ArduinoJson.h>

const int fire_Sensor = A0; // 불꽃 감지 센서
const int gas_Sensor = A1; // 가스 감지 센서
const int vibe_Sensor = A2; // 진동 감지 센서
const int solar_Current_Sensor = A3; // 태양전지 전류 측정 센서
const int battery_Volt_Sensor = A4; // 배터리 전압 측정 센서
const int moter_On_Off_Switch = 8; // 모터 온오프 스위치
const int moter_Direction_Switch = 9; // 모터 정/역방향 스위치

// 1초동안 센서 값의 총합
unsigned long fire_Sensor_Values = 0; // 불꽃 감지 센서 값
unsigned long gas_Sensor_Values = 0; // 가스 감지 센서 값
unsigned long vibe_Sensor_Values = 0; // 진동 감지 센서 값
unsigned long solar_Current_Sensor_Values = 0; // 태양전지 전류 센서 값
unsigned long battery_Volt_Sensor_Values = 0; //배터리 전압 센서 값
unsigned int sensor_Value_Count = 0; // 1초동안의 개수

// 모터 관련
int moter_Step = 1; // 모터 현재 위치 값
unsigned long moter_Step_Time = 1433; // 모터 1단계 당 시간 값
unsigned long moter_Run_Time = 0; // 모터 작동해야할 시간
bool moter_Run_Time_Overflow_State = false; // 모터 작동해야할 시간이 오버플로우 여부
bool moter_Run_State = false; // 모터 작동 여부

// unsigned long 관련
const unsigned long max_Num = 4294967295; // unsigned long 최댓값
const unsigned long max_Half_Num = max_Num/2; // max_Num 절반

// 시리얼 수신 처리 관련
String receive_Data = ""; // 받아온 값 저장
int start_index = -1; // 받아온 값 시작 위치

// 시리얼 송신 처리 관련
unsigned long serial_Time = 0; // 시리얼 통신 멈춤 시간 (이 시간 이후 통신 가능)
bool serial_Time_Overflow_State = false; // 시리얼 통신 멈춤 시간 오버플로우 여부
const size_t receive_Capacity = JSON_ARRAY_SIZE(1) + JSON_OBJECT_SIZE(1); // 수신 데이터 크기 설정 (Json 1개)
const size_t transmit_Capacity = JSON_ARRAY_SIZE(1) + JSON_OBJECT_SIZE(6); // 송신 데이터 크기 설정 (Json 5개)


void setup() {
  Serial.begin(9600);

  pinMode(moter_On_Off_Switch, OUTPUT);
  pinMode(moter_Direction_Switch, OUTPUT);

  reset_Moter();
}

void loop() {
  const unsigned long now_Time = millis();
  if (time_Compare(now_Time, serial_Time, serial_Time_Overflow_State)) {
    // 전송해야할 시간
    set_Transmit_Data(101); // 데이터 전송 (101호)
    set_Transmit_Data(201); // 데이터 전송 (201호)
    set_Serial_Time(now_Time);
    save_Sensor_Data(true);
  } else {
    save_Sensor_Data(false);
  }

  // 모터 작동 정지 관련
  if(moter_Run_State && time_Compare(now_Time, moter_Run_Time, moter_Run_Time_Overflow_State)){
    // 모터가 작동 중이고 현재 시간이 모터 작동 시간보다 클 경우
    digitalWrite(moter_On_Off_Switch, LOW); // 모터 작동 정지
    digitalWrite(moter_Direction_Switch, LOW); // 방향 설정
    moter_Run_State = false;
  }
}

// 모터 초기화
void reset_Moter() {
  unsigned long now_Time = millis();
  digitalWrite(moter_Direction_Switch, HIGH); // 모터 역방향 설정
  
  unsigned long moter_Step_Time_Value = moter_Step_Time * 30; // 모터 이동 시간 값
  moter_Run_Time = now_Time + moter_Step_Time_Value;
  moter_Run_Time_Overflow_State = false;
  
  digitalWrite(moter_On_Off_Switch, HIGH); // 모터 작동 시작
  moter_Run_State = true; // 모터 작동 시작
}

// 센서 저장
void save_Sensor_Data(bool state) {
  int fire_Sensor_Value = analogRead(fire_Sensor); // 불꽃 감지 센서 값
  int gas_Sensor_Value = analogRead(gas_Sensor); // 가스 감지 센서 값
  int vibe_Sensor_Value = analogRead(vibe_Sensor); // 진동 감지 센서 값
  
  int solar_Current_Sensor_Value = analogRead(solar_Current_Sensor); // 태양전지 전류 측정 센서 값
  int battery_Volt_Sensor_Value = analogRead(battery_Volt_Sensor); // 배터리 전압 측정 센서 값

  fire_Sensor_Value = 1024 - fire_Sensor_Value; // 불꽃 감지 센서 값 반전 (원래는 낮을 수록 불꽃 감지)

  if (state) {
    // 전송 단계
    fire_Sensor_Values = 0; 
    gas_Sensor_Values = 0;
    vibe_Sensor_Values = 0; 
    solar_Current_Sensor_Values = 0;
    battery_Volt_Sensor_Values = 0;
    sensor_Value_Count = 0;
  }
  fire_Sensor_Values += fire_Sensor_Value; 
  gas_Sensor_Values += gas_Sensor_Value;
  vibe_Sensor_Values += vibe_Sensor_Value; 
  solar_Current_Sensor_Values += solar_Current_Sensor_Value;
  battery_Volt_Sensor_Values += battery_Volt_Sensor_Value;
  sensor_Value_Count++;
}

// 시리얼 통신 시간 계산
void set_Serial_Time(unsigned long now_Time) {
  if (now_Time > max_Num - 1000) {
    // 현재 시간이 최대 허용값 - 1초보다 클 경우 (오버플로우)
    serial_Time = (max_Num - now_Time) + 1000;
    serial_Time_Overflow_State = true;
  } else {
    // 현재 시간이 최대 허용값 - 1초 값 보다 작을 경우 (오버플로우 X)
    serial_Time = now_Time + 1000;
    serial_Time_Overflow_State = false;
  }
}

// 전송 처리 관련 함수
void set_Transmit_Data(int house_Number) {
  int fire_Sensor_Value = fire_Sensor_Values / sensor_Value_Count; // 불꽃 감지 센서 값 평균
  int gas_Sensor_Value = gas_Sensor_Values / sensor_Value_Count; // 가스 감지 센서 값 평균
  int vibe_Sensor_Value = vibe_Sensor_Values / sensor_Value_Count; // 진동 감지 센서 값 평균
  
  float solar_Current_Sensor_Value = solar_Current_Sensor_Values / sensor_Value_Count; // 전류 센서 값 평균
  float battery_Volt_Sensor_Value = battery_Volt_Sensor_Values / sensor_Value_Count; // 전압 센서 값 평균

  int solar_Current_Value = current_Cov(solar_Current_Sensor_Value); // 태양전지 전류값으로 변환
  int battery_Per_Value = volt_Cov(battery_Volt_Sensor_Value); // 배터리 퍼센트 값 으로 변환

  if (house_Number / 100 == 1) {
    vibe_Sensor_Value = 0;
  }

  DynamicJsonDocument transmit_doc(transmit_Capacity);

  transmit_doc["house_number"] = house_Number;
  transmit_doc["fire"] = fire_Sensor_Value;
  transmit_doc["gas"] = gas_Sensor_Value;
  transmit_doc["noise"] = vibe_Sensor_Value;
  transmit_doc["solar"] = solar_Current_Value;
  transmit_doc["battery"] = battery_Per_Value;
  
  char transmit_Data[128];

  serializeJson(transmit_doc, transmit_Data);
  Serial.println(transmit_Data);
}

// 전압 변환 (배터리 용량으로 변경)
int volt_Cov(float value) {
  float volt_Value = (value * 5.0 / 1024.0 / 0.2);
  float battery_Value = 0;
  
  if (volt_Value >= 8.4) {
    battery_Value = 1;
  } else if (volt_Value < 8.4 && volt_Value > 5.2) {
    battery_Value = (volt_Value - 5.2) / (8.4 - 5.2);
  } else {
    battery_Value = 0;
  }
  int battery_Per = battery_Value * 100;
  return battery_Per;
}

// 전류 변환
int current_Cov(int value) {
  float volt_Value = value * 5.0 / 1024.0;
  int current_Value = ((volt_Value - 2.5) * 10 * 1000) + 146;
  if (current_Value <= 0) {
    current_Value = 0;
  }
  return current_Value;
}

// 라즈베리파이에서 온 데이터를 수신했을 때
void serialEvent() {
  char serial_data = (char)Serial.read();
  receive_Data += serial_data;
  
  if (serial_data == '}') {
    start_index = receive_Data.indexOf('{'); // 시작부분 인덱스 가져오기

    String receive_Text = receive_Data.substring(start_index, receive_Data.length()); // '{' 부분 부터 '}' 부분까지 자르기

    if (receive_Text.indexOf("moter_addr") != -1) {
      char receive_Json[20] = {0};
      receive_Text.toCharArray(receive_Json, receive_Text.length() + 1);
      
      DynamicJsonDocument receive_doc(receive_Capacity);
      
      deserializeJson(receive_doc, receive_Json);
  
      // 받은 값 moter_addr이 null이 아니라면
      int receive_Addr = receive_doc["moter_addr"]; // 모터 위치 값
      run_Moter(receive_Addr); // 모터 작동
    }
    receive_Data = ""; // 받은 데이터 초기화
  }
}

// 모터 작동 설정
void run_Moter(int addr) {
  unsigned long now_Time = millis();
  int gap_Moter_Step = addr - moter_Step; // 현재 모터 위치와 설정 모터 작동 위치와의 차이
  
  if (gap_Moter_Step > 0) {
    // 차이가 양수면
    digitalWrite(moter_Direction_Switch, LOW); // 모터 정방향 설정
  } else {
    // 차이가 음수면
    digitalWrite(moter_Direction_Switch, HIGH); // 모터 역방향 설정
  }
  gap_Moter_Step = abs(gap_Moter_Step); // 절대값 변환
  
  unsigned long moter_Step_Time_Value = moter_Step_Time * gap_Moter_Step; // 모터 이동 시간 값
  if (now_Time > max_Num - moter_Step_Time_Value) {
    // 현재 시간이 최대 허용값 - 모터 이동 시간 값 보다 클 경우 (오버플로우)
    moter_Run_Time = (max_Num - now_Time) + moter_Step_Time_Value;
    moter_Run_Time_Overflow_State = true;
  } else {
    // 현재 시간이 최대 허용값 - 모터 이동 시간 값 보다 작을 경우 (오버플로우 X)
    moter_Run_Time = now_Time + moter_Step_Time_Value;
    moter_Run_Time_Overflow_State = false;
  }
  digitalWrite(moter_On_Off_Switch, HIGH); // 모터 작동 시작
  moter_Run_State = true; // 모터 작동 시작
}

unsigned long set_ABS(unsigned long a, unsigned long b) {
  unsigned long gap_Time = 0;
  // abs() [절댓값] 이 안 먹혀서 사용
  if (a > b) {
    // a 시간이 b시간 보다 클 경우
    gap_Time = a - b;
  } else {
    // a 시간이 b시간 보다 작을 경우
    gap_Time = b - a;
  }
  return gap_Time;
}

// 모터 작동 시간 여부 (현재시간이 작동 시간보다 클 경우)
bool time_Compare(unsigned long now_Time, unsigned long compare_Time, bool overflow_State) {
  unsigned long gap_Time = set_ABS(now_Time, compare_Time); // 현재 시간과 모터 시간 차이
  
  if (overflow_State) {
    // 모터 작동 시간이 오버플로우 상태라면
    if (now_Time >= max_Half_Num) {
      // 현재 시간이 허용값 절반보다 크면 (아직 현재 시간이 오버플로우 안된다고 판단)
      if(long(now_Time - max_Num) >= long(compare_Time)) {
        // 현재 시간이 모터 작동 시간보다 클 경우 (이 경우 클 수가 없음)
        return true;
      } else {
        return false;
      }
    } else {
      // 현재 시간이 허용값 절반보다 작다면 (현재 시간이 오버플로우 됬다고 판단)
      if (now_Time >= compare_Time) {
        return true;
      } else {
        return false;
      }
    }
  } else {
    // 모터 작동 시간이 오버플로우 상태가 아니라면
    if (gap_Time >= max_Half_Num) {
      // 현재 시간과 모터 작동 시간 사이 값이 허용값 절반 보다 크면 (현재 시간이 오버 플로우 되었다고 판단)
      if (long(now_Time) >= long(compare_Time - max_Num)) {
        return true;
      } else {
        return false;
      }
    } else {
      // 현재 시간이 오버 플로우가 안된 경우
      if (now_Time >= compare_Time) {
        return true;
      } else {
        return false;
      }
    }
  }
}
