package speech.client;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class SpeechCapture extends Activity {

	public boolean isRecording = true;
	int frequency = 8000;
	int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
	int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
	// Create a new AudioRecord object to record the audio.
//	int bufferSize = AudioRecord.getMinBufferSize(frequency,
//			channelConfiguration, audioEncoding);
	int bufferSize = 30000;

	final String bufferFolder = "/buffer";
	final String baseName = "seg";
	final String path = Environment.getExternalStorageDirectory()
			.getAbsolutePath()
			+ bufferFolder;
	Network network = new Network();
	Timer timer = new Timer();
	String currSeg;

	GPSLocationListener locListener = null;
	LinkedList<Segment> segList = new LinkedList<Segment>();
	LinkedList<Segment> deleteList = new LinkedList<Segment>();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
//		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//		locListener = new GPSLocationListener(lm);
	}

	public void myClickHandler(View view) {
		switch (view.getId()) {
		case R.id.start:
			Button b = (Button) this.findViewById(R.id.start);
			if (b.getText().equals("start")) {
				TextView text = (TextView) this.findViewById(R.id.TextView01);
				text.setText("Connect to server...");
				AccountManager manager = AccountManager.get(this); 
			    Account[] accounts = manager.getAccountsByType("com.google"); 
			    network.connect(accounts[0].name);
				text.setText("Connect successfully");
				Thread thread = new Thread(new Runnable() {
					public void run() {
						try {
							start();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
				thread.start();
				b.setText("stop");
			} else {
				isRecording=false;
				b.setText("start");
			}
			break;
		case R.id.exit:
			try {
					network.disconnect();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			finish();
			break;
		}
	}

	public void startRecord(String bufferPath) throws IOException {
		// make sure the directory we plan to store the recording in exists
		File directory = new File(bufferPath);
		if (!directory.exists() && !directory.mkdirs()) {
			throw new IOException("Path to file could not be created.");
		}

		AudioRecord audioRecord = null;
		audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, frequency,
				channelConfiguration, audioEncoding, bufferSize);
		short[] buffer = new short[bufferSize];
		audioRecord.startRecording();
		while (isRecording) {
			currSeg = bufferPath + "/" + baseName + "_"
					+ System.currentTimeMillis() + ".pcm";
			File file = new File(currSeg);
			// Delete any previous recording.
			if (file.exists())
				file.delete();

			// Create the new file.
			try {
				file.createNewFile();
			} catch (IOException e) {
				throw new IllegalStateException("Failed to create "
						+ file.toString());
			}
			try {
				OutputStream os = new FileOutputStream(file);
				BufferedOutputStream bos = new BufferedOutputStream(os);
				DataOutputStream dos = new DataOutputStream(bos);
				int bufferReadResult = audioRecord.read(buffer, 0, bufferSize);
				for (int i = 0; i < bufferReadResult; i++)
					dos.writeShort(buffer[i]);
				dos.close();
				int isSpeech=SpeechDetector.detect(currSeg)?1:0;
				segList.add(new Segment(currSeg,System.currentTimeMillis(),null,isSpeech));
			} catch (Throwable t) {
				Log.e("AudioRecord", "Recording Failed");
			}
		}
		audioRecord.stop();
	}

	public void start() throws IOException {
		//TextView text = (TextView) this.findViewById(R.id.TextView01);
		// Location loc = locListener.getLocation();
		// if (loc != null)
		// text.setText("Provider: " + loc.getProvider() + "\nLoc: ("
		// + loc.getLongitude() + ", " + loc.getLatitude() + ")");
		// else
		// text.setText("GPS location not available");
		//text.setText("Connect to server...");
		//network.connect();
		//text.setText("Connect successfully");
		//message("Start recording...");
		String state = android.os.Environment.getExternalStorageState();
		if (!state.equals(android.os.Environment.MEDIA_MOUNTED)) {
			throw new IOException("SD Card is not mounted.  It is " + state
					+ ".");
		}
		isRecording=true;
		timer.schedule(new deleteFile(), 5000, 5000);
		new streaming().start();
		startRecord(path);
		//message("Recording thread stop...");
	}
//	public void message(String mes) {
//		((TextView) this.findViewById(R.id.TextView01)).setText(mes);
//	}

	class deleteFile extends TimerTask {
		@Override
		public void run() {
			while(!deleteList.isEmpty()){
				Segment seg=deleteList.removeLast();
				File file = new File(seg.fileName);
				if (file.exists())
					file.delete();
			}
		}
	}
	class streaming extends Thread {
		public void run() {
			while (isRecording||!segList.isEmpty()) {
				if (!segList.isEmpty()) {
					Segment seg = segList.getLast();
					try {
						network.send(seg);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						continue;
					}
					// now segment sent successfully, remove it from buffer
					segList.removeLast(); 
					deleteList.addFirst(seg);

				}
			}
			//message("Streaming thread stop...");
		}
		
	}
}