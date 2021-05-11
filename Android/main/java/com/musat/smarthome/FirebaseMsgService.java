package com.musat.smarthome;

import android.app.Notification;
import android.net.Uri;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FirebaseMsgService extends FirebaseMessagingService {
    private static final String TAG = "FCM";

    private String title = "";
    private String body = "";

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        PreferenceManager.setString(this, "fcm_key", s);
        Log.d(TAG, "토큰이 생성 및 저장되었습니다. 토근 값 : " + s);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        //super.onMessageReceived(remoteMessage);

        // 푸시알림 메시지 분기
        //putDate를 사용했을때 data 가져오기
        if (remoteMessage.getData().size() > 0) {
            title = remoteMessage.getData().get("title");
            body = remoteMessage.getData().get("body");
            Log.d(TAG, "수신된 데이터(제목) : " + remoteMessage.getData().get("title"));
            Log.d(TAG, "수신된 데이터(내용): " + remoteMessage.getData().get("body"));

            if (true) {
                // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
                scheduleJob();
            } else {
                // Handle message within 10 seconds
                handleNow();
            }
        }

        //Notification 사용했을때 data 가져오기
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "수신된 알림(제목) : " + remoteMessage.getNotification().getTitle());
            Log.d(TAG, "수신된 알림(내용) : " + remoteMessage.getNotification().getBody());
            title = remoteMessage.getNotification().getTitle();
            body = remoteMessage.getNotification().getBody();
        }

        sendNotification(title, body);
    }

    private void scheduleJob() {
        // [START dispatch_job]
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));
        Job myJob = dispatcher.newJobBuilder()
                .setService(MyJobService.class)
                .setTag("my-job-tag")
                .build();
        dispatcher.schedule(myJob);
        // [END dispatch_job]
    }

    private void handleNow() {
        Log.d(TAG, "Short lived task is done.");
    }

    private void sendRegistrationToServer(String token) {
        // TODO: Implement this method to send token to your app server.
    }

    private void sendNotification(String messageTitle, String messageBody) {
        Uri warning_sound_Uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.warning_sound);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        Notification notification = new NotificationCompat.Builder(this, "SmartHome")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(messageTitle)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setVibrate(new long[]{1000, 3000})
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setSound(warning_sound_Uri)
                .build();

        notificationManager.notify(0, notification);
        Log.d(TAG, "알림창을 띄웠습니다.");
    }
}