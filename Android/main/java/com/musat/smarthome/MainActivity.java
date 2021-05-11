package com.musat.smarthome;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private static Context thisContext;
    private static List<String> state_Data = Arrays.asList("좋음", "보통", "나쁨", "심각");
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "앱이 실행되었습니다.");
        thisContext = this;

        // 로딩창 띄우기
        Intent intent = new Intent(this, LoadingActivity.class);
        startActivity(intent);

        // 버튼 클릭 시 메뉴바 보이기
        ImageButton btn = (ImageButton) findViewById(R.id.imageButton);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "메뉴를 클릭하였습니다..");
                PopupMenu popup= new PopupMenu(getApplicationContext(), v); //v는 클릭된 뷰를 의미

                getMenuInflater().inflate(R.menu.option_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.menu_item1:
                                Log.d(TAG, "집 호수 설정 버튼을 클릭하였습니다.");
                                house_Num_setting();
                                break;
                            case R.id.menu_item2:
                                Log.d(TAG, "집 호수 추가/삭제 버튼을 클릭하였습니다.");
                                house_Num_Add();
                                break;
                            default:
                                break;
                        }
                        return false;
                    }
                });
                popup.show(); //Popup Menu 보이기
            }
        });

        // 새로 고침 시 행동
        final SwipeRefreshLayout mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.data_Swipe_Refresh_Layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d(TAG, "새로고침을 합니다.");
                mSwipeRefreshLayout.setRefreshing(true);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        main_View();
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                }, 1000);
            }
        });

        /*
        // 스크롤 시 바 생기는 것
        final ScrollView scrollView = (ScrollView)findViewById(R.id.data_Scroll_View);
        scrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                View action_Bar_Line = (View) findViewById(R.id.state_bar_line);
                int scroll_loc = scrollView.getScrollY();

                if (scroll_loc > 0) {
                    action_Bar_Line.setBackgroundColor(Color.parseColor("#cccccc"));
                } else {
                    action_Bar_Line.setBackgroundColor(Color.parseColor("#f9f9f9"));
                }
                return false;
            }
        });

         */

        // 클릭 이벤트 활성화
        LinearLayout warning_Fire_Layout = (LinearLayout) findViewById(R.id.warning_Fire_Layout);
        LinearLayout warning_Gas_Layout = (LinearLayout) findViewById(R.id.warning_Gas_Layout);
        LinearLayout warning_Noise_Layout = (LinearLayout) findViewById(R.id.warning_Noise_Layout);

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SubActivity.class);
                switch (v.getId()) {
                    case R.id.warning_Fire_Layout :
                        Log.d(TAG, "화재 감지 현항을 클릭했습니다.");
                        TextView fire_Value = (TextView) findViewById(R.id.warning_Fire_Value);
                        intent.putExtra("type","fire");
                        intent.putExtra("state", fire_Value.getText().toString());
                        startActivity(intent);
                        break;
                    case R.id.warning_Gas_Layout :
                        Log.d(TAG, "가연성 가스 현항을 클릭했습니다.");
                        TextView gas_Value = (TextView) findViewById(R.id.warning_Gas_Value);
                        intent.putExtra("type","gas");
                        intent.putExtra("state", gas_Value.getText().toString());
                        startActivity(intent);
                        break;
                    case R.id.warning_Noise_Layout :
                        Log.d(TAG, "우리집 층간 소음 현황을 클릭했습니다.");
                        TextView noise_Value = (TextView) findViewById(R.id.warning_Noise_Value);
                        intent.putExtra("type","noise");
                        intent.putExtra("state", noise_Value.getText().toString());
                        startActivity(intent);
                        break;
                }
            }
        };

        warning_Fire_Layout.setOnClickListener(clickListener);
        warning_Gas_Layout.setOnClickListener(clickListener);
        warning_Noise_Layout.setOnClickListener(clickListener);

        // 메인화면 구성
        main_View();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "해당 액티비티가 재개하여 새로고침을 합니다.");
        main_View();
    }

    // 집 번호 설정
    public void house_Num_setting() {
        Context mContext = getApplicationContext();
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
        AlertDialog.Builder set_dialog = new AlertDialog.Builder(this);

        final View layout;

        if (PreferenceManager.getBoolean(this, "set_House")) {
            Log.d(TAG, "집 호수가 설정 되어있습니다.");
            // 호수가 설정이 되어 있을 경우
            layout = inflater.inflate(R.layout.dialog_text,(ViewGroup) findViewById(R.id.layout_dialog_text));

            TextView textView = layout.findViewById(R.id.textView);
            textView.setText(String.format("%s호 거주자 님 안녕하세요.", PreferenceManager.getString(this, "set_House_Num")));

            set_dialog.setNeutralButton("설정 해제", new DialogInterface.OnClickListener() {
                // 설정 해제 버튼 클릭 시 동작
                public void onClick(DialogInterface dialog, int which) {
                    // 설정 해제
                    Log.d(TAG, "집 호수 설정을 해제합니다.");
                    PreferenceManager.clearHouse(thisContext);
                    Toast.makeText(getApplicationContext(), "설정 해제가 되었습니다.", Toast.LENGTH_SHORT).show();

                    main_View();
                    house_Num_setting();
                    return;
                }
            });

        } else {
            // 호수 설정이 되어있지 않을 경우
            Log.d(TAG, "집 호수가 설정 되어있지않습니다.");
            layout = inflater.inflate(R.layout.dialog_house_reg,(ViewGroup) findViewById(R.id.layout_dialog_house_reg));

            set_dialog.setNeutralButton("설정", new DialogInterface.OnClickListener() {
                // 설정 버튼 클릭 시 동작
                public void onClick(DialogInterface dialog, int which) {
                    Log.d(TAG, "설정 버튼을 클릭했습니다.");
                    EditText textNum = layout.findViewById(R.id.house_num);
                    EditText textPW = layout.findViewById(R.id.house_pw);

                    String saveNum = textNum.getText().toString();
                    String savePW = textPW.getText().toString();

                    if (textNum.length() == 0) {
                        // 호수가 칸이 빈칸 이면
                        Toast.makeText(getApplicationContext(), "호수를 입력하세요.", Toast.LENGTH_SHORT).show();
                        house_Num_setting(); // 다이얼로그(팝업창) 실행
                        return;
                    } else if (textPW.length() == 0) {
                        // 비밀번호 칸이 빈칸 이면
                        Toast.makeText(getApplicationContext(), "비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show();
                        house_Num_setting(); // 다이얼로그(팝업창) 실행
                        return;
                    }

                    if (!PreferenceManager.getStringAES(thisContext, saveNum).equals(savePW)) {
                        // 비밀번호가 틀릴 경우
                        Toast.makeText(getApplicationContext(), "비밀번호가 틀렸습니다.", Toast.LENGTH_SHORT).show();
                        house_Num_setting(); // 다이얼로그(팝업창) 실행
                        return;
                    }

                    // 호수 설정
                    PreferenceManager.setHouse(thisContext, saveNum);
                    Toast.makeText(getApplicationContext(), "설정이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "설정이 완료되었습니다.");

                    main_View();
                    house_Num_setting(); // 다이얼로그(팝업창) 실행
                    return;
                }
            });
        }

        set_dialog.setPositiveButton("닫기", new DialogInterface.OnClickListener() {
            // 닫기 버튼 클릭 시 동작
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        set_dialog.setTitle("집 번호 설정"); // 다이얼로그(팝업창) 제목 설정
        set_dialog.setView(layout); // 다이얼로그(팝업창) 화면 추가

        // 다이얼로그(팝업창 표시)
        AlertDialog ad = set_dialog.create();
        ad.show();
    }

    // 집 번호 추가
    public void house_Num_Add() {
        Context mContext = getApplicationContext();
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
        AlertDialog.Builder set_dialog = new AlertDialog.Builder(this);

        final View layout = inflater.inflate(R.layout.dialog_house_reg,(ViewGroup) findViewById(R.id.layout_dialog_house_reg));

        set_dialog.setNeutralButton("추가", new DialogInterface.OnClickListener() {
            // 설정 버튼 클릭 시 동작
            public void onClick(DialogInterface dialog, int which) {
                EditText textNum = layout.findViewById(R.id.house_num);
                EditText textPW = layout.findViewById(R.id.house_pw);

                String saveNum = textNum.getText().toString();
                String savePW = textPW.getText().toString();

                if (textNum.length() == 0) {
                    // 호수가 칸이 빈칸 이면
                    Toast.makeText(getApplicationContext(), "호수를 입력하세요.", Toast.LENGTH_SHORT).show();
                    house_Num_Add(); // 다이얼로그(팝업창) 실행
                    return;
                } else if (textPW.length() == 0) {
                    // 비밀번호 칸이 빈칸 이면
                    Toast.makeText(getApplicationContext(), "비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show();
                    house_Num_Add(); // 다이얼로그(팝업창) 실행
                    return;
                }

                // 호수 저장
                PreferenceManager.addHouse(thisContext, saveNum, savePW);
                Toast.makeText(getApplicationContext(), "호수 데이터 저장이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "호수 데이터 저장이 완료되었습니다.");

                house_Num_Add(); // 다이얼로그(팝업창) 실행
                return;
            }
        });

        set_dialog.setNegativeButton("삭제", new DialogInterface.OnClickListener() {
            // 삭제 버튼 클릭 시 동작
            public void onClick(DialogInterface dialog, int which) {
                EditText textNum = layout.findViewById(R.id.house_num);
                EditText textPW = layout.findViewById(R.id.house_pw);

                String saveNum = textNum.getText().toString();
                String savePW = textPW.getText().toString();

                if (textNum.length() == 0) {
                    // 호수가 칸이 빈칸 이면
                    Toast.makeText(getApplicationContext(), "호수를 입력하세요.", Toast.LENGTH_SHORT).show();
                    house_Num_Add(); // 다이얼로그(팝업창) 실행
                    return;
                } else if (textPW.length() == 0) {
                    // 비밀번호 칸이 빈칸 이면
                    Toast.makeText(getApplicationContext(), "비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show();
                    house_Num_Add(); // 다이얼로그(팝업창) 실행
                    return;
                }

                if (!PreferenceManager.getStringAES(thisContext, saveNum).equals(savePW)) {
                    // 비밀번호가 틀릴 경우
                    Toast.makeText(getApplicationContext(), "비밀번호가 틀렸습니다.", Toast.LENGTH_SHORT).show();
                    house_Num_Add(); // 다이얼로그(팝업창) 실행
                    return;
                }

                // 호수 삭제
                PreferenceManager.delHouse(thisContext, saveNum);
                Toast.makeText(getApplicationContext(), "해당 호수 데이터 삭제가 완료되었습니다.", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "해당 호수 데이터 삭제가 완료되었습니다.");
            }
        });

        set_dialog.setPositiveButton("닫기", new DialogInterface.OnClickListener() {
            // 닫기 버튼 클릭 시 동작
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        set_dialog.setTitle("집 번호 추가/삭제"); // 다이얼로그(팝업창) 제목 설정
        set_dialog.setView(layout); // 다이얼로그(팝업창) 화면 추가

        // 다이얼로그(팝업창 표시)
        AlertDialog ad = set_dialog.create();
        ad.show();
    }

    public void main_View() {
        Log.d(TAG, "화면을 구성합니다.");
        boolean house_State = PreferenceManager.getBoolean(this, "set_House");
        String house_Number = "";

        TextView text_Hello = (TextView) findViewById(R.id.houseInfo_Text_Hello);
        TextView text_Num = (TextView) findViewById(R.id.houseInfo_Text_Num);
        TextView text_Resident = (TextView) findViewById(R.id.houseInfo_Text_Resident);

        if (house_State) {
            // 집 설정이 되어있으면
            text_Hello.setText("안녕하세요");

            house_Number = PreferenceManager.getString(this, "set_House_Num");

            text_Num.setText(house_Number + "호");
            text_Num.setTextSize(40);

            text_Resident.setText("거주자 님");
            text_Resident.setTextSize(15);
        } else {
            // 집 설정이 되어있지않으면
            text_Hello.setText("");

            text_Num.setText("설정이 필요합니다.");
            text_Num.setTextSize(24);

            text_Resident.setText("좌측 상단에서 설정가능합니다.");
            text_Resident.setTextSize(15);
        }


        GetAPI get_API = new GetAPI();
        apiData api_Data = new apiData();

        try {
            String fcm_key = PreferenceManager.getString(this, "fcm_key");
            String api_Result = get_API.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, fcm_key, house_Number, "").get();
            api_Result = api_Result.replaceAll("\\\\", "");
            api_Result = api_Result.substring(1, api_Result.length()-1);

            api_Data = get_API.json_Conv(api_Result);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();

            api_Data.set_state(false);
            api_Data.set_error_MSG(e.getMessage());
        }

        TextView text_weather_Sunrise_Value = (TextView) findViewById(R.id.weather_Sunrise_Value);
        TextView text_weather_Sunset_Value = (TextView) findViewById(R.id.weather_Sunset_Value);
        TextView text_weather_State_Value = (TextView) findViewById(R.id.weather_State_Value);
        TextView text_weather_Temp_Value = (TextView) findViewById(R.id.weather_Temp_Value);

        TextView text_warning_Fire_Value = (TextView) findViewById(R.id.warning_Fire_Value);
        TextView text_warning_Gas_Value = (TextView) findViewById(R.id.warning_Gas_Value);
        TextView text_warning_Noise_Value = (TextView) findViewById(R.id.warning_Noise_Value);

        TextView text_power_Current_Value = (TextView) findViewById(R.id.power_Current_Value);
        TextView text_power_Battery_Value = (TextView) findViewById(R.id.power_Battery_Value);


        TextView text_update_time_Value = (TextView) findViewById(R.id.data_Update_Text);

        if (api_Data.get_state()) {
            // 정상적으로 API 접속이 완료되었으면
            text_weather_Sunrise_Value.setText(api_Data.set_Data(api_Data.get_sunrise_time()));
            text_weather_Sunset_Value.setText(api_Data.set_Data(api_Data.get_sunset_time()));
            text_weather_State_Value.setText(api_Data.get_weather_data());
            text_weather_Temp_Value.setText(api_Data.get_temp_data());

            text_warning_Fire_Value.setText(set_Data_Color(api_Data.get_fire_data()));
            text_warning_Gas_Value.setText(set_Data_Color(api_Data.get_gas_data()));
            text_warning_Noise_Value.setText(set_Data_Color(api_Data.get_noise_data()));

            text_power_Current_Value.setText(api_Data.get_solar_data());
            text_power_Battery_Value.setText(api_Data.get_battery_data());

            text_update_time_Value.setText("데이터 갱신 시간 : " + api_Data.get_rx_time());
        } else {
            // API 접속에 실패한 경우
            Toast.makeText(getApplicationContext(), "API 접속에 실패했습니다.", Toast.LENGTH_SHORT).show();

            text_weather_Sunrise_Value.setText("");
            text_weather_Sunset_Value.setText("");
            text_weather_State_Value.setText("");
            text_weather_Temp_Value.setText("");

            text_warning_Fire_Value.setText("");
            text_warning_Gas_Value.setText("");
            text_warning_Noise_Value.setText("");

            text_power_Current_Value.setText("");
            text_power_Battery_Value.setText("");

            text_update_time_Value.setText("데이터 갱신 시간 : 갱신 실패");
        }

        // 101호면 우리집 층간 소음 현황 제거
        ImageView warning_Noise_Line = (ImageView) findViewById(R.id.warning_Noise_Line);
        LinearLayout warning_Noise_Layout = (LinearLayout) findViewById(R.id.warning_Noise_Layout);

        if (house_Number.equals("101")) {
            // 101호이면 우리집 층간 소음 현황 제거
            warning_Noise_Line.setVisibility(View.GONE);
            warning_Noise_Layout.setVisibility(View.GONE);
        } else {
            warning_Noise_Line.setVisibility(View.VISIBLE);
            warning_Noise_Layout.setVisibility(View.VISIBLE);
        }
    }

    // 색상 설정
    private SpannableString set_Data_Color(String text_Data) {
        SpannableString spannableString = new SpannableString(text_Data);

        int start = 0;
        int end = text_Data.length();
        String color = "#707070";

        switch (state_Data.indexOf(text_Data)) {
            case 0 :
                 // 파란색
                color = "#4fa7ff";
                break;
            case 1 :
                //녹색
                color = "#5bc708";
                break;
            case 2:
                //주황색
                color = "#e79c2b";
                break;
            case 3:
                color = "#d14747";
                break;
        }
        spannableString.setSpan(new ForegroundColorSpan(Color.parseColor(color)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableString;
    }
}