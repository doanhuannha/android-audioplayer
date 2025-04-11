package bluemoon.audioplayer;

import bluemoon.framework.ui.BaseActivity;
import bluemoon.framework.ui.ListView;
import bluemoon.framework.ui.ListViewItem;

class AudioFileListView extends ListView {

	public AudioFileListView(int id, BaseActivity context) {
		super(id, context);
		// TODO Auto-generated constructor stub
	}
	public void addItem(String text, String filePath){
		addItem(new ListViewItem(filePath, text, null));
	}

}
