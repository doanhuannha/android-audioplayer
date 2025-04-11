package bluemoon.audioplayer;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class BackgroundService extends IntentService {

	public BackgroundService() {
		super("Audio player service");
		// TODO Auto-generated constructor stub
		s_current = this;
	}
	static BackgroundService s_current = null;
	private Notification noti = null;
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		
		RemoteViews view = new RemoteViews(getPackageName(), R.layout.noti_layout);
		Intent playIntent = new Intent(this, PlayerService.class);
		playIntent.setAction("bluemoon.audioplayer.intent.action.PLAYSTOP");

		view.setOnClickPendingIntent(R.id.playActor, PendingIntent.getService(this, 1, playIntent, PendingIntent.FLAG_UPDATE_CURRENT));
		
		Intent exitIntent = new Intent(this, PlayerService.class);
		exitIntent.setAction("bluemoon.audioplayer.intent.action.EXIT");
		
		view.setOnClickPendingIntent(R.id.exitActor, PendingIntent.getService(this, 2, exitIntent, PendingIntent.FLAG_UPDATE_CURRENT));
		
		//noti.contentView = view;
		noti = MainActivity.s_Current.buildNotification(R.drawable.ic_launcher, "Media player is running", view);
		startForeground(MainActivity.PLAYING_NOTIFICATION_ID, noti);
	}
	protected void updateNotification(int isPlaying){
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		if(isPlaying==-1){
			noti.contentView.setTextViewText(R.id.playActor, "►/□");
		}
		else {
			noti.contentView.setTextViewText(R.id.playActor, isPlaying>0?"  □  ":"  ►  ");
		}
		mNotificationManager.notify(MainActivity.PLAYING_NOTIFICATION_ID, noti);
	}
	@Override
	protected void onHandleIntent(Intent workIntent) {
		while(MainActivity.s_Current!=null){
			
			try {
				Thread.sleep(54321);
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				MainActivity.s_Current.showMessage(e.getMessage());
			}
			
		}
		
	}
}