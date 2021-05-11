package com.musat.smarthome;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class SubActivity extends AppCompatActivity {
    private static List<String> type_Data = Arrays.asList("fire", "gas", "noise");
    private static List<String> state_Data = Arrays.asList("좋음", "보통", "나쁨", "심각");
    private static final String TAG = "SubActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub);

        final String house_Number = PreferenceManager.getString(SubActivity.this, "set_House_Num");
        Intent intent = getIntent(); /*데이터 수신*/

        Log.d(TAG, "서브 액티비티로 들어왔습니다.");
        final String type = intent.getExtras().getString("type");
        String state = intent.getExtras().getString("state");
        Log.d(TAG, "전달 받은 데이터 (type) : " + type);
        Log.d(TAG, "전달 받은 데이터 (state) : " + state);

        // 첫번째 버튼 클릭 시 그냥 뒤로가기
        Button text_foot_btn_1 = (Button) findViewById(R.id.foot_btn_1);
        text_foot_btn_1.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view)
            {
                Log.d(TAG, "(뒤로가기) 서브 액티비티를 종료합니다.");
                onBackPressed();
            }
        });

        // 두번째 버튼 클릭 시
        Button text_foot_btn_2 = (Button) findViewById(R.id.foot_btn_2);
        text_foot_btn_2.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view)
            {
                Log.d(TAG, "(API 전송) 데이터를 전송합니다.");
                GetAPI get_API = new GetAPI();

                try {
                    String fcm_key = PreferenceManager.getString(SubActivity.this, "fcm_key");
                    get_API.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, fcm_key, house_Number, type).get();
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "(API 전송) 서브 액티비티를 종료합니다.");
                onBackPressed();
            }
        });

        // 포함 되지 않을 경우 뒤로가기
        if (!type_Data.contains(type) || !state_Data.contains(state) || house_Number == null || house_Number == "") {
            Log.d(TAG, "(전달받은 값이 없음) 서브 액티비티를 종료합니다.");
            onBackPressed();
        }

        switch (type_Data.indexOf(type)) {
            case 0 :
                // 화재
                set_fire(state);
                break;
            case 1 :
                // 가스
                set_gas(state);
                break;
            case 2:
                // 층간소음
                set_noise(state);
                break;
        }
    }

    // 화재 설정
    private void set_fire(String state) {
        Log.d(TAG, "화재 화면을 설정합니다.");
        ImageView img_head_icon = (ImageView) findViewById(R.id.head_icon);
        ImageView img_main_icon = (ImageView) findViewById(R.id.main_icon);

        img_head_icon.setImageResource(R.drawable.fire_icon);

        String head_text_1 = "";
        String head_text_2 = "";
        int color = 0;

        String main_text_1 = "";
        String main_text_2 = "";

        String foot_text_1 = "";
        String foot_text_2 = "";
        int btn_2_Visibilty = 0;

        switch (state_Data.indexOf(state)) {
            case 0 :
                // 좋음
                head_text_1 = "스마트홈은";
                head_text_2 = "화재로부터 안전합니다.";
                color = Color.parseColor("#5bc708");

                img_main_icon.setImageResource(R.drawable.safe_icon);
                main_text_1 = "안전합니다.";
                main_text_2 = "앞으로도 계속 확인해주세요.";

                foot_text_1 = "뒤로가기";
                foot_text_2 = "";
                btn_2_Visibilty = View.GONE;
                break;
            case 1 :
            case 2 :
                // 보통, 나쁨
                head_text_1 = "스마트홈에";
                head_text_2 = "화재가 감지되었습니다.";
                color = Color.parseColor("#e79c2b");

                img_main_icon.setImageResource(R.drawable.caution_icon);
                main_text_1 = "주의가 필요합니다.";
                main_text_2 = "확인해 볼 필요가 있습니다.";

                foot_text_1 = "확인해볼께요.";
                foot_text_2 = "";
                btn_2_Visibilty = View.GONE;
                break;
            case 3 :
                // 심각
                head_text_1 = "스마트홈에";
                head_text_2 = "화재가 발생했습니다.";
                color = Color.parseColor("#d14747");

                img_main_icon.setImageResource(R.drawable.siren_icon);
                main_text_1 = "119 신고하기";
                main_text_2 = "터치하면 자동으로 119 안전센터에 연결됩니다.";

                LinearLayout main_layout = (LinearLayout) findViewById(R.id.main_layout);
                main_layout.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view)
                    {
                        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:119"));
                        startActivity(intent);
                    }
                });

                foot_text_1 = "확인해볼께요.";
                foot_text_2 = "괜찮아요. 오보에요.";
                btn_2_Visibilty = View.VISIBLE;
        }
        set_View(head_text_1, head_text_2, main_text_1, main_text_2, foot_text_1, foot_text_2, color, btn_2_Visibilty);
    }

    // 가스 설정
    private void set_gas(String state) {
        Log.d(TAG, "가스 화면을 설정합니다.");
        ImageView img_head_icon = (ImageView) findViewById(R.id.head_icon);
        ImageView img_main_icon = (ImageView) findViewById(R.id.main_icon);

        img_head_icon.setImageResource(R.drawable.gas_icon);

        String head_text_1 = "";
        String head_text_2 = "";
        int color = 0;

        String main_text_1 = "";
        String main_text_2 = "";

        String foot_text_1 = "";
        String foot_text_2 = "";
        int btn_2_Visibilty = 0;

        switch (state_Data.indexOf(state)) {
            case 0 :
                // 좋음
                head_text_1 = "스마트홈은";
                head_text_2 = "가연성 가스로부터 안전합니다.";
                color = Color.parseColor("#5bc708");

                img_main_icon.setImageResource(R.drawable.safe_icon);
                main_text_1 = "안전합니다.";
                main_text_2 = "앞으로도 계속 확인해주세요.";

                foot_text_1 = "뒤로가기";
                foot_text_2 = "";
                btn_2_Visibilty = View.GONE;
                break;
            case 1 :
            case 2 :
                // 보통, 나쁨
                head_text_1 = "스마트홈에";
                head_text_2 = "가연성 가스가 감지되었습니다.";
                color = Color.parseColor("#e79c2b");

                img_main_icon.setImageResource(R.drawable.caution_icon);
                main_text_1 = "주의가 필요합니다.";
                main_text_2 = "확인해 볼 필요가 있습니다.";

                foot_text_1 = "확인해볼께요.";
                foot_text_2 = "";
                btn_2_Visibilty = View.GONE;
                break;
            case 3 :
                // 심각
                head_text_1 = "가연성 가스 수치가 높습니다.";
                head_text_2 = "환기가 필요합니다.";
                color = Color.parseColor("#d14747");

                img_main_icon.setImageResource(R.drawable.ventilation_icon);
                main_text_1 = "실내를 환기를 시키세요.";
                main_text_2 = "환기를 시키면 자동으로 수치가 내려갑니다.";

                foot_text_1 = "확인해볼께요.";
                foot_text_2 = "괜찮아요. 오보에요.";
                btn_2_Visibilty = View.VISIBLE;
        }

        set_View(head_text_1, head_text_2, main_text_1, main_text_2, foot_text_1, foot_text_2, color, btn_2_Visibilty);
    }

    // 층간소음
    private void set_noise(String state) {
        Log.d(TAG, "층간소음 화면을 설정합니다.");
        ImageView img_head_icon = (ImageView) findViewById(R.id.head_icon);
        ImageView img_main_icon = (ImageView) findViewById(R.id.main_icon);

        img_head_icon.setImageResource(R.drawable.noise_icon);

        String head_text_1 = "";
        String head_text_2 = "";
        int color = 0;

        String main_text_1 = "";
        String main_text_2 = "";

        String foot_text_1 = "";
        String foot_text_2 = "";
        int btn_2_Visibilty = 0;

        switch (state_Data.indexOf(state)) {
            case 0 :
                // 좋음
                head_text_1 = "스마트홈의";
                head_text_2 = "층간 소음 수치가 양호합니다.";
                color = Color.parseColor("#5bc708");

                img_main_icon.setImageResource(R.drawable.safe_icon);
                main_text_1 = "조용합니다.";
                main_text_2 = "앞으로도 계속 확인해주세요.";

                foot_text_1 = "뒤로가기";
                foot_text_2 = "";
                btn_2_Visibilty = View.GONE;
                break;
            case 1 :
            case 2 :
                // 보통, 나쁨
                head_text_1 = "스마트홈의";
                head_text_2 = "층간 소음 수치가 약간 높습니다.";
                color = Color.parseColor("#e79c2b");

                img_main_icon.setImageResource(R.drawable.caution_icon);
                main_text_1 = "주의가 필요합니다.";
                main_text_2 = "확인해 볼 필요가 있습니다.";

                foot_text_1 = "확인해볼께요.";
                foot_text_2 = "";
                btn_2_Visibilty = View.GONE;
                break;
            case 3 :
                // 심각
                head_text_1 = "스마트홈의";
                head_text_2 = "층간 소음 수치가 높습니다.";
                color = Color.parseColor("#d14747");

                img_main_icon.setImageResource(R.drawable.noise_warning);
                main_text_1 = "확인이 필요합니다.";
                main_text_2 = "아랫집에서 신고가 들어 올 수 있습니다.";

                foot_text_1 = "확인해볼께요.";
                foot_text_2 = "괜찮아요. 오보에요.";
                btn_2_Visibilty = View.VISIBLE;
        }

        set_View(head_text_1, head_text_2, main_text_1, main_text_2, foot_text_1, foot_text_2, color, btn_2_Visibilty);
    }

    // 화면 설정
    private void set_View(String head_text_1, String head_text_2, String main_text_1, String main_text_2, String foot_text_1, String foot_text_2, int color, int btn_2_visibilty) {
        TextView view_head_text_1 = (TextView) findViewById(R.id.head_text_1);
        TextView view_head_text_2 = (TextView) findViewById(R.id.head_text_2);

        TextView view_main_text_1 = (TextView) findViewById(R.id.main_text_1);
        TextView view_main_text_2 = (TextView) findViewById(R.id.main_text_2);

        Button foot_btn_1 = (Button) findViewById(R.id.foot_btn_1);
        Button foot_btn_2 = (Button) findViewById(R.id.foot_btn_2);


        view_head_text_1.setText(head_text_1);
        view_head_text_2.setText(head_text_2);
        view_head_text_2.setTextColor(color);

        view_main_text_1.setText(main_text_1);
        view_main_text_2.setText(main_text_2);

        foot_btn_1.setText(foot_text_1);
        foot_btn_2.setText(foot_text_2);
        foot_btn_2.setVisibility(btn_2_visibilty);
        Log.d(TAG, "화면 설정을 완료했습니다.");
    }
}