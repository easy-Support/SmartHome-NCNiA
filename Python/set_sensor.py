# -*- coding: utf-8 -*-
from collections import OrderedDict

import datetime
import notification
import pandas as pd


class set_Sensor(notification.notification):
    def __init__(self):
        notification.notification.__init__(self)
        self.state_Data = ["좋음", "보통", "나쁨", "심각"]  # 현황 데이터

        # 데이터 저장
        self.house_Data = pd.DataFrame(
            columns=[
                'House_Number',  # 집 호수
                'Fire_State',  # 불꽃 감지 수치 현황
                'Gas_State',  # 가연성 가스 수치 현황
                'Noise_State',  # 층간 소음 현황
                'Fire_Count',  # 화재 감지 카운트
                'Gas_Count',  # 가연성 가스 카운트
                'Noise_Count',  # 층간 소음 카운트
                'Fire_Warning_Time',  # 화재 위험 알림 시간
                'Gas_Warning_Time',  # 화재 위험 알림 시간
                'Noise_Warning_Time',  # 화재 위험 알림 시간
            ])

        self.fire_Allow = 50  # 불꽃 감지 수치 허용 치
        self.gas_Allow = 150  # 가연성 가스 수치 허용 치
        self.noise_Allow = 100  # 층간 소음 수치 허용 치

        self.solar_Current = 0  # 태양전지 전류
        self.battery_Remain = 0  # 배터리 잔량

        self.motor_run = False  # 모터 작동 여부
        self.motor_addr = 1  # 모터 현재 위치

    # 센서 값이 허용치가 넘으면 카운트 +1, 아니면 0
    def __allow_Check(self, allow, value, count):
        if value >= allow:
            # 센서 값이 허용 치보다 같거나 클 경우
            count += 1
        else:
            # 센서 값이 허용 치 보다 작을 경우
            if count != 0:
                count -= 1

        if count >= 255:
            # 최댓값 255 설정
            count = 255
        return count

    # 상태값 변환
    def __state_Cov(self, count):
        warning = False
        if count < 10:
            # 카운트가 10 미만이면 좋음 (10초 미만)
            state = self.state_Data[0]
        elif count < 30:
            # 카운트가 30 미만이면 보통 (10 ~ 30초)
            state = self.state_Data[1]
        elif count < 60:
            # 카운트가 60 미만이면 나쁨 (30초 ~ 60초)
            state = self.state_Data[2]
        else:
            # 카운트가 60 이상이면 심각 (60초 이상)
            state = self.state_Data[3]
            warning = True


        # 테스트 모드
        if count < 5:
            state = self.state_Data[0]
        else:
            state = self.state_Data[3]
            warning = True

        return [state, warning]

    # 화재 상태 설정
    def set_fire_State(self, index, value):
        count_Previous = self.house_Data['Fire_Count'].values[index]
        count_After = self.__allow_Check(self.fire_Allow, value, count_Previous)
        state = self.__state_Cov(count_After)

        if state[1]:
            # 심각 단계일 경우
            now_time = datetime.datetime.now()  # 현재 시간
            warning_time_Str = self.house_Data['Fire_Warning_Time'].values[index]  # 알람 울린 시간
            warning_time = datetime.datetime.strptime(warning_time_Str, "%Y-%m-%d %H:%M:%S")  # 알람 울린 시간 변환

            if now_time >= warning_time:
                # 지금 시간이 알람 울린 시간 + 5분(유효 시간)보다 크거나 같으면 알람
                house_Number = self.get_House_Number(index)
                valid_time = datetime.timedelta(minutes=1)  # 유효 시간 : 5분
                warning_Time_Set = now_time + valid_time
                self.house_Data.loc[index, ['Fire_Warning_Time']] = warning_Time_Set.strftime("%Y-%m-%d %H:%M:%S")
                self.send_fcm_notification(house_Number, "스마트홈에 화재가 발생했습니다!!!")

                house_Number_All = self.house_Data['House_Number']
                for i in range(len(house_Number_All)):
                    if house_Number_All[i] != house_Number:
                        self.send_fcm_notification(house_Number, str(house_Number) + "호에서 화재가 발생했습니다!!!")

        self.house_Data.loc[index, ['Fire_State']] = state[0]
        self.house_Data.loc[index, ['Fire_Count']] = count_After

    # 가스 상태 설정
    def set_gas_State(self, index, value):
        count_Previous = self.house_Data['Gas_Count'].values[index]
        count_After = self.__allow_Check(self.gas_Allow, value, count_Previous)
        state = self.__state_Cov(count_After)

        if state[1]:
            # 심각 단계일 경우
            now_time = datetime.datetime.now()  # 현재 시간
            warning_time_Str = self.house_Data['Gas_Warning_Time'].values[index]  # 알람 울린 시간
            warning_time = datetime.datetime.strptime(warning_time_Str, "%Y-%m-%d %H:%M:%S")  # 알람 울린 시간 변환

            if now_time >= warning_time:
                # 지금 시간이 알람 울린 시간 + 5분(유효 시간)보다 크거나 같으면 알람
                house_Number = self.get_House_Number(index)
                valid_time = datetime.timedelta(minutes=1)  # 유효 시간 : 5분
                warning_Time_Set = now_time + valid_time
                self.house_Data.loc[index, ['Gas_Warning_Time']] = warning_Time_Set.strftime("%Y-%m-%d %H:%M:%S")
                self.send_fcm_notification(house_Number, "스마트홈의 가연성 가스 수치가 높습니다!!!")

        self.house_Data.loc[index, ['Gas_State']] = state[0]
        self.house_Data.loc[index, ['Gas_Count']] = count_After

    # 층간 소음 설정
    def set_house_Noise(self, index, value):
        count_Previous = self.house_Data['Noise_Count'].values[index]
        count_After = self.__allow_Check(self.noise_Allow, value, count_Previous)
        state = self.__state_Cov(count_After)

        if state[1]:
            # 심각 단계일 경우
            now_time = datetime.datetime.now()  # 현재 시간
            warning_time_Str = self.house_Data['Noise_Warning_Time'].values[index]  # 알람 유효 시간
            warning_time = datetime.datetime.strptime(warning_time_Str, "%Y-%m-%d %H:%M:%S")  # 알람 유효 시간 변환

            if now_time >= warning_time:
                # 현재 시간이 알람 울려야할 유효 시간보다 크거나 같으면 알람
                house_Number = self.get_House_Number(index)
                valid_time = datetime.timedelta(minutes=1)  # 유효 시간 : 5분
                warning_Time_Set = now_time + valid_time
                self.house_Data.loc[index, ['Noise_Warning_Time']] = warning_Time_Set.strftime("%Y-%m-%d %H:%M:%S")
                self.send_fcm_notification(house_Number, "스마트홈의 층간 소음 수치가 매우 높습니다!!!")

        self.house_Data.loc[index, ['Noise_State']] = state[0]
        self.house_Data.loc[index, ['Noise_Count']] = count_After

    # 태양전지 전류 설정
    def set_solar_Current(self, state):
        self.solar_Current = state

    # 배터리 잔량 설정
    def set_battery_Remain(self, state):
        self.battery_Remain = state

    # 모터 작동 여부 설정
    def set_motor_run(self, state):
        self.motor_run = state

    # 모터 현재 위치 설정
    def set_motor_addr(self, state):
        self.motor_addr = state

    # 카운트 리셋
    def set_Reset_Count(self, index, type):
        if type == "fire":
            self.house_Data.loc[index, ['Fire_Count']] = 0
        elif type == "gas":
            self.house_Data.loc[index, ['Gas_Count']] = 0
        elif type == "noise":
            self.house_Data.loc[index, ['Noise_Count']] = 0

    # 데이터를 Json 형태로 반환
    def get_Json_Data(self, index):
        file_data = OrderedDict()

        if index != -1:
            file_data["fire"] = self.house_Data['Fire_State'].values[index]
            file_data["gas"] = self.house_Data['Gas_State'].values[index]
            file_data["noise"] = self.house_Data['Noise_State'].values[index]

            file_data["solar"] = str(self.solar_Current) + "mA"
            file_data["battery"] = str(self.battery_Remain) + "%"
        else:
            file_data["fire"] = ""
            file_data["gas"] = ""
            file_data["noise"] = ""

            file_data["solar"] = str(self.solar_Current) + "mA"
            file_data["battery"] = str(self.battery_Remain) + "%"

        return file_data

    # 호수로 인덱스 값 반환
    def get_index(self, number):
        try:
            index = self.house_Data.loc[self.house_Data['House_Number'] == number].index[0]
        except:
            index = -1
        return index

    # 인덱스로 호수 값 반환
    def get_House_Number(self, index):
        try:
            number = self.house_Data['House_Number'].values[index]
        except:
            number = 0
        return number

    # 호수 설정
    def set_House_Data(self, number):
        now_time = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        self.house_Data = self.house_Data.append({
            'House_Number': number,
            'Fire_State': '좋음',  # 불꽃 감지 수치 현황
            'Gas_State': '좋음',  # 가연성 가스 수치 현황
            'Noise_State': '좋음',  # 층간 소음 현황
            'Fire_Count': 0,
            'Gas_Count': 0,
            'Noise_Count': 0,
            'Fire_Warning_Time': now_time,
            'Gas_Warning_Time': now_time,
            'Noise_Warning_Time': now_time
        }, ignore_index=True)