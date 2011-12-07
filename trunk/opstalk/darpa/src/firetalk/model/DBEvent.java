package firetalk.model;

public class DBEvent extends Event {
	public static enum DBType{
		rally,objPoint,wayPoint,IED
	}
	private DBType dbType=DBType.rally;
	public DBEvent(DBType type,byte[] content, String id){
		this.setEventType(Event.DB_SYNC);
		dbType=type;
		this.setId(id);
		this.setContent(content);
	}
	public DBType getDbType() {
		return dbType;
	}
	public void setDbType(DBType dbType) {
		this.dbType = dbType;
	}
	

}
