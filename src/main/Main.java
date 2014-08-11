package main;

import java.io.BufferedWriter;
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

import songStructures.AlbumBag;
import songStructures.Song;
import commonTools.CommonTools;
import commonTools.FileTools;
import commonTools.LoadingThread;
import commonTools.Timer;

public class Main {
	
	private static final boolean mShowBusyDisplay = false;
	private static final boolean mEnableLogging = false;
	private static final boolean mAddToAlbumBag = true;
	private static final boolean mSaveAlbumBag = true;
	private static final boolean mLoadAlbumBag = false;
	
	private static final String mAlbumBagFileLocation = "sav/albumbag.bag";
	
	private static BufferedWriter mResultFile = null;
	private static AlbumBag mAlbumBag = new AlbumBag();
	
	
	public static void main(String[] args) throws IOException {
		runTagBot();
	}
	
	/*
	 * 
	 * TODO Implement SongInfo
	 * TODO Clean up albums with multiple discnumbers
	 * TODO Implement renameByTag on a per-album basis, rather than on a per-song basis
	 * 
	 * TODO Investigate "AWT blocker activation interrupted" Exception
	 * 
	 * TODO Fix any folders which contain multiple albums
	 * TODO write Song, Album, and AlbumBag classes
	 * TODO check for albums with inconsistent year/artwork/location/artist/genre/
	 * TODO consolidate artists who have more than one genre, excluding the Bootlegs genre
	 * 
	 * TODO Fix Album Artist
	 * TODO Fix non-integer years and track numbers
	 * TODO fix/replace disk number tags
	 * TODO Rename files based on track/title
	 * 
	 * TODO Embed jpg files which correspond to albums where the art isn't embedded
	 * TODO Delete jpg files which correspond to albums where the art IS embedded
	 * TODO Investigate jaudiotagger warnings
	 */
	
	private static void runTagBot() throws IOException{
		LoadingThread busyDisplay = new LoadingThread();
		Timer botTimer = new Timer();
		File rootDirectory = null;

		//Start counting the time taken to iterate
		botTimer.start();
		
		/*
		 * Select the directory from the user, but only if
		 * we're actually traversing the directory, rather than
		 * simply loading and processing a saved albumbag.
		 */
		if(mLoadAlbumBag == false){
			rootDirectory = FileTools.selectSavedDirectory("Select MP3 Directory",  "cfg/mp3directory.cfg");
		}
		
		if(mEnableLogging == false){
			Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF);
			System.out.println("Disabling jaudiotagger logging");
		}
		
		//Start the busy display if it was specified
		if(mShowBusyDisplay){
			busyDisplay.start();
		}
		
		//Open the write file
		mResultFile = FileTools.openWriteFile("out/taggerOut.txt");

		
		if(mLoadAlbumBag){
			mAlbumBag = (AlbumBag) FileTools.readObjectFromFile(mAlbumBagFileLocation);
		}
		
		/*
		 * Traverse the directory structure doing things
		 * per each file and folder as long as we're not
		 * loading from the albumbag save file
		 */
		if(mLoadAlbumBag == false){
			traverseDirectory(rootDirectory.listFiles());
		}
		
		
		//If we were adding to the album bag, then print/write
		//the toString for each album.
		if(mAddToAlbumBag){
			//printOutput(mAlbumBag.toStringConflictingAlbums());
		}
		
		
		mAlbumBag.doStuffEachAlbum(mResultFile);
		
		//If we wrote to album bag and want to save it, then save it here
		if(mAddToAlbumBag && mSaveAlbumBag){
			FileTools.writeObjectToFile(mAlbumBag, mAlbumBagFileLocation);
		}
		
		//Stop the busy display if it's running
		if(busyDisplay.isRunning()){
			busyDisplay.stopRunning();
		}
		
		//Stop the iteration timer
		botTimer.stop();
		
		//If the write file has been opened for writing, then close it here to save the changes
		if(mResultFile != null){
			mResultFile.close();
		}
		
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
		Song mySong = null;
		
		try {
			//Open the file and create an associated Song object
			myAudioFile = AudioFileIO.read(myFile);
			mySong = new Song(myAudioFile);
			
			//Do explicitly programmed stuff where relevant
			doStuffSong(mySong);
			
			//Add to the Album bag if specified
			if(mAddToAlbumBag){
				mAlbumBag.addSong(mySong);
			}
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
	private static void doStuffSong(Song mySong) throws IOException{
		String operationResult;
		
		operationResult = mySong.doCommonReplacements(true);
		
		printOutput(operationResult);
	}
	
	private static void doStuffFolder(File myFolder){
		//Do nothing for now
	}
	
	private static void printOutput(String output) throws IOException{
		System.out.print(output);
		
		if(mResultFile != null){
			mResultFile.write(output);
		}
	}

}
