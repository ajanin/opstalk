package firetalk.model;

public class Alert {
	int id;
	int level; //how severe this alert is
	double time; // time when this alert is triggered
	int personId; // the person who triggered the alert
}
