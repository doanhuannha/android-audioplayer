package bluemoon.audioplayer;



import java.io.ByteArrayInputStream;
import java.util.Locale;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.net.http.SslError;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

class YoutubeView extends WebView {
	

	private MainActivity _context;
	protected static YoutubeView s_current = null;
	private String _prevUrl = "";
	final static String APP_URL = "file:///android_asset/index.htm";
	//final static String APP_URL = "http://dev-world.net/music/";
	@SuppressLint("SetJavaScriptEnabled")
	public YoutubeView(MainActivity context) {
		super(context);
		// TODO Auto-generated constructor stub
		s_current = this;
		_context = context;
		WebSettings settings = getSettings();
		settings.setDefaultTextEncodingName("utf-8");
		settings.setSupportZoom(false);
		settings.setBuiltInZoomControls(false);
		settings.setDisplayZoomControls(false);
		settings.setJavaScriptEnabled(true);
		settings.setDomStorageEnabled(true);
		
		settings.setAllowFileAccess(true);
		settings.setAllowFileAccessFromFileURLs(true);
		settings.setAllowUniversalAccessFromFileURLs(true);
		
		settings.setAllowFileAccess(true);
		settings.setAllowContentAccess(true);
		settings.setUseWideViewPort(true);
		if(android.os.Build.VERSION.SDK_INT > 21) settings.setUserAgentString("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36");
		//enablecrossdomain41();
		
		//support to download file
		setDownloadListener(new DownloadListener() {
		    public void onDownloadStart(String url, String userAgent,
		                String contentDisposition, String mimetype,
		                long contentLength) {
		        Intent i = new Intent(Intent.ACTION_VIEW);
		        i.setData(Uri.parse(url));
		        _context.startActivity(i);
		    }
		});
		
		//support to view full screen
		setWebChromeClient(new WebChromeClient(){
			private View mCustomView;
	        private WebChromeClient.CustomViewCallback mCustomViewCallback;
	        private int mOriginalOrientation;
	        private int mOriginalSystemUiVisibility;
	        private ViewGroup.LayoutParams mOriginalLayout;
			@Override
			public void onHideCustomView() {
				// 1. Remove the custom view
                FrameLayout decor = (FrameLayout) _context.getWindow().getDecorView();
                decor.removeView(mCustomView);
                mCustomView = null;
                decor.setLayoutParams(mOriginalLayout);
                // 2. Restore the state to it's original form
                decor.setSystemUiVisibility(mOriginalSystemUiVisibility);
                _context.setRequestedOrientation(mOriginalOrientation);

                mCustomViewCallback.onCustomViewHidden();
                mCustomViewCallback = null;
                clearFocus();

			}
			@SuppressLint("InlinedApi")
			@Override
			public void onShowCustomView(View view, CustomViewCallback callback) {
				this.onShowCustomView(view, isLandscape() ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, callback);
			}
			@Override
		    public void onShowCustomView(View view, int requestedOrientation, CustomViewCallback callback) {
				if (mCustomView != null) {
                    onHideCustomView();
                    return;
                }
				FrameLayout decor = (FrameLayout)_context.getWindow().getDecorView();
                mCustomView = view;
                mOriginalSystemUiVisibility = decor.getSystemUiVisibility();
                mOriginalOrientation = _context.getRequestedOrientation();

                mCustomViewCallback = callback;

               
                mOriginalLayout = decor.getLayoutParams();
                decor.setBackgroundColor(Color.BLACK);
                decor.addView(mCustomView, new FrameLayout.LayoutParams(
                		ViewGroup.LayoutParams.MATCH_PARENT,
                		ViewGroup.LayoutParams.MATCH_PARENT, Gravity.FILL));
                _context.setRequestedOrientation(requestedOrientation);
                clearFocus();
		    }
		});
		setWebViewClient(new WebViewClient(){
			boolean timeout = false;
			@Override
			public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
				// TODO Auto-generated method stub
				handler.proceed();
			}
			@Override
			public void onPageStarted(WebView view, final String url, Bitmap favicon) {
				// TODO Auto-generated method stub
				if(url.startsWith(APP_URL) || url.startsWith("data:")){ 
					_prevUrl = url;
					super.onPageStarted(view, url, favicon);
					if(!url.startsWith("data:")){
						new Thread(new Runnable() {
				            @Override
				            public void run() {
				                timeout = true;

				                try {
				                    Thread.sleep(50000);
				                } catch (InterruptedException e) {
				                    e.printStackTrace();
				                }
				                if(timeout) {
				                	_context.runOnUiThread(new Runnable() {
										@Override
										public void run() {
											MainActivity.s_Current.showMessage("Request timeout: "+ url);
						                	
										}
									});
				                	
				                }
				            }
				        }).start();
					}
					
				}
				else if(url.startsWith("https://youtu.be")){
					stopLoading();
					String videoId = url.substring(url.lastIndexOf("/"));
					if(videoId!=null && !videoId.isEmpty())loadUrl(APP_URL+"?v="+videoId);
					else loadUrl(_prevUrl);
				}
				else if(url.startsWith("https://www.youtube.com")){
					stopLoading();
					Uri uri = Uri.parse(url);
					String videoId = uri.getQueryParameter("v");
					if(videoId!=null) loadUrl(APP_URL+"?v="+videoId);
					else loadUrl(_prevUrl);
				}
				else {
					stopLoading();
					loadUrl(_prevUrl);
				}
			}
			@SuppressLint("DefaultLocale")
			@Override
			public WebResourceResponse shouldInterceptRequest(final WebView view,
					String url) {
				// TODO Auto-generated method stub
				WebResourceResponse response = super.shouldInterceptRequest(view, url);
				if(url.toLowerCase().endsWith(".css"))
				{
					String cssData = _context.downloadFile(url);
					//.ytp-endscreen-content
					cssData+=".ytp-overflow-button,.ytp-cards-button,.ytp-watch-later-button,.ytp-share-button,.video-ads,.ytp-ce-element,.ytp-ad-progress-list,.ytp-paid-content-overlay{display:none !important;visibility:hidden !important;}.ytp-cards-teaser{top:-100px !important;visibility:hidden !important;}.ytp-pause-overlay{visibility:hidden !important;}";
					response = new WebResourceResponse("text/css", "utf-8", new ByteArrayInputStream(cssData.getBytes()));
				}
				return response;
			}
			
			@Override
			public void onReceivedError(WebView view, int errorCode,
					String description, String url) {
				// TODO Auto-generated method stub
				timeout = false;
				//MainActivity.s_Current.showMessage("WebView Error: "+url+"\r\n"+ description);
				if(url.toLowerCase(Locale.US).contains("downloadyoutube.ashx") && errorCode==-1 && description.equalsIgnoreCase("net::ERR_FAILED")){
					
				}
				else{ 
					MainActivity.s_Current.backToMain();
					_context.showMessage("Error code="+errorCode+"\r\n"+description);
				}
				
			}
			@Override
			public void onPageFinished(WebView view, String url) {
				// TODO Auto-generated method stub
				super.onPageFinished(view, url);
				timeout = false;
				if(!(url.startsWith("data:") || url.startsWith(APP_URL))){
					MainActivity.s_Current.showMessage("Invalid request:" + url);
					String title = view.getTitle();
					Log.v("DEBUG", url+" => "+title);
					if(title==null || title.compareToIgnoreCase("Youtube Music")!=0){
						MainActivity.s_Current.backToMain();
					}
					view.requestFocus();
				}
			}
		});
		addJavascriptInterface(new WebAppInterface(context), "android");
		setLongClickable(false);
		
	}
	protected void playYtb(String url){
		loadUrl("javascript:ytbPlayer.play('"+url+"', true)");
	}
	boolean _receivedBoolean = false;
	protected boolean togglePlayingMedia(){
		loadUrl("javascript:android.receiveBoolean(ytbPlayer.togglePlaying())");
		return _receivedBoolean;
	}
	protected boolean isLandscape(){
		loadUrl("javascript:android.receiveBoolean(ytbPlayer.getVideoOrientation())");
		return _receivedBoolean;
	}
	//keep alive when hidden
	@Override
	protected void onWindowVisibilityChanged(int visibility) {
		if (visibility != View.GONE && visibility != View.INVISIBLE) super.onWindowVisibilityChanged(visibility);
	}
	public class WebAppInterface {
	    //Context _context;

	    /** Instantiate the interface and set the context */
	    WebAppInterface(Context c) {
	    	//_context = c;
	    }
	    @JavascriptInterface
	    public void onPlayerStateChanged(final int state) {
	    	_context.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					BackgroundService.s_current.updateNotification(state==1?1:0);
				}
			});
	    }
	    @JavascriptInterface
	    public void receiveBoolean(boolean v) {
	    	_receivedBoolean = v;
	    }
	    @JavascriptInterface
	    public void goBack() {
	    	_context.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					loadData("", "text/html; charset=utf-8", "UTF-8");
					_context.backToMain();
				}
			});
	    	
	    }
	    @JavascriptInterface
	    public void exitIfQualified(){
	    	_context.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					_context.exitFromWeb();
				}
			});
	    }
	    
	    @JavascriptInterface
	    public void exitWhenDone(){
	    	_context.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					_context.setExitMode(0);
				}
			});
	    }
	    @JavascriptInterface
	    public void exitAfter(final int minute){
	    	_context.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					_context.setExitMode(minute);
				}
			});
	    }
	    //
	    
	}
}
