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

public class Main {
	
	private static String mLastErrorString = "";
	
	public static void main(String[] args) {
		runTagBot();
	}
	
	/*
	 * TODO Investigate jaudiotagger warnings
	 * TODO delete tmp and goutputstream files
	 * TODO add Timer
	 * TODO add optional LoadingThread
	 * TODO Fix any folders which contain multiple albums
	 * TODO Embed jpg files which correspond to albums where the art isn't embedded
	 * TODO Delete jpg files which correspond to albums where the art IS embedded
	 * TODO check for any other remaining files which can't be opened for tagging and process accordingly
	 * TODO design Album and AlbumBag classes
	 * TODO check for albums with inconsistent artwork/location/artist/genre/
	 * TODO clean up messy song/album titles
	 * TODO consolidate artists who have more than one genre, excluding the Bootlegs genre
	 */
	
	private static void runTagBot(){
		File rootDirectory = FileTools.selectSavedDirectory("Select MP3 Directory",  "cfg/mp3directory.cfg");
		
		Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF);
		
		traverseDirectory(rootDirectory.listFiles());
	}
	
	/*
	 * Loop through each file/folder in the selected directory
	 * and do the appropriate action for each file/folder.
	 */
	private static void traverseDirectory(File[] myFiles){
		for(File loopFile : myFiles){
			
			if(loopFile.isFile()){
				doStuffFile(loopFile);
			}
			
			if(loopFile.isDirectory()){
				doStuffFolder(loopFile);
				
				if(loopFile.listFiles() != null){
					traverseDirectory(loopFile.listFiles());
				}
			}
		}
	}
	
	private static void doStuffFile(File myFile){
		AudioFile mySong = null;
		Tag audioTag = null;
		
		deleteNonReadableNonJPGFiles(myFile, mySong, false);
		
		//audioTag = mySong.getTag();
		//System.out.println(audioTag.getFirst(FieldKey.ALBUM));
	}
	
	private static void doStuffFolder(File myFolder){
		//Do nothing for now
	}
	
	private static void deleteNonReadableNonJPGFiles(File myFile, AudioFile mySong, boolean commitChanges){
		try {
			mySong = AudioFileIO.read(myFile);
		} catch (CannotReadException | IOException | TagException
				| ReadOnlyFileException | InvalidAudioFrameException e) {
			//Display the non-readable files that aren't jpg images
			if(!myFile.getAbsolutePath().endsWith(".jpg")){
				System.out.println(mLastErrorString + " " + myFile.getAbsolutePath());
				if(commitChanges){
					myFile.delete();
				}
			}
		}
	}

}
