package combattalk.sr;

public interface SpeechResultsHandler {
	public void finalResult(String str);   // Called when final results are available.
	public void partialResult(String str); // Called periodically as the recognizer runs.
}
