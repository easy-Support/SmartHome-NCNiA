# -*- coding: utf-8 -*-
from urllib.request import urlopen, Request
from collections import OrderedDict

import urllib
import json
import bs4
import pandas as pd
import datetime


# 웹 크롤링해서 날씨값과 일출,일몰 시간 가져오기
class weather_and_Suntime:
    def __init__(self):
        # 초기화
        self.today_Date = pd.Timestamp(datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S"))  # 오늘 날짜
        self.now_Time = pd.Timestamp(datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S"))  # 현재 시간

        self.today_Sunrise_Time = pd.Timestamp(0)  # 일출 시간 (초기값 : 0)
        self.today_Sunset_Time = pd.Timestamp(0)  # 일몰 시간 (초기값 : 0)
        self.now_Weather = "맑음"  # 현재 날씨 (초기값 : "맑음")
        self.now_Temp = "0"  # 현재 온도 (초기값 : "24")

        self.get_Suntime_Date = pd.Timestamp(0)  # 일출, 일몰 가져온 시간
        self.get_Weather_Date = pd.Timestamp(0)  # 날씨 가져온 시간

    def get_Sunrise_Time(self):
        return self.today_Sunrise_Time

    def get_Sunset_Time(self):
        return self.today_Sunset_Time

    # 일출, 일몰 가져온 시간 비교해서 지난 날 일 경우 갱신
    def set_Suntime(self, now_Date):
        if self.get_Suntime_Date < now_Date:
            self.suntime_data()

    # 날씨 갱신한 시간 + 10분이 현재 시간보다 작을 경우 갱신
    def set_Weather(self, now_Time):
        weather_Update = pd.Timestamp(self.get_Weather_Date + datetime.timedelta(minutes=10))  # 업데이트 한 시간 + 10분
        if weather_Update < now_Time:
            self.weather_data()

    def get_Json_Data(self):
        file_data = OrderedDict()

        file_data["sunrise"] = str(self.today_Sunrise_Time.date()) + " " + str(self.today_Sunrise_Time.time())
        file_data["sunset"] = str(self.today_Sunset_Time.date()) + " " + str(self.today_Sunset_Time.time())
        file_data["weather"] = self.now_Weather
        file_data["temp"] = self.now_Temp + " ℃"

        return file_data

    # 웹 크롤링 (HTML 가져오기)
    def web_crawling(self, url):
        # HTTP 요청
        req = Request(url)

        # 웹 리소스 가져오기
        page = urlopen(req)

        # 페이지 읽어 오기
        html = page.read()

        # BeautifulSoup으로 html소스를 python객체로 변환하기
        soup = bs4.BeautifulSoup(html, 'html5lib')
        return soup


    # 날씨값 가져오기
    # 1 : 날씨
    # 2 : 현재 온도
    def weather_data(self):
        location = '울산광역시 동구 화정동'
        enc_location = urllib.parse.quote(location + '+ 날씨')

        url = 'https://search.naver.com/search.naver?ie=utf8&query=' + enc_location

        # HTML 가저오기
        soup = self.web_crawling(url)

        try:
            # 현재 날씨 가져오기
            now_weather_data = soup.find('ul', class_='info_list').find('p', class_='cast_txt').text
            comma_location = now_weather_data.find(',')
            now_weather_data = now_weather_data[0:comma_location]

            # 현재 온도 가져오기
            now_temp_data = soup.find('p', class_='info_temperature').find('span', class_='todaytemp').text

            # 전역 변수에 저장
            self.now_Weather = now_weather_data
            self.now_Temp = now_temp_data

            self.get_Weather_Date = pd.Timestamp(datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S"))
            print(datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S") + ": 날씨 갱신 완료")
        except:
            print(datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S") + ": 접속 실패 / 날씨 갱신 실패")


    # 날씨와 시간 판단
    # 날씨가 맑음, 구름 많음이 아니라면 0 반환
    # 시간이 일출 ~ 일몰 시간이 아닌 경우 0 반환
    # 날씨가 맑고, 구름 많음이고, 현재 시간이 일출 ~ 일몰 시간안에 있을 경우 30개를 나누어서 숫자 반환
    def weather_time_decision(self):
        # 날씨 허용 배열
        allow_weather = ["맑음", "대체로 맑음", "구름 많음"]

        # 날씨 판단 (현재 날씨가 날씨 허용 배열에 포함되지 않으면 1 반환)
        if self.now_Weather not in allow_weather:
            return 1

        # 현재 시간
        now_time = pd.Timestamp(datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S"))

        # 현재날씨 판단 (현재 시간이 일출 시간보다 작고, 일몰 시간보다 클 경우 0 반환)
        if now_time < self.today_Sunrise_Time and now_time > self.today_Sunset_Time:
            return 1

        # 일출시간과 일몰 시간 30개로 분할한 시간
        time_division = (self.today_Sunset_Time - self.today_Sunrise_Time) / 30

        for i in range(0, 30):
            # 시작 시간
            time_start = self.today_Sunrise_Time + (time_division * i)

            # 끝 시간
            time_end = self.today_Sunrise_Time + (time_division * (i + 1))

            # 현재 시간이 시작 시간보다 크고, 끝 시간보다 작을 경우 i 반환
            if now_time > time_start and now_time < time_end:
                return i + 1

        # for 문에서 반환하지 못한 모든 것들 1 반환
        return 1


    # 일출, 일몰 시간 가져오기
    # 1 : 일출 시간
    # 2 : 일몰 시간
    def suntime_data(self):
        url = 'https://weather.naver.com/today/10170102'

        # HTML 가저오기
        soup = self.web_crawling(url)

        # 일몰, 일출 시간 가져오기
        sun_time_data_table = soup.find('table', class_='sun_table')

        if not sun_time_data_table is None:
            sun_time_data = sun_time_data_table.find_all("strong", class_="sun_time")

            sunrise_text = sun_time_data[0].text
            sunset_text = sun_time_data[1].text

            # 오늘 날짜
            today_date = datetime.datetime.today().strftime("%Y-%m-%d")

            # 일출 시간
            self.today_Sunrise_Time = pd.Timestamp(today_date + " " + sunrise_text)

            # 일몰 시간
            self.today_Sunset_Time = pd.Timestamp(today_date + " " + sunset_text)

            self.get_Suntime_Date = pd.Timestamp(datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S"))
            print(datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S") + ": 일출, 일몰시간 갱신 완료")
        else:
            sun_time_data_div = soup.find('div', class_='sun_info')
            if not sun_time_data_div is None:
                sun_time_data = sun_time_data_div.find_all("dd", class_="time_sun")

                sunrise_text = sun_time_data[0].text
                sunset_text = sun_time_data[1].text

                # 오늘 날짜
                today_date = datetime.datetime.today().strftime("%Y-%m-%d")

                # 일출 시간
                self.today_Sunrise_Time = pd.Timestamp(today_date + " " + sunrise_text)

                # 일몰 시간
                self.today_Sunset_Time = pd.Timestamp(today_date + " " + sunset_text)

                self.get_Suntime_Date = pd.Timestamp(datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S"))
                print(datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S") + ": 일출, 일몰시간 갱신 완료")
            else:
                print(datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S") + ": 접속 실패 / 일출, 일몰 시간 갱신 실패")

    def get_Moter_Json(self, data):
        file_data = OrderedDict()
        file_data["moter_addr"] = data
        json_Data = json.dumps(file_data, ensure_ascii=False, separators=(',', ':'))
        return json_Data
