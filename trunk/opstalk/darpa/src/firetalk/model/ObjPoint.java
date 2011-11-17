package firetalk.model;


import firetalk.db.UIRepository;

public class ObjPoint extends CheckPoint {

	public ObjPoint(String id, String userID, String name, double lat,
			double lon) {
		super(id, userID, name, lat, lon,true,false,"");
		// TODO Auto-generated constructor stub
	}

	public String toString() {
		return String.format("Objective point for %s\nLocation: <%f,%f>",
				UIRepository.peopleList.get(userID).getName(), lat, lon);
	}

}
