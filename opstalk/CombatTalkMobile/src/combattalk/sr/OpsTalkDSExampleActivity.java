//
//package combattalk.sr;
//
//import java.io.BufferedInputStream;
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.net.URL;
//import java.net.URLConnection;
//
//import org.apache.http.util.ByteArrayBuffer;
//
//import android.app.Activity;
//import android.os.Bundle;
//import android.os.Environment;
//import android.util.Log;
//import android.view.Menu;
//import android.view.MenuInflater;
//import android.view.MenuItem;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.widget.Button;
//import android.widget.TextView;
//
//public class OpsTalkDSExampleActivity extends Activity implements SpeechCommandHandler, SpeechResultsHandler {
//	private DynaSpeakRecognizer m_recognizer;	
//	
//   	private Button m_recordButton;
//	private TextView m_statusTextView;
//	private TextView m_heardTextView;
//	
//    /** Called when the activity is first created. */
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.main);
//        
//        // This instance handles both speech commands and speech results,
//        // hence the DynaSpeakRecognizer constructor is called with this, this.
//        
//        m_recognizer = new DynaSpeakRecognizer(this, this);
//        
//        m_recordButton = (Button) findViewById(R.id.ButtonRecord);
//        m_statusTextView = (TextView) findViewById(R.id.TextViewStatus);
//        m_heardTextView  = (TextView) findViewById(R.id.TextViewHeard);
//        
//        // With endpointing enabled, you have to track when the 
//        // callback occurs to reenable recording.
//        
//        m_recordButton.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//            	if (m_recognizer.canListen()) {
//            		m_recordButton.setText("Listening...");
//            		m_recordButton.setEnabled(false);
//            		m_recognizer.start();
//            	} else {
//            		setStatusText("Recognizer not ready");
//            	}
//            }
//        });
//    }
//    
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.menu, menu);
//        return true;
//    }
//    
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle item selection
//        switch (item.getItemId()) {
//        case R.id.update_grammar:
//        	update_grammar_from_web();
//        	return true;
//        case R.id.reload_grammar:
//            reload_grammar();
//    		setStatusText("Recognizer reset.");
//            return true;
//        case R.id.help:
//        	// TODO: Add some help
//            return true;
//        default:
//            return super.onOptionsItemSelected(item);
//        }
//    }
//    
//    
//    // Reset the recognizer. This should be called, for example, if
//    // you load a new grammar into /mnt/sdcard/dsexample_data.
//
//    public void reload_grammar() {
//    	m_recognizer = new DynaSpeakRecognizer(this, this);
//    }
//    
//    
//    // TODO: make this asynchronous.
//    
//    // Download new version of the grammar from the web. If successful, replaces existing grammar.
//    public void update_grammar_from_web() {
//    	final String GRAMFILE = "dsexample.Grammars";
//    	final String WORDFILE = "dsexample.Words";
//    	
//    	if (download_grammar_file(GRAMFILE) &&
//    		download_grammar_file(WORDFILE)) {
//    		update_grammar_file(GRAMFILE);
//    		update_grammar_file(WORDFILE);
//    		reload_grammar();
//    		setStatusText("Updated grammar from web.");
//    	} else {
//    		setStatusText("Updating grammar from web failed.");
//    	}
//    }
//    
//    // Do the actual download of the file to a local file with the
//    // same name but .new appended.
//    // TODO: Make this more efficient.
//    
//    private boolean download_grammar_file(String name) {
//    	String localdir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/dsexample_data";
//
//    	try {
//    		URL url = new URL("http://www3.icsi.berkeley.edu/~janin/opstalk/" + name);
//    		File file = new File(localdir, name + ".new");
//            URLConnection con = url.openConnection();
//            InputStream is = con.getInputStream();
//            BufferedInputStream bis = new BufferedInputStream(is);
//            ByteArrayBuffer baf = new ByteArrayBuffer(20000);
//            int current = 0;
//            while ((current = bis.read()) != -1) {
//            	baf.append((byte) current);
//            }
//            FileOutputStream fos = new FileOutputStream(file);
//            fos.write(baf.toByteArray());
//            fos.close();
//        } catch (IOException e) {
//                Log.d("opstalk", "Error: " + e);
//                return false;
//        }
//        return true;
//    }
//    
//    // Rename the local file by removing the .new at the end.
//    
//    private void update_grammar_file(String name) {
//    	String localdir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/dsexample_data";
//    	File origfile = new File(localdir, name + ".new");
//    	if (!origfile.exists()) {
//    		setStatusText("Updating grammar failed unexpectedly.");
//    		return;
//    	}
//    	File newfile = new File(localdir, name);
//    	if (!origfile.renameTo(newfile)) {
//    		setStatusText("Updating grammar failed unexpectedly.");
//    		return;
//    	}
//    }
//    
//    // These are required because the callbacks below run in a different thread.
//    // You must update the ui only from the ui thread.
//    
//    public void setStatusText(String txt) {
//    	final String ftxt = txt;
//    	runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                m_statusTextView.setText(ftxt);
//            }
//        });
//    }
//    
//    public void setHeardText(String txt) {
//    	final String ftxt = txt;
//    	runOnUiThread(new Runnable() {
//    		@Override
//    		public void run() {
//     	       m_heardTextView.setText(ftxt);
//    	    }
//    	});
//    }
//    
//    public void resetRecordButton() {
//    	runOnUiThread(new Runnable() {
//    		@Override
//    		public void run() {
//    			m_recordButton.setText("Start Recording");
//    			m_recordButton.setEnabled(true);
//    		}
//    	});
//    }
//    
//
//    //////////////////////////////////////////////////////////
//    //
//    // SpeechCommandHandler methods
//    // 
//    
//    public void whereIsPersonCommand(int person) {
//    	setStatusText(String.format("Got whereIsPersonCommand %d", person));
//    	resetRecordButton();
//    }
//    
//    public void whereIsWaypointCommand(int waypoint) {
//    	setStatusText(String.format("Got whereIsWaypointCommand %d", waypoint));
//    	resetRecordButton();
//    }
//
//    public void whereIsTeamCommand(int team) {
//    	setStatusText(String.format("Got whereIsTeamCommand %d", team));
//    	resetRecordButton();
//    }
//    
//    public void whereIsRallyCommand(int rally) {
//    	setStatusText(String.format("Got whereIsRallyCommand %d", rally));
//    	resetRecordButton();
//    }
//
//    public void whereIsObjectiveCommand(int obj) {
//    	setStatusText(String.format("Got whereIsObjectiveCommand %d", obj));
//    	resetRecordButton();
//    }
//
//
//    public void whoIsNearPersonCommand(int person) {
//    	setStatusText(String.format("Got whoIsNearPerson %d", person));
//    	resetRecordButton();
//    }
//    
//    public void whoIsNearWaypointCommand(int waypoint) {
//    	setStatusText(String.format("Got whoIsNearWaypoint %d", waypoint));
//    	resetRecordButton();
//    }
//
//    public void whoIsNearTeamCommand(int team) {
//    	setStatusText(String.format("Got whoIsNearTeam %d", team));
//    	resetRecordButton();
//    }
//    
//    public void whoIsNearRallyCommand(int rally) {
//    	setStatusText(String.format("Got whoIsNearRally %d", rally));
//    	resetRecordButton();
//    }
//
//    public void whoIsNearObjectiveCommand(int obj) {
//    	setStatusText(String.format("Got whoIsNearObjective %d", obj));
//    	resetRecordButton();
//    }
//
//
//    public void closestPersonToMeCommand() {
//    	setStatusText("Got closestPersonToMe");
//    	resetRecordButton();
//    }
//    
//    public void closestWaypointToMeCommand() {
//    	setStatusText("Got closestWaypointToMe");
//    	resetRecordButton();
//    }
//    
//    public void closestRallyToMeCommand() {
//    	setStatusText("Got closestRallyToMe");
//    	resetRecordButton();
//    }
//
//    public void closestObjectiveToMeCommand() {
//    	setStatusText("Got closestObjectiveToMe");
//    	resetRecordButton();
//    }
//
//    
//    public void whereAmICommand() {
//    	setStatusText("Got whereAmI");
//    	resetRecordButton();
//    }
//
//    public void sayAgainCommand() {
//    	setStatusText("Got sayAgain");
//    	resetRecordButton();
//    }
//    
//    public void voiceNoteCommand() {
//    	setStatusText("Got voiceNote");
//    	resetRecordButton();
//    }
//    
//    public void parserError(String msg) {	
//    	setStatusText("Got parser error " + msg);
//    	resetRecordButton();
//    }
//    
//    
//    //////////////////////////////////////////////////////////
//    //
//    // SpeechResultsHandler methods
//    // 
//    
//    public void finalResult(String str) {
//    	setHeardText("Final: " + str);
//    	resetRecordButton();
//    }
//    
//    public void partialResult(String str) {
//    	setHeardText("Partial: " + str);
//    }
//      
//     
//} // class TestDSActivity