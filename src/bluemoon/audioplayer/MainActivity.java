package bluemoon.audioplayer;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.RemoteViews;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.FrameLayout.LayoutParams;
import android.widget.TextView;
import bluemoon.framework.ui.*;
import bluemoon.framework.ui.AudioPlayer.IPlayerStateListener;
import bluemoon.framework.ui.ListView.IOnItemSelected;

public class MainActivity extends BaseActivity implements OnClickListener,
		FileListView.IOnFileSelected, IOnItemSelected,
		FileListView.IOnSwitchMode {
	public static final int VIEW_MAIN = 0x1;
	public static final int VIEW_LISTFILE = 0x2;
	public static final int VIEW_YTB = 0x3;

	private static final int BT_ADD = 0x1;
	private static final int BT_LOAD = 0x2;
	private static final int BT_SAVE = 0x3;
	private static final int BT_EXIT = 0x4;
	private static final int BT_CLEAR = 0x5;
	private static final int BT_YTB = 0x6;
	private static final int BT_SFL = 0x7;

	private static final int EXIT_NONE = 0x0;
	private static final int EXIT_FINISH_LIST = 0x1;
	private static final int EXIT_TIMEOUT = 0x2;

	static final int PLAYING_NOTIFICATION_ID = 0x101;

	static MainActivity s_Current = null;

	private FileListView _fileListView;
	private AudioFileListView _fileList;
	private AudioPlayer _player;
	private LinearLayout _mainLayout, _toolbar;
	private int _exitMode = EXIT_NONE, _timeoutDuration = 0;
	private PlayListDB _playlist;
	
	private Timer _timer;
	private TextView _timeCounter;
	// FrameLayout _webView;
	// YoutubeView _ytbViewer;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//

		s_Current = this;
		_playlist = new PlayListDB(this);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.setMargins(3, 3, 3, 3);
		layoutParams.gravity = Gravity.CENTER;

		_toolbar = new LinearLayout(0x0, this);
		_toolbar.setLayoutParams(layoutParams);
		_toolbar.setOrientation(LinearLayout.HORIZONTAL);
		Button bt = null;
		bt = new Button(this);
		bt.setText(" YTB ");
		bt.setId(BT_YTB);
		bt.getBackground().setColorFilter(Color.argb(0xFF, 255, 0x6A, 34), PorterDuff.Mode.MULTIPLY);
		bt.setOnClickListener(this);
		bt.setLayoutParams(layoutParams);
		_toolbar.addView(bt);
		
		bt = new Button(this);
		bt.setText(" Add ");
		bt.setId(BT_ADD);
		bt.getBackground().setColorFilter(Color.argb(0xFF, 34, 139, 34), PorterDuff.Mode.MULTIPLY);
		bt.setOnClickListener(this);
		bt.setLayoutParams(layoutParams);
		_toolbar.addView(bt);
		
		bt = new Button(this);
		bt.setText("Clear");
		bt.setId(BT_CLEAR);
		bt.getBackground().setColorFilter(Color.argb(0xFF, 34, 139, 34), PorterDuff.Mode.MULTIPLY);
		bt.setOnClickListener(this);
		bt.setLayoutParams(layoutParams);
		_toolbar.addView(bt);
		
		bt = new Button(this);
		bt.setText("Load");
		bt.setId(BT_LOAD);
		bt.getBackground().setColorFilter(Color.argb(0xFF, 34, 139, 34), PorterDuff.Mode.MULTIPLY);
		bt.setOnClickListener(this);
		bt.setLayoutParams(layoutParams);
		bt.setVisibility(View.GONE);
		_toolbar.addView(bt);
		
		bt = new Button(this);
		bt.setText("Save");
		bt.setId(BT_SAVE);
		bt.getBackground().setColorFilter(Color.argb(0xFF, 34, 139, 34), PorterDuff.Mode.MULTIPLY);
		bt.setOnClickListener(this);
		bt.setLayoutParams(layoutParams);
		bt.setVisibility(View.GONE);
		_toolbar.addView(bt);
		
		bt = new Button(this);
		bt.setText("  ↑↓  ");
		bt.setId(BT_SFL);
		bt.getBackground().setColorFilter(Color.argb(0xFF, 225, 193, 110), PorterDuff.Mode.MULTIPLY);
		bt.setOnClickListener(this);
		bt.setLayoutParams(layoutParams);
		_toolbar.addView(bt);

		bt = new Button(this);
		bt.setText(" Quit ");
		bt.setId(BT_EXIT);
		bt.getBackground().setColorFilter(Color.argb(0xFF, 205, 0, 0), PorterDuff.Mode.MULTIPLY);
		bt.setOnClickListener(this);
		bt.setLayoutParams(layoutParams);
		_toolbar.addView(bt);
		

		_player = new AudioPlayer(this);
		_player.setVisibilityButtons(AudioPlayer.BT_RECORD, View.GONE);
		_player.setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT));
		_player.setCompletedListener(new MediaPlayer.OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				if (!playNextItem()) {
					if (_exitMode == EXIT_FINISH_LIST) {
						_player.stop();
						exit();
					} else {
						ListViewItem[] items = _fileList.getItems();
						if (_currentPlayItem != null)
							_currentPlayItem.setSelected(false);
						_player.play(items[0].getValue().toString());
						_currentPlayItem = items[0];
						_currentPlayItem.setSelected(true);
						_fileList.refresh();
					}
				}
			}
		});
		_player.setPlayerStateListener(new IPlayerStateListener() {
			
			@Override
			public void onStateChange(AudioPlayer controller, int oldState, int newState) {
				// TODO Auto-generated method stub
				if(BackgroundService.s_current!=null) BackgroundService.s_current.updateNotification(newState==AudioPlayer.PLAYER_PLAYING?1:0);
				
			}
		});
		_player.setActivity(this);
		_fileList = new AudioFileListView(0x0, this);
		_fileList.setBackgroundColor(Color.WHITE);
		_fileList.setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT, 1));
		_fileList.setOnItemSelectedListener(this);
		_fileList.setOnSwitchModeListener(this);
		_fileList.setShowSelectedItems(true);
		_fileList.setAllowSelectingMode(true);

		_mainLayout = new LinearLayout(VIEW_MAIN, this);
		_mainLayout.setBackgroundColor(Color.DKGRAY);
		_mainLayout.setOrientation(LinearLayout.VERTICAL);
		_mainLayout.addView(_toolbar);
		_mainLayout.addView(_fileList);
		_mainLayout.addView(_player);

		// _fileListView = new FileListView(VIEW_LISTFILE, this,
		// Environment.getExternalStorageDirectory().getPath(),this,null);
		_fileListView = new FileListView(VIEW_LISTFILE, this, null, this, null);
		_fileListView.setDisableBackButton(true);

		String[] s = _playlist.getTempPlayList();
		if (s != null) {
			for (int i = 0; i < s.length; i++) {
				_fileList.addItem(new ListViewItem(s[i], s[i].substring(s[i].lastIndexOf("/") + 1), null));
			}
		}
		s = _playlist.getLastMedia();
		if (s != null) {
			ListViewItem[] items = _fileList.getItems();
			for (int i = 0; i < items.length; i++) {
				if (s[0].compareTo(items[i].getValue().toString()) == 0) {
					_player.setMediaFilePath(items[i].getValue().toString());
					// _player.play();
					// _player.seekTo(Integer.parseInt(s[1]));
					// _player.pause();
					_currentPlayItem = items[i];
					_currentPlayItem.setSelected(true);
					_fileList.refresh();
				}
			}
		}
		
		Intent intent = new Intent(this, BackgroundService.class);
		startService(intent);
		//addNotification(R.drawable.ic_launcher, "Audio player is running...");
		
		intent = getIntent();
		doNewIntent(intent);
		
		FrameLayout.LayoutParams layout = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,FrameLayout.LayoutParams.WRAP_CONTENT);
		layout.gravity = Gravity.BOTTOM | Gravity.RIGHT;
		int size = getSize(10);
		layout.setMargins(0, 0, size, size);
		GradientDrawable gd = new GradientDrawable();
        gd.setColor(Color.LTGRAY);
        gd.setCornerRadius(size);
        gd.setStroke(1, 0xFF000000);
		_timeCounter = new TextView(this);
		_timeCounter.setTextSize(TypedValue.COMPLEX_UNIT_PX, size+size/2);
		_timeCounter.setTextColor(Color.RED);
		_timeCounter.setBackground(gd);
		_timeCounter.setLayoutParams(layout);
		_timeCounter.setGravity(Gravity.CENTER);
		_timeCounter.setText("00:00");
		_timeCounter.setVisibility(View.INVISIBLE);
		_timeCounter.setPadding(size, size, size, size);
		registerExtraLayout(_timeCounter);
		
		viewMainScreen();
		
	}
	private void doNewIntent(Intent intent){
		String ytb_Param = intent.getStringExtra(Intent.EXTRA_TEXT);
		intent.removeExtra(Intent.EXTRA_TEXT);
		if (ytb_Param != null) {
			openYtbWebPlayer(ytb_Param);
		}
	}
	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
		doNewIntent(intent);
	}	

	private void viewMainScreen() {
		setContentView(_mainLayout);
	}

	private void viewSelectFileScreen() {
		setContentView(_fileListView);
	}

	private String[] convertToStringArray(ListViewItem[] items) {
		if (items == null)
			return null;
		String[] s = new String[items.length];
		for (int i = 0; i < s.length; i++) {
			s[i] = items[i].getValue().toString();
		}
		return s;

	}

	protected void exit() {
		cancelNotification();
		_playlist.saveTempPlayList(convertToStringArray(_fileList.getItems()));
		if (_player.getMediaFilePath() != null) {
			_playlist.saveCurrentMedia(_player.getMediaFilePath(), _player
					.getCurrentState() == AudioPlayer.PLAYER_READY ? 0
					: _player.getCurrentPosition());
		}
		/*
		 * else { _playlist.saveCurrentMedia(null, 0); }
		 */
		finish();
		System.exit(0);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		_player.hideVideo();
		super.onPause();
	}

	@Override
	protected boolean onBackPressed(int viewId) {
		switch (viewId) {
		case VIEW_LISTFILE:
			viewMainScreen();
			return true;
		}
		onPause();
		moveTaskToBack(true);
		return true;
	}

	protected void backToMain() {
		// showMessage("Go back to main");
		try {
			
			viewMainScreen();
			new Handler().postDelayed(new Runnable() {
		        @Override
		        public void run() {
		        	BackgroundService.s_current.updateNotification(-1);
		        }
		    }, 500);
			
		} catch (Exception ex) {
			showMessage(ex.getMessage(), 5000);
		}

	}

	protected void exitFromWeb() {
		if (_exitMode == EXIT_FINISH_LIST) {
			exit();
		}
	}

	private void openYtbWebPlayer(String params) {
		// if(playingWeb) viewMainScreen();
		if(getContentViewId()==VIEW_YTB){
			FrameLayout webView = (FrameLayout) getContentView();
			YoutubeView ytbViewer = (YoutubeView)webView.getChildAt(0);
			if (params != null) ytbViewer.playYtb("https://youtube.com/watch?"+params.replace("p=", "list="));
		}
		else{
			_player.stop();
			_player.setMediaFilePath(null);
			FrameLayout webView = new FrameLayout(VIEW_YTB, this);
			YoutubeView ytbViewer = new YoutubeView(this);
			ytbViewer.setLayoutParams(new FrameLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT,
					Gravity.FILL));
			webView.addView(ytbViewer);
			if (params == null)
				ytbViewer.loadUrl(YoutubeView.APP_URL);
			else
				ytbViewer.loadUrl(YoutubeView.APP_URL+"?" + params);
			setContentView(webView);
		}
		
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v instanceof Button) {
			Button bt = (Button) v;
			switch (bt.getId()) {
				case BT_EXIT: {
					openExitOptions();
				}
				break;
				case BT_ADD: {
					viewSelectFileScreen();
				}
				break;
				case BT_CLEAR: {
					_fileList.clearItems();
					_fileList.refresh();
					_player.stop();
					_player.setMediaFilePath(null);
				}
				break;
				case BT_YTB: {
					openYtbWebPlayer(null);
				}
				break;
				case BT_SFL: {
					_fileList.shuffle();
				}
				break;
			}
		} else {

		}
	}

	@Override
	public void onFileSelected(FileListView sender, String filePath,
			String fileName) {
		_fileList.addItem(fileName, filePath);
		viewMainScreen();
	}

	public void onFilesSelected(FileListView sender, boolean[] isFolders,
			String[] filePaths, String[] fileNames) {
		for (int i = 0; i < filePaths.length; i++) {
			if (isFolders[i]) {
				List<String> files = FileListView.getFiles(filePaths[i]);
				if (files != null) {
					int count = files.size();
					for (int b = 0; b < count; b++) {
						String f = files.get(b);
						_fileList.addItem(f.substring(f.lastIndexOf("/") + 1),
								f);
					}
				}
			} else {
				_fileList.addItem(fileNames[i], filePaths[i]);
			}
		}
		viewMainScreen();
	}

	boolean togglePlayingMedia(){
		if(getContentViewId()==VIEW_YTB){
			FrameLayout webView = (FrameLayout) getContentView();
			YoutubeView ytbViewer = (YoutubeView)webView.getChildAt(0);
			return ytbViewer.togglePlayingMedia();
		}
		else{
			if(_player.getCurrentState()==AudioPlayer.PLAYER_PLAYING){
				_player.pause();
				return false;
			}
			else if(_player.getCurrentState()==AudioPlayer.PLAYER_PAUSE){
				_player.resume();
				return true;
			}
			else{
				_player.play();
				return true;
			}
			
		}
	}
	
	boolean playNextItem() {
		ListViewItem[] items = _fileList.getItems();
		boolean foundCurrentItem = false;
		for (int i = 0; i < items.length; i++) {
			if (foundCurrentItem) {
				if (_currentPlayItem != null)
					_currentPlayItem.setSelected(false);
				_player.play(items[i].getValue().toString());
				_currentPlayItem = items[i];
				_currentPlayItem.setSelected(true);
				_fileList.refresh();
				return true;
			}
			if (items[i] == _currentPlayItem) {
				foundCurrentItem = true;
			}
		}
		return false;// no next item;
	}

	ListViewItem _currentPlayItem = null;

	@Override
	public void onItemSelected(ListView sender, ListViewItem item) {
		// TODO Auto-generated method stub
		if (_currentPlayItem != null) {
			_currentPlayItem.setSelected(false);

		}
		if (_currentPlayItem == item) {
			_player.stop();
			_player.setMediaFilePath(null);
			_currentPlayItem = null;
		} else {
			_player.play(item.getValue().toString());
			_currentPlayItem = item;
		}
	}

	@Override
	public boolean onItemLongSelected(ListView sender, ListViewItem item) {
		// _player.stop();
		// _player.setAudioFile(null);
		return false;
	}

	@Override
	public void onItemsSelected(ListView sender, ListViewItem[] items) {
		if (items == null)
			return;
		for (int i = 0; i < items.length; i++) {
			ListViewItem item = items[i];
			if (_currentPlayItem == item) {
				_player.stop();
				_player.setMediaFilePath(null);
				_currentPlayItem = null;
			}
			_fileList.removeItem(item);
		}
		/*
		 * if(_currentPlayItem!=null){ _currentPlayItem.setSelected(true); }
		 */
		_fileList.refresh();
	}

	@Override
	public void onModeSwitched(ListView sender, boolean selectingMode) {
		// TODO Auto-generated method stub
		if (!selectingMode) {
			if (_currentPlayItem != null) {
				_currentPlayItem.setSelected(true);
			}
		}

	}
	private int _coundownTimer = 0;
	private boolean _shouldIgnoreDropdown;
	void openExitOptions() {

		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Exit options");
		alert.setMessage("Select an option:");

		// Set an EditText view to get user input
		final TableLayout layout = new TableLayout(this);
		TableRow row = null;

		final RadioButton exitNowOpt = new RadioButton(this);
		exitNowOpt.setText("Now");
		exitNowOpt.setTextColor(Color.WHITE);
		exitNowOpt.setGroup("me");
		exitNowOpt.setChecked(_exitMode == EXIT_NONE);
		row = new TableRow(this);
		row.addView(exitNowOpt);
		layout.addView(row);

		final RadioButton finishListOpt = new RadioButton(this);
		finishListOpt.setText("Finish list");
		finishListOpt.setTextColor(Color.WHITE);
		finishListOpt.setGroup("me");
		finishListOpt.setChecked(_exitMode == EXIT_FINISH_LIST);
		row = new TableRow(this);
		row.addView(finishListOpt);
		layout.addView(row);
		alert.setView(layout);

		final RadioButton timeoutOpt = new RadioButton(this);
		timeoutOpt.setText("After");
		timeoutOpt.setTextColor(Color.WHITE);
		timeoutOpt.setGroup("me");
		timeoutOpt.setChecked(_exitMode == EXIT_TIMEOUT);
		final Dropdown minuteOpt = new Dropdown(this);
		//minuteOpt.addItem(1,"test");
		for (int i = 1; i < 12; i++) {
			minuteOpt.addItem(i * 5, i * 5 + " minutes");
		}
		minuteOpt.addItem(60, "1 hours");
		minuteOpt.addItem(90, "1.5 hours");
		minuteOpt.addItem(120, "2 hours");
		
		
		minuteOpt.refresh();

		if (timeoutOpt.isChecked()) {
			minuteOpt.setSelectedValue(_timeoutDuration);
		}
		_shouldIgnoreDropdown = true;
		minuteOpt.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				if(_shouldIgnoreDropdown) {
					_shouldIgnoreDropdown = false;
					return;
				}
				timeoutOpt.setChecked(true);
				finishListOpt.setChecked(false);
				exitNowOpt.setChecked(false);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {}
			
		});
		//*/
		row = new TableRow(this);
		row.addView(timeoutOpt);
		row.addView(minuteOpt);
		layout.addView(row);
		alert.setView(layout);

		alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				if (_timer != null) _timer.cancel();
				if (exitNowOpt.isChecked()) exit();
				else if (finishListOpt.isChecked()) setExitMode(0);
				else if (timeoutOpt.isChecked()) {
					ListViewItem item = (ListViewItem) minuteOpt.getSelectedItem();
					_timeoutDuration = (Integer) item.getValue();
					setExitMode(_timeoutDuration);
				}
			}
		});

		alert.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Canceled.
					}
				});

		alert.show();
	}
	void setExitMode(int afterMinute){
		if(_timer!=null) {
			_timer.cancel();
			_timer = null;
		}
		_timeCounter.setVisibility(View.INVISIBLE);
		if(afterMinute==0){
			showMessage("Player will stop after finishing the current list");
			_exitMode = EXIT_FINISH_LIST;
		}
		else{
			showMessage("Player will stop after "+ afterMinute + " minutes");
			_coundownTimer = afterMinute* 60;
			_timeCounter.setText(formatTime(_coundownTimer));
			_timeCounter.setVisibility(View.VISIBLE);
			_exitMode = EXIT_TIMEOUT;
			_timer = new Timer();
			_timer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					_coundownTimer--;
			        
			        runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							_timeCounter.setText(formatTime(_coundownTimer));
						}
					});
					
					if(_coundownTimer==0) exit();
				}
			}, 0, 1000);
		}
	}
	@SuppressLint("DefaultLocale") 
	private String formatTime(int totalSeconds){
		int hour = totalSeconds / 3600;
		int min = (totalSeconds % 3600) / 60;
		int second = totalSeconds % 60;
		if(hour==0) return String.format("%02dm %02ds", min, second);
		else if(min==0) return String.format("%02ds", second);
		else return String.format("%02dh %02dm %02ds", hour, min, second);
	}
	@SuppressWarnings("deprecation")
	Notification buildNotification(int icon, String title, RemoteViews view) {
		Context activity = this;
		Notification.Builder mBuilder = new Notification.Builder(activity);
		// mBuilder.setAutoCancel(true);
		mBuilder.setSmallIcon(icon);
		// mBuilder.setContentText(title);
		mBuilder.setContentTitle(title);

		mBuilder.setOngoing(true);
		if(view!=null)  mBuilder.setContent(view);

		Intent intent = new Intent(activity, activity.getClass());
		intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);
		// intent.setAction("android.intent.action.MAIN");

		PendingIntent resultPendingIntent = PendingIntent.getActivity(activity,
				0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(resultPendingIntent);
		return mBuilder.getNotification();
	}

	

	protected void cancelNotification() {
		// Log.v("Notification", "Clear notification");
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancel(PLAYING_NOTIFICATION_ID);
	}

	protected String downloadFile(String url) {
		try {
			HttpResponse response = requestUrl(url, null, null);
			HttpEntity entity = response.getEntity();
			return EntityUtils.toString(entity);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			showMessage(e.getMessage());
		}
		return null;
	}

	static HttpResponse requestUrl(String url, HttpEntity postData,
			BasicHeader[] headers) throws KeyStoreException,
			KeyManagementException, UnrecoverableKeyException,
			NoSuchAlgorithmException, CertificateException, IOException {

		KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
		trustStore.load(null, null);

		SSLSocketFactory sf = new MySSLSocketFactory(trustStore);
		sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

		HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

		SchemeRegistry registry = new SchemeRegistry();
		registry.register(new Scheme("http", PlainSocketFactory
				.getSocketFactory(), 80));
		registry.register(new Scheme("https", sf, 443));

		ClientConnectionManager ccm = new ThreadSafeClientConnManager(params,
				registry);

		HttpClient client = new DefaultHttpClient(ccm, params);
		HttpResponse response = null;
		if (postData == null) {
			HttpGet get = new HttpGet(url);
			if (headers != null)
				for (Header header : headers) {
					get.addHeader(header);
				}
			response = client.execute(get);
		} else {
			HttpPost post = new HttpPost(url);
			if (headers != null)
				for (Header header : headers) {
					post.addHeader(header);
				}
			post.setEntity(postData);
			response = client.execute(post);
		}
		return response;
	}

	static class MySSLSocketFactory extends SSLSocketFactory {
		SSLContext sslContext = SSLContext.getInstance("TLS");

		public MySSLSocketFactory(KeyStore truststore)
				throws NoSuchAlgorithmException, KeyManagementException,
				KeyStoreException, UnrecoverableKeyException {
			super(truststore);

			TrustManager tm = new X509TrustManager() {

				public void checkClientTrusted(X509Certificate[] chain,
						String authType) throws CertificateException {
				}

				public void checkServerTrusted(X509Certificate[] chain,
						String authType) throws CertificateException {
				}

				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}
			};

			sslContext.init(null, new TrustManager[] { tm }, null);
		}

		@Override
		public Socket createSocket(Socket socket, String host, int port,
				boolean autoClose) throws IOException, UnknownHostException {
			return sslContext.getSocketFactory().createSocket(socket, host,
					port, autoClose);
		}

		@Override
		public Socket createSocket() throws IOException {
			return sslContext.getSocketFactory().createSocket();
		}
	}
}
