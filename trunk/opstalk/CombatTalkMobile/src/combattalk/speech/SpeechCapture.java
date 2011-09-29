package combattalk.speech;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
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
	private RecordThread rThread = null;

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
		if (rThread != null) {
//			rThread.interrupt();
//			rThread.stop();
			rThread.stopRecord();
		}
	}

	class RecordThread extends Thread {
		AudioRecord audioRecord = null;

		private native final void native_stop();

		public void stopRecord() {
//			if (audioRecord != null) {
//				try {
//					if (audioRecord.getRecordingState() == AudioRecord.STATE_INITIALIZED) {
//						audioRecord.stop();
//						audioRecord.release();
//					}
//				} catch (Exception e) {
//					native_stop();
//					e.printStackTrace();
//				}
//			}
		}

		public void run() {

			audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
					frequency, channelConfiguration, audioEncoding, bufferSize);
			audioRecord.startRecording();
			while (isRecording) {
				try {
					short[] buffer = new short[bufferSize];
					int bufferReadResult = audioRecord.read(buffer, 0,
							bufferSize);
					// int isSpeech = SpeechDetector.detect(currSeg) ? 1 : 0;
					Location loc = Preferences.saveBattery ? parent.locationHandler
							.getLocation() : parent.myLocationOverlay
							.getLastFix();
					send(new Segment(buffer, bufferReadResult,
							System.currentTimeMillis(), loc, actOn));
				} catch (Throwable t) {
					Log.e("AudioRecord", "Recording Failed");
				}
			}
			audioRecord.stop();
			audioRecord.release();
		}
	}

	public void start() throws IOException {
		isRecording = true;
		// new streaming().start();
		(rThread = new RecordThread()).start();
		// message("Recording thread stop...");
	}

	public void send(Segment seg) throws IOException {
		Event event = new Event(Event.AUDIO);
		if (seg.location != null) {
			event.setLatitude(seg.location.getLatitude());
			event.setLongitude(seg.location.getLongitude());
		}
		event.setValidTime(seg.timeStamp);
		ByteBuffer byteBuf = ByteBuffer.allocate(seg.length * 2);
		for (int i = 0; i < seg.length; i++)
			byteBuf.putShort(seg.buffer[i]);
		byte[] buffer = byteBuf.array();
		if (seg.isSpeech == 1) {
			event.setContent(buffer);
		}
		parent.addEvent(event);
		parent.showMessage("event " + seg.timeStamp + " sent");
	}
}