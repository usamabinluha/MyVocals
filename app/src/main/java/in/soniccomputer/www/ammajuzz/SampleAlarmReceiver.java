package in.soniccomputer.www.ammajuzz;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;

import com.google.android.vending.expansion.downloader.DownloaderClientMarshaller;

/**
 * You should start your derived downloader class when this receiver gets the message
 * from the alarm service using the provided service helper function within the
 * DownloaderClientMarshaller. This class must be then registered in your AndroidManifest.xml
 * file with a section like this:
 *         <receiver android:name=".SampleAlarmReceiver"/>
 */
public class SampleAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            DownloaderClientMarshaller.startDownloadServiceIfRequired(context, intent, SampleDownloaderService.class);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
    }

}
