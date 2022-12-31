package com.masrudyn.prayertimemanager;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.StrictMode;
import android.telephony.SmsManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AlarmReceiver extends BroadcastReceiver {

    private static Pattern patternDomainName;
    private Matcher matcher;
    private static final String DOMAIN_NAME_PATTERN
            = "([a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,6}";
    static {
        patternDomainName = Pattern.compile(DOMAIN_NAME_PATTERN);
    }


    @Override
    public void onReceive(Context context, Intent intent) {

        Intent i = new Intent(context, DestinationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,0,i,0);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Set<String> prayerTimeTomorrow = getDataFromGoogle("singapore prayer time tomorrow");
        sendSMS(prayerTimeTomorrow.toString());

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,"prayerTimeManager")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("Prayer Time Manager")
                .setContentText(prayerTimeTomorrow.toString())
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(123,builder.build());

    }

    private String getDomainName(String url){

        String domainName = "";
        matcher = patternDomainName.matcher(url);
        if (matcher.find()) {
            domainName = matcher.group(0).toLowerCase().trim();
        }
        return domainName;

    }

    private Set<String> getDataFromGoogle(String query) {

        Set<String> result = new HashSet<String>();
        String request = "https://www.google.com/search?q=" + query + "&num=20";
        Log.d("getDataFromGoogle..", request);

        try {

            // need http protocol, set this as a Google bot agent :)
            Document doc = Jsoup
                    .connect(request)
                    .userAgent(
                            "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)")
                    .timeout(5000).get();

            Elements allElements= doc.getAllElements();
            for (Element element : allElements) {
                if (element.text().startsWith("Islamic prayer times Singapore")) {
                    System.out.println("className: " + element.className());
                    System.out.println("text(): " + element.text().substring(0,element.text().indexOf("change GMT")-2));
                    result.add(element.text().substring(0,element.text().indexOf("change GMT")-2));
                    break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    private void sendSMS(String smsText) {

        ArrayList<String> recipients = new ArrayList<String>(Arrays.asList("96685594", "90483486", "97494363", "88000087", "98828808" ));

        try {

            // on below line we are initializing sms manager.
            //as after android 10 the getDefault function no longer works
            //so we have to check that if our android version is greater
            //than or equal toandroid version 6.0 i.e SDK 23
            SmsManager smsManager = SmsManager.getDefault();
            ArrayList<String> parts = smsManager.divideMessage(smsText);
            String recipient;

            for (int index=0; index < recipients.size(); index++) {

                recipient = recipients.get(index);
                // on below line we are sending text message.
                //smsManager.sendTextMessage(recipient, null, smsText, null, null);
                smsManager.sendMultipartTextMessage(recipient, null, parts, null, null);
            }

            Log.d("Called SMS Manager", "doWork: SMS API called!");

        } catch (Exception e) {

            Log.d("Exception sending SMS ", e.getMessage());

        }

    }
}
