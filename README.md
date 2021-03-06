# SmartHome-NCNiA
## 프로젝트 설명
+ 2020년 전문대 LINC플러스사업(사회맞춤형학과 중점형) 팀프로젝트 경진대회 참가한 팀 프로젝트입니다.
+ 서로 배려하는 스마트홈이라는 주제로 아두이노, 라즈베리파이 그리고 어플리케이션을 이용하여 프로젝트를 진행했습니다.

## 주요 기능
+ 아두이노의 진동센서 모듈을 이용하여 위 층에서의 층간 소음 발생 시 해당 층의 어플리케이션에 알람이 울립니다.
+ 아두이노의 가연성 가스 센서, 불꽃센서 모듈을 이용하여 해당 층에서 화재 발생 시 어플리케이션에 알람이 울립니다.
+ 리니어모터를 이용하여 태양 전지판의 각도를 조절하여 시간 별로 효율적인 위치로 이동합니다.
+ 어플리케이션으로 일출와 일몰 시간, 현재 날씨와 온도, 화재 발생 여부와 가연성 가스 수치, 층간 소흠 현황, 태양전지 전류 및 배터리 잔량을 볼 수 있습니다.

## 팀 구성
+ 어플리케이션 개발: Jiwon Lee
+ 아두이노 개발: Huijae Kim, Jiwon Lee
+ 라즈베리파이 서버 개발: Ikhyeon Kim, Jiwon Lee

## 프로젝트 기간
+ 2020년 9월 ~ 2020년 12월

## 사용
### 아두이노
+ Arduino IDE (C/C++)
+ Arduino Mega 2560
   + 불꽃센서 모듈
   + 가연성 가스 센서 모듈
   + 진동센서 모듈
   + 전압측정센서 모듈
   + 전류센서 모듈
   + 리니어모터
   + 태양 전지판

### 라즈베리파이
+ Python 3.8
   + FastAPI 
+ Raspberry Pi 3 Model B+

### 안드로이드 어플리케이션
+ Android Studio (Java)
+ Android Q (10, API Level 29)

## 어플케이션 화면
### 로딩화면 & 메인 화면
![main_page](https://user-images.githubusercontent.com/65817334/117976790-1e877b00-b36b-11eb-8ce5-ba52895eac43.png)

### 메뉴 화면
![menu](https://user-images.githubusercontent.com/65817334/117978533-f13bcc80-b36c-11eb-8e61-c3c52fa9f5cf.png)

### 화재 화면
![fire](https://user-images.githubusercontent.com/65817334/117975991-3b6f7e80-b36a-11eb-9f8a-3eb979021de0.png)

### 가연성 가스 화면
![gas](https://user-images.githubusercontent.com/65817334/117975993-3ca0ab80-b36a-11eb-8b0d-00efbdd04dea.png)

### 층간 소음 화면
![noise](https://user-images.githubusercontent.com/65817334/117975997-3d394200-b36a-11eb-9e16-09603333375f.png)
