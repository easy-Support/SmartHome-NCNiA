# -*- coding: utf-8 -*-
import serial
import datetime
import json
import threading
import set_sensor
import sys
import glob


class arduino_Serial(set_sensor.set_Sensor):
    def __init__(self):
        set_sensor.set_Sensor.__init__(self)
        self.line = []  # 라인 단위로 데이터 가져올 리스트 변수

        #self.port = "COM3" /dev/ttyACM0 # 시리얼 포트
        self.port = "/dev/ttyACM0"  # 시리얼 포트
        self.baud = 9600  # 시리얼 보드레이트(통신속도)

        self.state = True  # 쓰레드 종료용 변수

        self.receive_Json = ""  # 받은 데이터 저장하는 변수
        self.serial_Open_State = True  # 시리얼 포트 여부

        #self.update_Serial()  # 포트명 찾기

        self.serial_Open = serial.Serial(self.port, self.baud, timeout=0)  # 시리얼 열기
        thread = threading.Thread(target=self.__readThread, args=(self.serial_Open,))  # 시리얼 읽을 쓰레드 생성
        print(datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S") + " (Serial) : 포트를 연결합니다.")
        thread.start()  # 시작

    # 시리얼 포트 찾기
    def update_Serial(self):
        print(datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S") + " (Serial) : 포트 리스트를 찾습니다")
        self.port = self.serial_ports()
        if self.port:
            # 포트가 있을 경우
            print(datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S") + " (Serial) : 포트 리스트를 찾았습니다. 포트명 리스트 : " + str(self.port))
            self.start_Serial()
        else:
            # 포트가 없는 경우
            print(datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S") + " (Serial) : 포트 리스트를 찾지 못했습니다. 10초 다시 찾습니다.")
            threading.Timer(10, self.update_Serial).start()  # 10초마다 체크

    def start_Serial(self):
        for i in range(len(self.port)):
            try:
                self.serial_Open = serial.Serial(self.port[i], self.baud, timeout=0)  # 시리얼 열기
                print(datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S") + " (Serial) : 포트를 찾았습니다. 포트명 : " + self.port[i])
                self.serial_Open_State = True
            except:
                print(datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S") + " (Serial) : 포트가 맞지 않습니다. 포트명 : " + self.port[i])
                self.serial_Open_State = False

        if self.serial_Open_State:
            thread = threading.Thread(target=self.__readThread, args=(self.serial_Open,))  # 시리얼 읽을 쓰레드 생성
            thread.start()  # 시작
        else:
            print(datetime.datetime.now().strftime(
                "%Y-%m-%d %H:%M:%S") + " (Serial) : 포트 리스트에서 맞는 포트를 찾기 못했습니다. 10초 후 다시 찾습니다.")
            threading.Timer(10, self.update_Serial).start()

    # 포트 리스트 찾는 함수
    def serial_ports(self):
        """ Lists serial port names

            :raises EnvironmentError:
                On unsupported or unknown platforms
            :returns:
                A list of the serial ports available on the system
        """
        if sys.platform.startswith('win'):
            ports = ['COM%s' % (i + 1) for i in range(256)]
        elif sys.platform.startswith('linux') or sys.platform.startswith('cygwin'):
            # this excludes your current terminal "/dev/tty"
            ports = glob.glob('/dev/tty[A-Za-z]*')
        elif sys.platform.startswith('darwin'):
            ports = glob.glob('/dev/tty.*')
        else:
            raise EnvironmentError('Unsupported platform')

        result = []
        for port in ports:
            try:
                s = serial.Serial(port)
                s.close()
                result.append(port)
            except (OSError, serial.SerialException):
                pass
        return result

    # 통신할지 안할지 설정
    def set_Serial(self, state):
        self.state = state

    # 데이터 처리할 함수
    def __parsing_data(self, data):
        # 리스트 구조로 들어 왔기 때문에
        # 작업하기 편하게 스트링으로 합침
        tmp = ''.join(data)

        return tmp  # 출력

    # 전달 함수
    def transmit_Data(self, data):
        if self.serial_Open_State:
            print(datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S") + " (Serial) : 아두이노에게 데이터(" + data + ")를 전달합니다.")
            encode_Data = data.encode('utf-8')
            self.serial_Open.write(encode_Data)

    # 수신 함수 (쓰레드)
    def __readThread(self, receive_Data):
        # 쓰레드 종료될때까지 계속 돌림
        while self.state:
            #데이터가 있있다면
            for data in receive_Data.read():
                #line 변수에 차곡차곡 추가하여 넣는다.
                self.line.append(chr(data))

                # 데이터의 끝부분을 수신하면
                if str(data) == '}' or data == 10:
                    try:
                        # 라인의 끝을 만나면 데이터 처리 함수로 호출
                        receive_String = self.__parsing_data(self.line)
                        first_Index = receive_String.find('{')
                        last_Index = receive_String.find('}')

                        print(datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S") + " (Serial) : 아두이노에서 데이터(" + receive_String + ")를 수신했습니다.")

                        # line 변수 초기화
                        del self.line[:]

                        if receive_String[first_Index] == '{' and receive_String[last_Index] == '}':
                            receive_Json = receive_String[first_Index:last_Index+1].replace('\\n\\r', '')
                            json_data = json.loads(receive_Json)

                            # 센서 값 저장
                            house_Number = json_data["house_number"]
                            index = self.get_index(house_Number)
                            if index == -1:
                                self.set_House_Data(house_Number)
                                index = self.get_index(house_Number)

                            self.set_solar_Current(json_data["solar"])
                            self.set_battery_Remain(json_data["battery"])

                            self.set_fire_State(index, json_data["fire"])
                            self.set_gas_State(index, json_data["gas"])
                            self.set_house_Noise(index, json_data["noise"])

                            print(datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S") + " (Serial) : 데이터를 저장 완료했습니다.")
                        else:
                            print(datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S") + " (Serial) : 데이터 형식이 JSON이 아니라서 저장에 실패했습니다.")
                    except Exception as ex:
                        print(datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S") + " (Serial) : 수신 데이터를 처리하는 도중 에러가 발생하였습니다.", ex)