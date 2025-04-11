package bluemoon.audioplayer;

import java.net.URI;
import java.net.URISyntaxException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class YtbActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		
		
		if (intent != null) {
			String action = intent.getAction();
			
			String type = intent.getType();
			String ytbUrl = intent.getStringExtra(Intent.EXTRA_TEXT);
			if (ytbUrl != null && Intent.ACTION_SEND.equals(action) && type != null && "text/plain".equals(type)) {
				
				Intent lauchIntent = new Intent(this, MainActivity.class);
				try {
					URI uri = new URI(ytbUrl);
					ytbUrl = uri.getPath();
					String param = uri.getPath().substring(ytbUrl.lastIndexOf("/") + 1);
					if (param.startsWith("playlist")) {
						param = "p=" + uri.getQuery().substring("list=".length());
					}
					else if (param.startsWith("watch")) {
						param = "v=" + uri.getQuery().substring("v=".length());
					} else {
						
						param = "v=" + param;
					}
					lauchIntent.putExtra(Intent.EXTRA_TEXT, param);
				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					Toast.makeText(this, "Error on parsing URL: "+ytbUrl+"\r\n"+e.getMessage(), Toast.LENGTH_SHORT).show();
				}
				startActivity(lauchIntent);
				finish();
			}
		}
		
	}
	
}
