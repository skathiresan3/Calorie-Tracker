
package com.example.calorietracker;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import java.util.Calendar;

public class NotificationScheduler extends BroadcastReceiver {

    private static final String CHANNEL_ID = "calorie_tracker_channel";
    private static final int LUNCH_NOTIFICATION_ID = 1;
    private static final int DINNER_NOTIFICATION_ID = 2;

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Calorie Tracker";
            String description = "Channel for Calorie Tracker notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            notificationManager.createNotificationChannel(channel);
        }

        String title = intent.getStringExtra("title");
        String message = intent.getStringExtra("message");
        int notificationId = intent.getIntExtra("notification_id", 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with your app icon
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        notificationManager.notify(notificationId, builder.build());
    }

    public static void scheduleNotifications(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Lunch Notification
        Intent lunchIntent = new Intent(context, NotificationScheduler.class);
        lunchIntent.putExtra("title", "Lunch Time!");
        lunchIntent.putExtra("message", "Time to get on app and log lunch");
        lunchIntent.putExtra("notification_id", LUNCH_NOTIFICATION_ID);
        PendingIntent lunchPendingIntent = PendingIntent.getBroadcast(context, LUNCH_NOTIFICATION_ID, lunchIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Calendar lunchCalendar = Calendar.getInstance();
        lunchCalendar.set(Calendar.HOUR_OF_DAY, 12);
        lunchCalendar.set(Calendar.MINUTE, 0);
        lunchCalendar.set(Calendar.SECOND, 0);

        // if it's already past 12 PM, schedule for tomorrow
        if (Calendar.getInstance().after(lunchCalendar)) {
            lunchCalendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, lunchCalendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, lunchPendingIntent);

        // Dinner Notification
        Intent dinnerIntent = new Intent(context, NotificationScheduler.class);
        dinnerIntent.putExtra("title", "Dinner Time!");
        dinnerIntent.putExtra("message", "Time to get on app and log dinner");
        dinnerIntent.putExtra("notification_id", DINNER_NOTIFICATION_ID);
        PendingIntent dinnerPendingIntent = PendingIntent.getBroadcast(context, DINNER_NOTIFICATION_ID, dinnerIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Calendar dinnerCalendar = Calendar.getInstance();
        dinnerCalendar.set(Calendar.HOUR_OF_DAY, 19); // 7 PM
        dinnerCalendar.set(Calendar.MINUTE, 0);
        dinnerCalendar.set(Calendar.SECOND, 0);

        // if it's already past 7 PM, schedule for tomorrow
        if (Calendar.getInstance().after(dinnerCalendar)) {
            dinnerCalendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, dinnerCalendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, dinnerPendingIntent);
    }
}
