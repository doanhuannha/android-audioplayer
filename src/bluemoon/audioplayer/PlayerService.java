package bluemoon.audioplayer;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class PlayerService extends Service {
	@Override
	public void onCreate() {
	    super.onCreate();
	    Log.v("DEBUG", "onCreate");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		String action = intent.getAction();
		if(action.equalsIgnoreCase("bluemoon.audioplayer.intent.action.PLAYSTOP")){
			MainActivity.s_Current.togglePlayingMedia();
			Log.v("DEBUG", "action done");
		}
		else if(action.equalsIgnoreCase("bluemoon.audioplayer.intent.action.EXIT")){
			this.stopSelf();
			MainActivity.s_Current.exit();
			Log.v("DEBUG", "exit app");
		}
	    return START_NOT_STICKY;
	 }
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
}
