package com.musat.smarthome;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class GetAPI extends AsyncTask<String, Void, String> {
    public static final String TAG = "API";

    // AsyncTask 실행 시작 부분
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    // AsyncTask 실행 끝 부분
    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
    }

    @Override
    protected String doInBackground(String... params) {

        // 1. PHP 파일을 실행시킬 수 있는 주소와 전송할 데이터를 준비합니다.
        // POST 방식으로 데이터 전달시에는 데이터가 주소에 직접 입력되지 않습니다.
        String serverURL = "http://ljw1354.iptime.org:8000/";

        if (!params[2].equals("")) {
            serverURL += "send/";
        }

        String postParameters = "";

        try {
            JSONObject object = new JSONObject();

            for (int i=0; i < params.length; i++) {
                if (params[i] == null) {
                    params[i] = "";
                }
            }

            object.put("fcm_key", params[0]);
            object.put("house_num", params[1]);

            if (!params[2].equals("")) {
                object.put("type", params[2]);
            }

            postParameters = object.toString();

            Log.e(TAG, "전송할 데이터(" + params[0] + " / " + params[1] + ")를 변환하였습니다.");
            Log.e(TAG, "변환된 데이터 : " + postParameters);

        } catch (JSONException e) {
            Log.e(TAG, "전송할 데이터 변환에 실패했습니다.", e);
        }


        // HTTP 메시지 본문에 포함되어 전송되기 때문에 따로 데이터를 준비해야 합니다.
        // 전송할 데이터는 “이름=값” 형식이며 여러 개를 보내야 할 경우에는 항목 사이에 &를 추가합니다.
        // 여기에 적어준 이름을 나중에 PHP에서 사용하여 값을 얻게 됩니다.

        try {
            Log.d(TAG, "API에 접속합니다.");

            // 2. HttpURLConnection 클래스를 사용하여 POST 방식으로 데이터를 전송합니다.
            URL url = new URL(serverURL); // 주소가 저장된 변수를 이곳에 입력합니다.

            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

            httpURLConnection.setReadTimeout(3000); //3초안에 응답이 오지 않으면 예외가 발생합니다.
            httpURLConnection.setConnectTimeout(3000); //3초안에 연결이 안되면 예외가 발생합니다.
            httpURLConnection.setRequestMethod("POST"); //요청 방식을 POST로 합니다.
            httpURLConnection.setDoInput(true);
            httpURLConnection.connect();

            OutputStream outputStream = httpURLConnection.getOutputStream();
            outputStream.write(postParameters.getBytes("UTF-8")); //전송할 데이터가 저장된 변수를 이곳에 입력합니다. 인코딩을 고려해줘야 합니다.

            outputStream.flush();
            outputStream.close();

            // 3. 응답을 읽습니다.
            int responseStatusCode = httpURLConnection.getResponseCode();

            Log.d(TAG, "인터넷 응답 코드 : " + responseStatusCode);

            InputStream inputStream;
            if (responseStatusCode == HttpURLConnection.HTTP_OK) {
                // 정상적인 응답 데이터
                Log.d(TAG, "정상적으로 API 접속되었습니다.");
                inputStream = httpURLConnection.getInputStream();
            } else {
                // 에러 발생
                Log.d(TAG, "API 접속을 실패했습니다.");
                inputStream = httpURLConnection.getErrorStream();
            }

            // 4. StringBuilder를 사용하여 수신되는 데이터를 저장합니다.
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            StringBuilder sb = new StringBuilder();
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }

            bufferedReader.close();

            String rx_data = sb.toString().trim();

            Log.d(TAG, "수신한 데이터 : " + rx_data);
            Log.d(TAG, "데이터를 정상적으로 수신하였습니다.");
            return rx_data;
        } catch (Exception e) {
            Log.d(TAG, "APT 접속을 실패했습니다.");
            Log.d(TAG, "에러 메시지 : " + e.toString());
            return e.toString();
        }
    }

    public apiData json_Conv(String json_String) {
        String TAG_rx_time = "time";

        String TAG_sunrise_tim = "sunrise";
        String TAG_sunset_time = "sunset";
        String TAG_weather_data = "weather";
        String TAG_temp_data = "temp";

        String TAG_fire_data = "fire";
        String TAG_gas_data = "gas";
        String TAG_noise_data = "noise";

        String TAG_solar_data = "solar";
        String TAG_battery_data = "battery";

        try {
            // JSONObject으로 변환
            JSONObject jsonObject = new JSONObject(json_String);

            // jsonObject에서 TAG_JSON 키를 갖는 JSONArray를 가져옵니다.
            String rx_time = jsonObject.getString(TAG_rx_time);

            String sunrise_time = jsonObject.getString(TAG_sunrise_tim);
            String sunset_time = jsonObject.getString(TAG_sunset_time);
            String weather_data = jsonObject.getString(TAG_weather_data);
            String temp_data = jsonObject.getString(TAG_temp_data);

            String fire_data = jsonObject.getString(TAG_fire_data);
            String gas_data = jsonObject.getString(TAG_gas_data);
            String noise_data = jsonObject.getString(TAG_noise_data);

            String solar_data = jsonObject.getString(TAG_solar_data);
            String battery_data = jsonObject.getString(TAG_battery_data);

            apiData api_Data = new apiData();

            api_Data.set_state(true);
            api_Data.set_rx_time(rx_time);

            api_Data.set_sunrise_time(sunrise_time);
            api_Data.set_sunset_time(sunset_time);
            api_Data.set_weather_data(weather_data);
            api_Data.set_temp_data(temp_data);

            api_Data.set_fire_data(fire_data);
            api_Data.set_gas_data(gas_data);
            api_Data.set_noise_data(noise_data);

            api_Data.set_solar_data(solar_data);
            api_Data.set_battery_data(battery_data);

            Log.d(TAG, "데이터를 정상적으로 처리하였습니다.");
            return api_Data;
        } catch (JSONException e) {
            apiData api_Data = new apiData();

            api_Data.set_state(false);
            api_Data.set_error_MSG(e.getMessage());
            Log.d(TAG, "데이터 처리하는데 오류가 발생하였습니다.");
            Log.d(TAG, "에러 메시지 : " + e.getMessage());
            return api_Data;
        }
    }

}
