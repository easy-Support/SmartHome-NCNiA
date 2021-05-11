# -*- coding: utf-8 -*-
from collections import OrderedDict
from fastapi import FastAPI
from pydantic import BaseModel

import weather_and_suntime
import arduino_serial
import json
import pandas as pd
import datetime
import threading


# Item 클래스
class Item(BaseModel):
    fcm_key: str = ""
    house_num: str = ""
    type: str = ""

    def get_fcm_key(self):
        return self.fcm_key

    def get_house_num(self):
        return self.house_num

    def get_type(self):
        return self.type


# 보낼 데이터를 Json 형태로 반환
def get_Data_Json(house_Number):
    print(datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S") + " (API) : 전송할 데이터를 Json형태로 변환을 시작합니다.")

    file_data = OrderedDict()

    file_data["time"] = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")

    if house_Number == "":
        # 집 호수가 없을 경우
        house_Number = 0
    else:
        # 집 호수가 있는 경우
        house_Number = int(house_Number)

    index = set_Arduino_Serial.get_index(house_Number)
    # 집 호수가 저장 되어 있을 경우에만 센서 데이터 추가
    file_data.update(set_Arduino_Serial.get_Json_Data(index))

    file_data.update(data_Weather_AND_Suntime.get_Json_Data())

    json_Data = json.dumps(file_data, ensure_ascii=False, separators=(',', ':'))

    print(datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S") + " (API) : Json형태로 변환을 완료했습니다.")
    return json_Data

# 업데이트
def update_Thread():
    now_Date = pd.Timestamp(datetime.datetime.now().strftime("%Y-%m-%d"))
    now_Time = pd.Timestamp(datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S"))

    # 일출, 일몰 가져온 시간 비교해서 지난 날 일 경우 갱신
    #data_Weather_AND_Suntime.set_Suntime(now_Date)

    # 날씨 갱신한 시간 + 10분이 현재 시간보다 작을 경우 갱신
    #data_Weather_AND_Suntime.set_Weather(now_Time)

    # 모터 위치를 아두이노에게 전달
    #moter_Addr = data_Weather_AND_Suntime.weather_time_decision()

    #json_Moter_Data = data_Weather_AND_Suntime.get_Moter_Json(moter_Addr)
    #set_Arduino_Serial.transmit_Data(json_Moter_Data)

    print(datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S") + ": Thread 중 입니다.")

    threading.Timer(60, update_Thread).start()

# ========================================================================================================


# 업데이트 실행
data_Weather_AND_Suntime = weather_and_suntime.weather_and_Suntime()
set_Arduino_Serial = arduino_serial.arduino_Serial()

update_Thread()

# API 실행
app = FastAPI()


# ========================================================================================================
# API 설정 부분
@app.post("/")
async def root(item: Item):
    print(datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S") + " (API) : 요청이 들어왔습니다.")

    # 수신한 Key 값
    get_Fcm_Key = item.get_fcm_key()
    get_House_Num = item.get_house_num()

    print(datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S") + " (API) : 수신한 데이터 : fcm_key = " + get_Fcm_Key + " / house_num = " + get_House_Num)

    if get_Fcm_Key != "":
        #수신한 KEY 값이 있을 경우
        print(datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S") + " (API) : 수신한 데이터에서 KEY 값과 집 호수 데이터가 존재합니다.")
        set_Arduino_Serial.key_dec(item)
    else:
        print(datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S") + " (API) : 수신한 데이터에서 KEY 값 또는 집 호수 데이터가 존재하지 않습니다.")

    # 보낼 정보 Json 형태로 변환
    json_Data = get_Data_Json(get_House_Num)

    print(datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S") + " (API) : 전송할 데이터 : " + json_Data)
    print(datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S") + " (API) : 데이터 전송 완료")

    return json_Data


@app.post("/send/")
async def send(item: Item):
    print(datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S") + " (API) : 요청(저장)이 들어왔습니다.")

    # 수신한 Key 값
    get_Fcm_Key = item.get_fcm_key()
    get_House_Num = item.get_house_num()
    get_Type = item.get_type()

    print(datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S") + " (API) : 수신한 데이터 : fcm_key = " + get_Fcm_Key + " / house_num = " + get_House_Num + " / type = " + get_Type)

    # 카운트 수 초기화
    if get_House_Num != "":
        # 호수가 있을 경우
        index = set_Arduino_Serial.get_index(int(get_House_Num))
        set_Arduino_Serial.set_Reset_Count(index, get_Type)

# ========================================================================================================