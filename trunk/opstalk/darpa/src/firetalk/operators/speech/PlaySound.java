package firetalk.operators.speech;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Vector;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.DataLine.Info;
import javax.swing.JSlider;

import firetalk.db.Repository;

/**
 * 
 * <Replace this with a short description of the class.>
 * 
 * @author Giulio
 */
public class PlaySound extends Thread {

	private final int EXTERNAL_BUFFER_SIZE = 524288; // 128Kb
	private LinkedList<String> files = new LinkedList<String>();
	private SourceDataLine dataLine = null;
	private boolean isPlay = false;
	private String id = null;//
	private int startInd = 0;
	private JSlider slider = null;

	/**
	 * CONSTRUCTOR
	 * 
	 * @throws PlayWaveException
	 */
	public PlaySound(String id, int startInd, JSlider slider) {
		this.id = id;
		this.startInd = startInd;
		this.slider = slider;

	}

	public void setInd(int index) {
		this.startInd = index;
	}

	public int getInd() {
		return this.startInd;
	}

	public String getUserId() {
		return this.id;
	}

	public boolean isPlaying() {
		return isPlay;
	}

	// public void play(File file) {
	// BufferedInputStream inputStream = null;
	// try {
	// inputStream = new BufferedInputStream(new FileInputStream(file));
	// System.out.println(file.getName());
	// } catch (FileNotFoundException e) {
	// e.printStackTrace();
	// }
	// if (inputStream != null) {
	//
	// while (readBytes != -1) {
	// readBytes = inputStream
	// .read(audioBuffer, 0, audioBuffer.length);
	// if (readBytes >= 0) {
	// dataLine.write(audioBuffer, 0, readBytes);
	// }
	// }
	// inputStream.close();
	// }
	// }

	@Override
	public void run() {
		// Obtain the information about the AudioInputStream
		AudioFormat audioFormat = new AudioFormat(8000, 16, 1, true, true);
		DataLine.Info info = new DataLine.Info(SourceDataLine.class,
				audioFormat);
		// opens the audio channel
		try {
			dataLine = (SourceDataLine) AudioSystem.getLine(info);
			dataLine.open(audioFormat, this.EXTERNAL_BUFFER_SIZE);
		} catch (LineUnavailableException e1) {
			try {
				throw new PlayWaveException(e1);
			} catch (PlayWaveException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// Starts the music :P

		dataLine.start();
		isPlay = true;
		try {
			while (isPlay) {
				Vector<File> files = Repository.audioFiles.get(id);
				if (files != null) {
					while (isPlay && startInd >= 0 && startInd < files.size()) {
						// opens the inputStream
						BufferedInputStream inputStream = null;
						try {
							inputStream = new BufferedInputStream(
									new FileInputStream(files.get(startInd)));
							System.out.println(files.get(startInd).getName());
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						}
						if (inputStream != null) {
							int readBytes = 0;
							byte[] audioBuffer = new byte[this.EXTERNAL_BUFFER_SIZE];
							while (readBytes != -1) {
								readBytes = inputStream.read(audioBuffer, 0,
										audioBuffer.length);
								System.out.println("read " + readBytes);
								if (readBytes >= 0) {
									dataLine.write(audioBuffer, 0, readBytes);
								}
							}
							inputStream.close();
						}
						slider.setValue(startInd);
						slider.setMaximum(files.size());
						slider.setEnabled(true);
						startInd++;
						System.out.println("" + startInd + " " + files.size());
					}
				}
				Thread.sleep(500);
			}
		} catch (IOException e1) {
			try {
				throw new PlayWaveException(e1);
			} catch (PlayWaveException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void stopPlay() {
		isPlay = false;
		// plays what's left and and closes the audioChannel
		dataLine.drain();
		dataLine.close();
		// this.stop();

	}
}