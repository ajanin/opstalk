package firetalk.model;

public class DBEvent extends Event {
	public static final int rally=0;
	public static final int objPoint=1;
	public static final int wayPoint=2;
	public static final int IED=3;
	public static final int enemy=4;
	private int dbType;
	public DBEvent(int type,byte[] content, String id){
		this.setEventType(Event.DB_SYNC);
		dbType=type;
		this.setId(id);
		this.setContent(content);
	}
	public int getDbType() {
		return dbType;
	}
	public void setDbType(int dbType) {
		this.dbType = dbType;
	}
	

}
