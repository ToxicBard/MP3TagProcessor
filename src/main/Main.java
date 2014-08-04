package main;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.KeyNotFoundException;
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
	 * TODO clean up messy song/album titles and genres
	 * TODO Implement writing of process info to a file, rather than the console
	 * 
	 * TODO Fix any folders which contain multiple albums
	 * TODO design Album and AlbumBag classes
	 * TODO check for albums with inconsistent year/artwork/location/artist/genre/
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
		
		if(enableLogging == false){
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
		AudioFile myAudioFile = null;
		
		try {
			myAudioFile = AudioFileIO.read(myFile);
			doStuffSong(myAudioFile);
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
	private static void doStuffSong(AudioFile myAudioFile){

		/*
		findReplaceTag(myAudioFile, FieldKey.GENRE, "60s Rock (Done)", "60s Rock |", true);
		findReplaceTag(myAudioFile, FieldKey.GENRE, "70s Hard Rock/Metal (Done)", "70s Hard Rock / Metal |", true);
		findReplaceTag(myAudioFile, FieldKey.GENRE, "70s Metal (done)", "70s Hard Rock / Metal |", true);
		findReplaceTag(myAudioFile, FieldKey.GENRE, "80s Metal (done)", "80s Metal |", true);
		findReplaceTag(myAudioFile, FieldKey.GENRE, "80s Metal (Done)", "80s Metal |", true);
		findReplaceTag(myAudioFile, FieldKey.GENRE, "90s Alternative (done)", "90s Music |", true);
		findReplaceTag(myAudioFile, FieldKey.GENRE, "90s Music (done)", "90s Music |", true);
		*/
		
		trimTag(myAudioFile, FieldKey.TITLE, true);
	}
	
	private static void findReplaceTag(AudioFile myAudioFile, FieldKey tagKey, String findString, String replaceString, boolean commit){
		Tag tag = myAudioFile.getTag();
		String currentTag = tag.getFirst(tagKey);
		String newTag = null;
		
		if(currentTag.contains(findString)){
			newTag = currentTag.replace(findString, replaceString);
			System.out.println(currentTag + ", " + newTag);
			
			//Only write if specified.  Otherwise we run in a read-only mode to check the comparison before writing changes.
			if(commit){
				try {
					tag.setField(tagKey, newTag);
					myAudioFile.commit();
				} catch (KeyNotFoundException | FieldDataInvalidException | CannotWriteException e) {
					CommonTools.processError("Error writing tag for " + currentTag);
				}
			}
		}
	}
	
	private static void trimTag(AudioFile myAudioFile, FieldKey tagKey, boolean commit){
		Tag tag = myAudioFile.getTag();
		String currentTag = tag.getFirst(tagKey);
		String newTag = null;
		
		if(!currentTag.trim().equals(currentTag)){
			newTag = currentTag.trim();
			System.out.println(currentTag + ", " + newTag);
			
			//Only write if specified.  Otherwise we run in a read-only mode to check the comparison before writing changes.
			if(commit){
				try {
					tag.setField(tagKey, newTag);
					myAudioFile.commit();
				} catch (KeyNotFoundException | FieldDataInvalidException | CannotWriteException e) {
					CommonTools.processError("Error writing tag for " + currentTag);
				}
			}
		}
	}
	
	private static void doStuffFolder(File myFolder){
		//Do nothing for now
	}

}
