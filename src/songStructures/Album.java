package songStructures;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.TagException;

import commonTools.CommonTools;

public class Album implements Serializable {
	/*
	 * TODO Change mAlbumSongPaths to an ArrayList of SongInfo objects
	 * TODO Find/replace on album title
	 * TODO Add Arraylist property to hold album art
	 */
	
	private String mAlbumTitle = null;
	private String mArtist = null;
	private ArrayList<String> mAlbumSongPaths = new ArrayList<String>();
	
	private ArrayList<String> mAlbumGenres = new ArrayList<String>();
	private ArrayList<String> mAlbumYears = new ArrayList<String>();
	private ArrayList<File> mAlbumDirectories = new ArrayList<File>();
	
	
	public Album(Song mySong){
		this.mAlbumTitle = mySong.getAlbum();
		this.mArtist = mySong.getArtist();
		this.addSong(mySong);
	}
	
	public void addSong(Song newSong){
		String songGenre = newSong.getGenre();
		String songYear = newSong.getYear();
		File songDirectory = newSong.getParentDirectory();
		
		//If a song that doesn't match the artist and album title is added, then crash
		if(!this.songMatchesAlbum(newSong)){
			String errorText = "Can't add a song from a different album.\n";
			
			errorText += "Song: " + newSong.getAbsolutePath() + "\n";
			errorText += "Artist: " + newSong.getArtist() + "\n";
			errorText += "Album: " + this.getAlbumTitle();
			CommonTools.processError(errorText);
		}
		
		//Always add the song to the Songs list
		mAlbumSongPaths.add(newSong.getAbsolutePath());
		
		//Add the genre to the genres list if it's not already there
		if(!mAlbumGenres.contains(songGenre)){
			mAlbumGenres.add(songGenre);
		}
		
		//Add the year to the years list if it's not already there
		if(!mAlbumYears.contains(songYear)){
			mAlbumYears.add(songYear);
		}
		
		if(!mAlbumDirectories.contains(songDirectory)){
			mAlbumDirectories.add(songDirectory);
		}
	}
	
	public boolean songMatchesAlbum(Song mySong){
		if(!mySong.getAlbum().equals(this.getAlbumTitle())){
			return false;
		}
		if(!mySong.getArtist().equals(this.getArtist())){
			return false;
		}
		
		return true;
	}
	
	public String getAlbumTitle(){
		return mAlbumTitle;
	}
	
	public String getArtist(){
		return mArtist;
	}
	
	public String getFirstAlbumGenre(){
		for(String loopGenre : mAlbumGenres){
			if(!loopGenre.trim().isEmpty()){
				return loopGenre;
			}
		}
		
		return "";
	}
	
	public String getFirstAlbumYear(){
		for(String loopYear : mAlbumYears){
			if(!loopYear.trim().isEmpty()){
				return loopYear;
			}
		}
		
		return "";
	}
	
	public File getFirstAlbumDirectory(){
		File toReturn = null;
		
		if(mAlbumDirectories.size() > 0){
			toReturn = mAlbumDirectories.get(0);
		}
		
		return toReturn;
	}
	
	public String toString(){
		String toReturn = "";
		String conflictsString = this.getConflictsString();
		
		toReturn += this.getArtist() + " - ";
		toReturn += this.getAlbumTitle() + ", ";
		toReturn += mAlbumSongPaths.size() + " songs";
		
		//If there are conflicts, then display them
		if(!conflictsString.isEmpty()){
			toReturn += ", conflicts: " + conflictsString;
		}
		
		return toReturn;
	}
	
	private enum albumConflictType{
		Genres, Years, Directories
	}
	
	private ArrayList<albumConflictType> getConflicts(){
		ArrayList<albumConflictType> conflicts = new ArrayList<albumConflictType>();
		
		if(mAlbumGenres.size() > 1){
			conflicts.add(albumConflictType.Genres);
		}
		if(mAlbumYears.size() > 1){
			conflicts.add(albumConflictType.Years);
		}
		if(mAlbumDirectories.size() > 1){
			conflicts.add(albumConflictType.Directories);
		}
		
		return conflicts;
	}
	
	public boolean hasConflicts(){
		if(this.getConflicts().size() > 0){
			return true;
		}
		return false;
	}
	
	private String getConflictsString(){
		String toReturn = "";
		boolean firstItem = true;
		ArrayList<albumConflictType> conflicts = this.getConflicts();
		
		for(albumConflictType conflict : conflicts){
			//If this is the first conflict then set the flag, otherwise add a comma
			if(firstItem){
				firstItem = false;
			}
			else{
				toReturn += ", ";
			}
			
			toReturn += conflict.toString();
		}
		
		return toReturn;
	}
	
	public ArrayList<Song> loadSongs(){
		ArrayList<Song> albumSongs = new ArrayList<Song>();
		File readFile = null;
		AudioFile readAudioFile = null;
		Song readSong = null;
		
		for(String songPath : mAlbumSongPaths){
			//Open the file
			readFile = new File(songPath);
			
			try {
				//Read the audio file from disk
				readAudioFile = AudioFileIO.read(readFile);
			} catch (CannotReadException | IOException | TagException
					| ReadOnlyFileException | InvalidAudioFrameException e) {
				CommonTools.processError("Error Opening Album Song AudioFile");
			}
			
			//Create the song object from the audio file
			readSong = new Song(readAudioFile);
			
			//Add the newly loaded song to the ArrayList
			albumSongs.add(readSong);
		}
		
		return albumSongs;
	}
	
	private int countNonEmptyYearTags(){
		int nonEmptyYearTags = 0;
		
		for(String tagLoop : mAlbumYears){
			if(!tagLoop.trim().isEmpty()){
				nonEmptyYearTags++;
			}
		}
		
		return nonEmptyYearTags;
	}
	
	private boolean canFixYearTags(){
		//If there is a year conflict, but there's only one valid year value,
		//Then that means that not all songs in the album have a year value.
		//This can easily be fixed by simply filling in the year for the
		//songs with missing year values.
		if(this.getConflicts().contains(albumConflictType.Years) && this.countNonEmptyYearTags() == 1){
			return true;
		}
		return false;
	}
	
	private String fixYearTags(boolean commit){
		String toReturn = "";
		String year = this.getFirstAlbumYear();
		ArrayList<Song> albumSongs = null;
		
		if(this.canFixYearTags()){
			albumSongs = this.loadSongs();
			
			for(Song loopSong : albumSongs){
				toReturn += loopSong.writeTag(FieldKey.YEAR, year, commit);
			}
		}
		
		return toReturn;
	}
	
	public void doStuffAlbum(BufferedWriter fileOut) {
		//Do stuff
		String operationOutput = "";
		
		//operationOutput = this.fixYearTags(false);
		
		if(!operationOutput.trim().isEmpty()){
			operationOutput += "\n";
		}
		
		System.out.print(operationOutput);
		try {
			fileOut.write(operationOutput);
		} catch (IOException e) {
			CommonTools.processError("Error writing doStuffAlbum output to file");
		}
	}
	
	public void doStuffEachSong(){
		ArrayList<Song> mySongs = this.loadSongs();
		
		for(Song loopSong : mySongs){
			loopSong.doStuff();
		}
	}
}
