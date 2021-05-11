# -*- coding: utf-8 -*-
import requests
import json
import datetime

class notification:
    def __init__(self):
        self.app_House_Data = [[None]]  # 집 주소 & FCM 토큰 저장 배열

    # 집 주소, 토큰 반환
    def get_App_House_Data(self):
        return self.app_House_Data

    # 집 주소, 토큰 저장
    def set_App_House_Data(self, data):
        self.app_House_Data = data

    # 알림 보내기
    def send_fcm_notification(self, number, body):
        print(datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S") + " (FCM) : 알림 보내기를 시작합니다.")
        print(datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S") + " (FCM) : 알림 내용 : " + body)

        # fcm 푸시 메세지 요청 주소
        url = 'https://fcm.googleapis.com/fcm/send'

        # 인증 정보(서버 키)를 헤더에 담아 전달
        headers = {
            'Authorization': 'key=AAAAoFCNx2o:APA91bEUoxN2z72Fuc2L8cvk5XqVoEr0woZE2r2q-ZwpjUfEvlZ6qlxP-d54QDsEecNDp8ZeJDq3bEpsRYYzLM063EEifzCyJbw0VU-krLTzFMnh-yGvYNWJlViuiwrLW4xL4t5ingYA',
            'Content-Type': 'application/json; UTF-8',
        }

        # 보낼 내용과 대상을 지정
        get_data = self.app_House_Data
        for i in range(len(get_data)):
            if not get_data[i][0] is None and get_data[i][1] == str(number):
                # 해당 배열 위치가 공백이 아니고, 호수가 알람 울린 호수와 일치한 경우
                key = get_data[i][0]
                content = {
                    "to": key,
                    "priority": "high",
                    "data": {
                        "title": "SmartHome",
                        "body": body,
                    },
                    "notification": {
                        "title": "SmartHome",
                        "body": body,
                    }
                }
                # json 파싱 후 requests 모듈로 FCM 서버에 요청
                print(datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S") + " (FCM) : 해당 단말(" + key + ")에게 알림을 보냅니다.")
                request_msg = requests.post(url, data=json.dumps(content), headers=headers)
                print(datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S") + " (FCM) : 응답 메시지 : " + str(request_msg))

        print(datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S") + " (FCM) : 알림 보내기가 끝났습니다.")

    # 키 확인 하는 함수
    def key_dec(self, get_data):
        set_data = [get_data.get_fcm_key(), get_data.get_house_num()]

        for i in range(len(self.app_House_Data)):
            if self.app_House_Data[i][0] is None:
                # 키 값이 공백이면 해당 위치에 저장 및 반환
                print(datetime.datetime.now().strftime(
                    "%Y-%m-%d %H:%M:%S") + " (API) : 저장되어있는 데이터에서 공백이 존재합니다. 데이터를 저장합니다.")
                self.app_House_Data[i] = set_data
                return

            if self.app_House_Data[i][0] == get_data.get_fcm_key():
                # 키가 일치하는 것이 있으면 집 호수 설정
                print(datetime.datetime.now().strftime(
                    "%Y-%m-%d %H:%M:%S") + " (API) : 저장되어있는 데이터에서 일치한 키 값이 존재합니다.")
                self.app_House_Data[i] = set_data
                return

        # 키가 일치하는 것이 없으면 저장 및 반환
        self.app_House_Data.append(set_data)
        print(datetime.datetime.now().strftime(
            "%Y-%m-%d %H:%M:%S") + " (API) : 저장되어있는 데이터에서 일치한 키 값이 존재하지 않습니다. 데이터를 저장합니다.")




