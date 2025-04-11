package bluemoon.audioplayer;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import android.content.Context;
import bluemoon.framework.db.BaseTable;
import bluemoon.framework.db.DataItem;
import bluemoon.framework.db.Database;


class PlayListDB{
	private static final int DATABASE_VERSION = 2;
	private static final int TABLE_PLAYLIST = 0;
	private static final int TABLE_MEDIA = 1;
	Database _db;
	public PlayListDB(Context context){
		_db = new Database(context, "dbAudioPlayer", new BaseTable[]{ new DefPlayListDetail(), new DefCurrentMediaDetail()},DATABASE_VERSION,null);
	}
	public void saveTempPlayList(String[] filePaths){
		_db.delete(TABLE_PLAYLIST, DefPlayListDetail.COL_PlayListId+"=-1", null);
		if(filePaths==null) return;
		for(int i=0;i<filePaths.length;i++){
			DataItem fileItem = new DataItem();
			fileItem.data.put(DefPlayListDetail.COL_PlayListId, -1);
			fileItem.data.put(DefPlayListDetail.COL_FilePath, filePaths[i]);
			_db.insert(TABLE_PLAYLIST, fileItem);
		}
		
	}
	public void saveCurrentMedia(String filePath, int currentPos){
		_db.delete(TABLE_MEDIA, DefCurrentMediaDetail.COL_PlayListId+"=-1", null);
		if(filePath==null) return;
		DataItem fileItem = new DataItem();
		fileItem.data.put(DefCurrentMediaDetail.COL_PlayListId, -1);
		fileItem.data.put(DefCurrentMediaDetail.COL_FilePath, filePath);
		fileItem.data.put(DefCurrentMediaDetail.COL_CurrentPos, currentPos);
		_db.insert(TABLE_MEDIA, fileItem);
		
	}
	public String[] getLastMedia(){
		List<DataItem> list =new  ArrayList<DataItem>();
		boolean result = _db.getAll(TABLE_MEDIA, list);
		if(result){
			DataItem item = list.get(0);
			
			String[] ret = new String[2];
			
			ret[0] = item.data.get(DefCurrentMediaDetail.COL_FilePath).toString();
			ret[1] = item.data.get(DefCurrentMediaDetail.COL_CurrentPos).toString();
			return ret;
		}
		else{
			return null;
		}
	}
	public String[] getTempPlayList(){
		List<DataItem> listFile =new  ArrayList<DataItem>();
		boolean result = _db.getAll(TABLE_PLAYLIST, DefPlayListDetail.COL_PlayListId +"= -1", null, listFile);
		if(result){
			String[] files = new String[listFile.size()];
			for(int i=0;i<files.length;i++){
				files[i] = listFile.get(i).data.get(DefPlayListDetail.COL_FilePath).toString();
			}
			return files;
		}
		else{
			return null;
		}
	}
	class DefPlayListDetail extends BaseTable {
		protected static final String COL_PlayListId = "PlayListId";
		protected static final String COL_FilePath = "FilePath";
		public DefPlayListDetail(){


		}
		@Override
		public String getName() {
			return "PlayList";
		}
	

		@Override
		protected void initColumns(Hashtable<String, Integer> columns) {
			// TODO Auto-generated method stub
			columns.put(COL_PlayListId, Database.DATATYPE_INT);
			columns.put(COL_FilePath, Database.DATATYPE_STRING);
		}
	
	}
	class DefCurrentMediaDetail extends BaseTable {
		protected static final String COL_PlayListId = "PlayListId";
		protected static final String COL_FilePath = "FilePath";
		protected static final String COL_CurrentPos = "CurrentPos";
		public DefCurrentMediaDetail(){
			

		}
		@Override
		public String getName() {
			return "CurrentMedia";
		}
	

		@Override
		protected void initColumns(Hashtable<String, Integer> columns) {
			// TODO Auto-generated method stub
			columns.put(COL_PlayListId, Database.DATATYPE_INT);
			columns.put(COL_FilePath, Database.DATATYPE_STRING);
			columns.put(COL_CurrentPos, Database.DATATYPE_INT);
		}
	
	}
}

