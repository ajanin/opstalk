//package firetalk.operators.speech;
//
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//
//public class PlayWaveFile {
// 
//    /**
//     * <Replace this with one clearly defined responsibility this method does.>
//     * 
//     * @param args
//     *            the name of the wave file to play
//     */
//    public static void main(String[] args) {
// 
//	String filename = "data/audio/05_1317238702098.pcm";
//// 
////	// opens the inputStream
////	FileInputStream inputStream;
////	try {
////	    inputStream = new FileInputStream(filename);
////	} catch (FileNotFoundException e) {
////	    e.printStackTrace();
////	    return;
////	}
//// 
//	// initializes the playSound Object
//	PlaySound playSound = new PlaySound(filename);
// 
//	// plays the sound
//	try {
//	    playSound.play();
//	} catch (PlayWaveException e) {
//	    e.printStackTrace();
//		return;
//	}
//    }
// 
//}