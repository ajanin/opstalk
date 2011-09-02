package combattalk.speech;

import android.app.Activity;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.Locale;
import java.util.Random;

import combattalk.mobile.CombatTalkView;

/**
 * <p>
 * Demonstrates text-to-speech (TTS). Please note the following steps:
 * </p>
 * 
 * <ol>
 * <li>Construct the TextToSpeech object.</li>
 * <li>Handle initialization callback in the onInit method. The activity
 * implements TextToSpeech.OnInitListener for this purpose.</li>
 * <li>Call TextToSpeech.speak to synthesize speech.</li>
 * <li>Shutdown TextToSpeech in onDestroy.</li>
 * </ol>
 * 
 * <p>
 * Documentation:
 * http://developer.android.com/reference/android/speech/tts/package
 * -summary.html
 * </p>
 * <ul>
 */
public class SpeechSynthesis implements TextToSpeech.OnInitListener {

	private static final String TAG = "TextToSpeechDemo";

	private TextToSpeech mTts = null;

	public SpeechSynthesis(CombatTalkView parent) {
		mTts = new TextToSpeech(parent, this);
	}

	public void stop() {
		// Don't forget to shutdown!
		if (mTts != null) {
			mTts.stop();
			mTts.shutdown();
		}
	}

	public void speak(String mes) {
		if (mTts != null)
			mTts.speak(mes, TextToSpeech.QUEUE_FLUSH, null);
	}

	// Implements TextToSpeech.OnInitListener.
	public void onInit(int status) {
		// status can be either TextToSpeech.SUCCESS or TextToSpeech.ERROR.
		if (status == TextToSpeech.SUCCESS) {
			// Set preferred language to US english.
			// Note that a language may not be available, and the result will
			// indicate this.
			if (TextToSpeech.LANG_COUNTRY_AVAILABLE == mTts
					.isLanguageAvailable(Locale.US)) {
				int result = mTts.setLanguage(Locale.US);
				// Try this someday for some interesting results.
				// int result mTts.setLanguage(Locale.FRANCE);
				if (result == TextToSpeech.LANG_MISSING_DATA
						|| result == TextToSpeech.LANG_NOT_SUPPORTED) {
					// Lanuage data is missing or the language is not supported.
					// Log.e(TAG, "Language is not available.");
					speak("language not supported in this device");
				} else {
					// Check the documentation for other possible result codes.
					// For example, the language may be available for the
					// locale,
					// but not for the specified country and variant.

					// The TTS engine has been successfully initialized.
					// Allow the user to press the button for the app to speak
					// again.
					// speak("success");
				}
			}
		} else {
			// Initialization failed.
			Log.e(TAG, "Could not initialize TextToSpeech.");
		}
	}
}