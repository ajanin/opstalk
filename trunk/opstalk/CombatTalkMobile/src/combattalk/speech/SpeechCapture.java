package combattalk.speech;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import combattalk.mobile.CombatTalkView;
import combattalk.mobile.Preferences;
import combattalk.mobile.data.Event;
import combattalk.mobile.util.NetUtil;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.location.Location;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class SpeechCapture {

	public boolean isRecording = true;
	public int actOn = 1; // 1 speech act on, 0 speech act off
	int frequency = 8000;
	int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
	int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
	// Create a new AudioRecord object to record the audio.
	// int bufferSize = AudioRecord.getMinBufferSize(frequency,
	// channelConfiguration, audioEncoding);
	int bufferSize = 30000;

	final String bufferFolder = "/buffer";
	final String baseName = "seg";
	final String path = Environment.getExternalStorageDirectory()
			.getAbsolutePath() + bufferFolder;

	Timer timer = new Timer();
	String currSeg;
	LinkedList<Segment> segList = new LinkedList<Segment>();
	// LinkedList<Segment> deleteList = new LinkedList<Segment>();
	CombatTalkView parent = null;

	public SpeechCapture(CombatTalkView parent) {
		this.parent = parent;
	}

	public void stop() {
		isRecording = false;
	}

	class RecordThread extends Thread {
		private String bufferPath = null;

		RecordThread(String path) {
			this.bufferPath = path;
		}

		public void run() {
			AudioRecord audioRecord = null;
			audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
					frequency, channelConfiguration, audioEncoding, bufferSize);
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
					// OutputStream os = new FileOutputStream(file);
					// BufferedOutputStream bos = new BufferedOutputStream(os);
					// DataOutputStream dos = new DataOutputStream(bos);
					short[] buffer = new short[bufferSize];
					int bufferReadResult = audioRecord.read(buffer, 0,
							bufferSize);
					// for (int i = 0; i < bufferReadResult; i++)
					// dos.writeShort(buffer[i]);
					// dos.close();
					int isSpeech = SpeechDetector.detect(currSeg) ? 1 : 0;
					Location loc = Preferences.saveBattery ? parent.locationHandler
							.getLocation() : parent.myLocationOverlay
							.getLastFix();
					segList.addFirst(new Segment(buffer, bufferReadResult,
							System.currentTimeMillis(), loc, actOn));
				} catch (Throwable t) {
					Log.e("AudioRecord", "Recording Failed");
				}
			}
			audioRecord.stop();
		}
	}

	public void startRecord(String bufferPath) throws IOException {

	}

	public void start() throws IOException {
		// TextView text = (TextView) this.findViewById(R.id.TextView01);
		// Location loc = locListener.getLocation();
		// if (loc != null)
		// text.setText("Provider: " + loc.getProvider() + "\nLoc: ("
		// + loc.getLongitude() + ", " + loc.getLatitude() + ")");
		// else
		// text.setText("GPS location not available");
		// text.setText("Connect to server...");
		// network.connect();
		// text.setText("Connect successfully");
		// message("Start recording...");
		String state = android.os.Environment.getExternalStorageState();
		if (!state.equals(android.os.Environment.MEDIA_MOUNTED)) {
			throw new IOException("SD Card is not mounted.  It is " + state
					+ ".");
		}
		isRecording = true;
		new streaming().start();
		new RecordThread(path).start();
		// message("Recording thread stop...");
	}

	public void send(Segment seg) throws IOException {
		Event event = new Event(Event.AUDIO);
		if (seg.location != null) {
			event.setLatitude(seg.location.getLatitude());
			event.setLongitude(seg.location.getLongitude());
		}
		event.setValidTime(seg.timeStamp);
		byte[] buffer = new byte[seg.length];
		for (int i = 0; i < seg.length; i++)
			buffer[i] = (byte) seg.buffer[i];
		if (seg.isSpeech == 1) {
			event.setContent(buffer);
		}
		parent.addEvent(event);
		parent.showMessage("event "+ seg.timeStamp+" sent");
	}

	class streaming extends Thread {
		public void run() {
			while (isRecording || !segList.isEmpty()) {
				if (!segList.isEmpty()) {
					Segment seg = segList.getLast();
					try {
						send(seg);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						continue;
					}
					// now segment sent successfully, remove it from buffer
					segList.removeLast();
					// deleteList.addFirst(seg);

				}
			}
			// message("Streaming thread stop...");
		}

	}
}