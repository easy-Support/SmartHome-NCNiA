package com.musat.smarthome;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;

public class App extends Application {
    public static final String my_Channel = "SmartHome";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannels();
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notification_Channel = new NotificationChannel(
                    my_Channel,
                    "SmartHome",
                    NotificationManager.IMPORTANCE_HIGH
            );
            notification_Channel.setDescription("SmartHome");

            Uri warning_sound_Uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.warning_sound);

            notification_Channel.setVibrationPattern(new long[]{ 1000, 1000, 1000, 1000, 1000, 1000, 1000});

            notification_Channel.enableVibration(true);
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build();
            notification_Channel.setSound(warning_sound_Uri, audioAttributes);

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(notification_Channel);
        }
    }

}
