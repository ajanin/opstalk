package combattalk.sr;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.os.Environment;
import android.util.Log;

import com.sri.dynaspeak.android.audio.AudioCapture;
import com.sri.dynaspeak.android.audio.AudioCaptureListener;
import com.sri.dynaspeak.android.recognizer.DSRecognizer;
import com.sri.dynaspeak.android.recognizer.RecognitionResultListener;

public class DynaSpeakRecognizer {

    private AudioCapture m_capture;
    private DSRecognizer m_recognizer;
	private DynaSpeakRecognizerListener m_listener;
	private SpeechCommandHandler m_commandHandler;
	private SpeechResultsHandler m_resultsHandler;
    private volatile boolean m_recording;
    private volatile boolean m_busy;
	
	// commandHandler and resultsHandler may either be null.
	public DynaSpeakRecognizer(SpeechCommandHandler commandHandler, SpeechResultsHandler resultsHandler) {
        m_busy = false;
        m_recording = false;
		m_listener = new DynaSpeakRecognizerListener();
        initializeRecognizer();
        m_capture = null;
        m_commandHandler = commandHandler;
        m_resultsHandler = resultsHandler;
	}
	        
    public void start() {
        if (m_recording || m_busy) {
            return;
        }

        m_recording = true;
        m_busy = true;
    	m_recognizer.recognize();
    	// New capture each time. I'm not sure if this is needed.
    	if (m_capture != null) {
    		m_capture.setListener(null);
    		m_capture = null;
    	}
    	m_capture = new AudioCapture(m_listener, 16000);
    	m_capture.start();
    }
    
    public void stop() {
    	if (!m_recording) {
    		return;
    	}
    	if (m_capture != null) {
    		m_capture.stopRecording();
       	}
    	m_recording = false;
    }
    
    public boolean canListen() {
    	return !m_recording && !m_busy;    	
    }
    
    private void initializeRecognizer() {
        String outDirectoryString = Environment.getExternalStorageDirectory().getAbsolutePath();

        try {
            m_recognizer = new DSRecognizer(outDirectoryString + "/dsexample_data/dsexample.info", "dsexample.info");
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.e("test", "FileNotFoundException: " + e);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.e("test", "IOException: " + e);
        }
        if (m_recognizer != null) {
            m_recognizer.setListener(m_listener);
            if (m_recognizer.init() == false) {
                Log.e("test", "DynaSpeak initialization failed");
                m_recognizer.destroy();
                m_recognizer = null;
            } else {
                Log.v("test", "DynaSpeak init returned true");
            }
        } else {
            Log.e("test", "DynaSpeak initialization failed");
        }
    }
    
    private class DynaSpeakRecognizerListener implements RecognitionResultListener, AudioCaptureListener {
    	//TODO: Redo this with enum
    	private class CommandPattern {
    		public int command; // From SpeechCommandHandler
    		public Pattern pattern;
    		public int ngroups; // Number of expected groups
    		public CommandPattern(int com, Pattern pat, int ng) { command = com; pattern = pat; ngroups = ng; }
    	}
    	
    	private static final int WHERE_IS_PERSON = 1;
    	private static final int WHERE_IS_WAYPOINT = 2;
    	private static final int WHERE_IS_TEAM = 3;
    	private static final int WHERE_IS_RALLY = 4;
    	private static final int WHERE_IS_OBJECTIVE = 5;
    	private static final int WHO_IS_NEAR_PERSON = 6;
    	private static final int WHO_IS_NEAR_WAYPOINT = 7;
    	private static final int WHO_IS_NEAR_TEAM = 8;
    	private static final int WHO_IS_NEAR_RALLY = 9;
    	private static final int WHO_IS_NEAR_OBJECTIVE = 10;
    	private static final int CLOSEST_PERSON_TO_ME = 11;
    	private static final int CLOSEST_WAYPOINT_TO_ME = 12;
    	private static final int CLOSEST_RALLY_TO_ME = 13;
    	private static final int CLOSEST_OBJECTIVE_TO_ME = 14;
    	private static final int WHERE_AM_I = 15;
    	private static final int SAY_AGAIN = 16;
    	private static final int VOICE_NOTE = 17;

    	private final CommandPattern m_commands[] = {
    			new CommandPattern(WHERE_IS_PERSON, Pattern.compile("\\{Person_([0-9]+)\\}.*\\{WhereIs\\}"), 1),
    			new CommandPattern(WHERE_IS_WAYPOINT, Pattern.compile("\\{Waypoint_([0-9]+)\\}.*\\{WhereIs\\}"), 1),
    			new CommandPattern(WHERE_IS_TEAM, Pattern.compile("\\{Team_([0-9]+)\\}.*\\{WhereIs\\}"), 1),
    			new CommandPattern(WHERE_IS_RALLY, Pattern.compile("\\{Rally_([0-9]+)\\}.*\\{WhereIs\\}"), 1),
    			new CommandPattern(WHERE_IS_OBJECTIVE, Pattern.compile("\\{Objective_([0-9]+)\\}.*\\{WhereIs\\}"), 1),

    			new CommandPattern(WHO_IS_NEAR_PERSON, Pattern.compile("\\{Person_([0-9]+)\\}.*\\{WhoIsNear\\}"), 1),
    			new CommandPattern(WHO_IS_NEAR_WAYPOINT, Pattern.compile("\\{Waypoint_([0-9]+)\\}.*\\{WhoIsNear\\}"), 1),
    			new CommandPattern(WHO_IS_NEAR_TEAM, Pattern.compile("\\{Team_([0-9]+)\\}.*\\{WhoIsNear\\}"), 1),
    			new CommandPattern(WHO_IS_NEAR_RALLY, Pattern.compile("\\{Rally_([0-9]+)\\}.*\\{WhoIsNear\\}"), 1),
    			new CommandPattern(WHO_IS_NEAR_OBJECTIVE, Pattern.compile("\\{Objective_([0-9]+)\\}.*\\{WhoIsNear\\}"), 1),
    			
    			new CommandPattern(CLOSEST_PERSON_TO_ME, Pattern.compile("\\{ClosestPersonToMe\\}"), 0),
    			new CommandPattern(CLOSEST_WAYPOINT_TO_ME, Pattern.compile("\\{ClosestWaypointToMe\\}"), 0),
    			new CommandPattern(CLOSEST_RALLY_TO_ME, Pattern.compile("\\{ClosestRallyToMe\\}"), 0),
    			new CommandPattern(CLOSEST_OBJECTIVE_TO_ME, Pattern.compile("\\{ClosestObjectiveToMe\\}"), 0),

    			new CommandPattern(WHERE_AM_I, Pattern.compile("\\{WhereAmI\\}"), 0),
    			new CommandPattern(SAY_AGAIN, Pattern.compile("\\{Repeat\\}"), 0),
    			new CommandPattern(VOICE_NOTE, Pattern.compile("\\{VoiceNote\\}"), 0)
    	};
    	
    	private void dispatchCommand(int command, int arg) {
    		switch (command) {
    		case WHERE_IS_PERSON:		  m_commandHandler.whereIsPersonCommand(arg); 		break;
    		case WHERE_IS_WAYPOINT: 	  m_commandHandler.whereIsWaypointCommand(arg);		break;
    		case WHERE_IS_TEAM:			  m_commandHandler.whereIsTeamCommand(arg);			break;
    		case WHERE_IS_RALLY:		  m_commandHandler.whereIsRallyCommand(arg);		break;
    		case WHERE_IS_OBJECTIVE:	  m_commandHandler.whereIsObjectiveCommand(arg);	break;

    		case WHO_IS_NEAR_PERSON:	  m_commandHandler.whoIsNearPersonCommand(arg);		break;
    		case WHO_IS_NEAR_WAYPOINT:	  m_commandHandler.whoIsNearWaypointCommand(arg);	break;
    		case WHO_IS_NEAR_TEAM:		  m_commandHandler.whoIsNearTeamCommand(arg);		break;
    		case WHO_IS_NEAR_RALLY:		  m_commandHandler.whoIsNearRallyCommand(arg);		break;
    		case WHO_IS_NEAR_OBJECTIVE:	  m_commandHandler.whoIsNearObjectiveCommand(arg); 	break;
    		
    		case CLOSEST_PERSON_TO_ME:	  m_commandHandler.closestPersonToMeCommand();		break;
    		case CLOSEST_WAYPOINT_TO_ME:  m_commandHandler.closestWaypointToMeCommand(); 	break;
    		case CLOSEST_RALLY_TO_ME: 	  m_commandHandler.closestRallyToMeCommand(); 		break;
    		case CLOSEST_OBJECTIVE_TO_ME: m_commandHandler.closestObjectiveToMeCommand(); 	break;
    		
    		case WHERE_AM_I:			  m_commandHandler.whereAmICommand();				break;
    		case SAY_AGAIN:				  m_commandHandler.sayAgainCommand();				break;
    		case VOICE_NOTE:			  m_commandHandler.voiceNoteCommand();				break;
    		default: m_commandHandler.parserError("Got a bad command index. This shouldn't happen.");
    		}
    	}
    	
    	// RecognitionResultListener
    
    	@Override
    	public void finalResult(String id, String m) {
    		if (m_commandHandler != null) {
    			doFinalCommand(id, m);
    		}
    		if (m_resultsHandler != null) {
    			m_resultsHandler.finalResult(m);
    		}
    		m_busy = false;
        	if (m_capture != null) {
        		m_capture.stopRecording();
           	}
    	}
    	
    	private void doFinalCommand(String id, String m) {
    		for (CommandPattern command : m_commands) {
    			Matcher matcher = command.pattern.matcher(m);
    			if (matcher.find()) {
    				if (command.ngroups == 0) {
    					dispatchCommand(command.command, 0);
    					return;
    				} else if (command.ngroups == 1) {
    					String grp = matcher.group(1);
    					if (grp != null) {
    						dispatchCommand(command.command, Integer.parseInt(grp));
    					} else {
    						m_commandHandler.parserError("Couldn't find argument within command. This shouldn't happen.");
    					}
    					return;
    				} else {
    					m_commandHandler.parserError("Too many groups. This shouldn't happen.");
    					return;
    				}
    			}
    		}
    		// If we get here, no patterns matches.
    		m_commandHandler.parserError("No matching command found.");
    	}
    
    	@Override
    	public void partialResult(String id, String m) {
    		if (m_resultsHandler != null) {
    			m_resultsHandler.partialResult(m);
    		}
    	}
    	
    
    	@Override
    	public void recognizerInitializationComplete(String id, boolean success,
    			String errorMessage) {
    		// Do nothing
    	}
    
    	// AudioCaptureListener
    	@Override
    	public void addSamples(short[] samples, int offset, int len) {
    		m_recognizer.addSamples(samples, offset, len);
    	}

    	// For AudioCaptureListener
    	@Override
    	public void captureStarted() {
    	}

    	// For AudioCaptureListener
    	@Override
    	public void captureStopped() {
    		m_recognizer.endSamples();
    		m_recording = false;
    	}    
    } // class DynaSpeakRecognizerListener
} // class DynaSpeakRecognizer