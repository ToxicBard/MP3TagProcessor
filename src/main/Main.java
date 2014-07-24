package main;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;

import commonTools.CommonTools;
import commonTools.FileTools;
import commonTools.LoadingThread;
import commonTools.Timer;

public class Main {
	
	private static final boolean mShowBusyDisplay = false;
	private static final boolean mEnableLogging = false;
	
	public static void main(String[] args) {
		runTagBot(mShowBusyDisplay, mEnableLogging);
	}
	
	/*
	 * TODO Write a find/replace function that works for a 
	 * 		provided id tag field.  It should also display 
	 * 		the old/new values and have a parameter saying 
	 * 		whether to actually write the new tags.
	 * TODO clean up messy song/album titles and genres
	 * 
	 * TODO Fix any folders which contain multiple albums
	 * TODO design Album and AlbumBag classes
	 * TODO check for albums with inconsistent artwork/location/artist/genre/
	 * TODO consolidate artists who have more than one genre, excluding the Bootlegs genre
	 * 
	 * TODO Embed jpg files which correspond to albums where the art isn't embedded
	 * TODO Delete jpg files which correspond to albums where the art IS embedded
	 * TODO Investigate jaudiotagger warnings
	 */
	
	private static void runTagBot(boolean showBusyDisplay, boolean enableLogging){
		LoadingThread busyDisplay = new LoadingThread();
		Timer botTimer = new Timer();
		File rootDirectory = null;

		//Start counting the time taken to iterate
		botTimer.start();
		
		rootDirectory = FileTools.selectSavedDirectory("Select MP3 Directory",  "cfg/mp3directory.cfg");
		
		if(!enableLogging){
			Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF);
		}
		
		//Start the busy display if it was specified
		if(showBusyDisplay){
			busyDisplay.start();
		}
		
		/*
		 * Traverse the directory structure doing things
		 * per each file and folder
		 */
		traverseDirectory(rootDirectory.listFiles());
		
		//Stop the busy display if it's running
		if(busyDisplay.isRunning()){
			busyDisplay.stopRunning();
		}
		
		//Stop the iteration timer
		botTimer.stop();
		
		System.out.println(botTimer.getElapsedIntervalString());
	}
	
	/*
	 * Loop through each file/folder in the selected directory
	 * and do the appropriate action for each file/folder.
	 */
	private static void traverseDirectory(File[] myFiles){
		for(File loopFile : myFiles){
			
			if(loopFile.isFile()){
				openFile(loopFile);
			}
			
			if(loopFile.isDirectory()){
				doStuffFolder(loopFile);
				
				if(loopFile.listFiles() != null){
					traverseDirectory(loopFile.listFiles());
				}
			}
		}
	}
	
	private static void openFile(File myFile){
		AudioFile mySong = null;
		
		try {
			mySong = AudioFileIO.read(myFile);
			doStuffSong(mySong);
		} catch (CannotReadException | IOException | TagException
				| ReadOnlyFileException | InvalidAudioFrameException e) {
			/*
			 * If a file can't be read as an audio file, then
			 * check to see if it's a *.jpg file.
			 * -If it's not a jpg, then crash because this needs to
			 * be handled on a case-by-case basis
			 * -If it is a jpg, then continue silently.  I'll go 
			 * through and embed/delete where relevant in the future,
			 * but they're not hurting anything in the meantime
			 */
			if(!myFile.getAbsolutePath().endsWith(".jpg")){
				CommonTools.processError("File isn't a *.jpg and can't be opened as an audio file: " + myFile.getAbsolutePath());
				//System.out.println(myFile.getAbsolutePath());
				//myFile.delete();
			}
		}
	}
	
	/*
	 * This should get called for every valid audio file
	 */
	private static void doStuffSong(AudioFile mySong){
		//Do nothing for now
		/*
		Tag audioTag = null;
		
		audioTag = mySong.getTag();
		System.out.println(audioTag.getFirst(FieldKey.ALBUM));
		*/
	}
	
	private static void doStuffFolder(File myFolder){
		//Do nothing for now
	}

}
