package in.soniccomputer.www.ammajuzz;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Constants {
    public interface ACTION {
        public static String MAIN_ACTION = "in.soniccomputer.www.ammajuzz.action.main";
        public static String INIT_ACTION = "in.soniccomputer.www.ammajuzz.action.init";
        public static String PREV_ACTION = "in.soniccomputer.www.ammajuzz.action.prev";
        public static String PLAY_ACTION = "in.soniccomputer.www.ammajuzz.action.play";
        public static String NEXT_ACTION = "in.soniccomputer.www.ammajuzz.action.next";
        public static String STARTFOREGROUND_ACTION = "in.soniccomputer.www.ammajuzz.action.startforeground";
        public static String STOPFOREGROUND_ACTION = "in.soniccomputer.www.ammajuzz.action.stopforeground";

    }

    public interface NOTIFICATION_ID {
        public static int FOREGROUND_SERVICE = 1;
    }

    public static Bitmap getDefaultAlbumArt(Context context) {
        Bitmap bm = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        try {
            bm = BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.default_album_art, options);
        } catch (Error ee) {
        } catch (Exception e) {
        }
        return bm;
    }

}