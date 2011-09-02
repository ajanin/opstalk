package combattalk.mobile;

import combattalk.mobile.data.People;
import combattalk.mobile.data.Repository;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ListActivity;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class HelloListView<View> extends ListActivity {
	String[] items=new String[]{"Squad Leader","Team Leader","Soldier"};
	int[] iconId=new int[]{R.drawable.squad_leader,R.drawable.soldier,R.drawable.soldier};
	String account;
	@Override
	public void onCreate(Bundle savedInstanceState) {
	  super.onCreate(savedInstanceState);
	// --- get account---
		AccountManager manager = AccountManager.get(this);
		Account[] accounts = manager.getAccountsByType("com.google");
		if (accounts.length > 1)
			account = accounts[1].name.substring(0, 10);
		else
			account = accounts[0].name.substring(0, 10);
		
	  setListAdapter(new ArrayAdapter<String>(this, R.layout.list_item, items));

	  ListView lv = getListView();
	  lv.setTextFilterEnabled(true);

	  lv.setOnItemClickListener(new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, android.view.View view,
				int position, long id) {
//		     Toast.makeText(getApplicationContext(), ((TextView) view).getText(),
//			          Toast.LENGTH_SHORT).show();
//		     String level=(String) ((TextView) view).getText();
		     People people=Repository.peopleList.get(account);
		     if(people!=null){
		    	 people.setIconId(iconId[position]);
		     }
		    finish();
			
		}
	  });
	}
}
