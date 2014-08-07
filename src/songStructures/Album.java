package songStructures;

import java.io.File;
import java.util.ArrayList;

import commonTools.CommonTools;

public class Album {
	/*
	 * TODO Add Arraylist property to hold album art
	 */
	
	private String mAlbumTitle = null;
	private ArrayList<Song> mAlbumSongs = new ArrayList<Song>();
	
	private ArrayList<String> mAlbumGenres = new ArrayList<String>();
	private ArrayList<String> mAlbumArtists = new ArrayList<String>();
	private ArrayList<String> mAlbumYears = new ArrayList<String>();
	private ArrayList<File> mAlbumDirectories = new ArrayList<File>();
	
	
	public Album(Song mySong){
		this.mAlbumTitle = mySong.getAlbum();
		this.addSong(mySong);
	}
	
	public void addSong(Song newSong){
		String songGenre = newSong.getGenre();
		String songArtist = newSong.getArtist();
		String songYear = newSong.getYear();
		File songDirectory = newSong.getParentDirectory();
		
		//If a song with a different album title is added, then crash.
		if(!newSong.getAlbum().equals(this.getAlbumTitle())){
			String errorText = "Can't add a song from a different album.\n";
			
			errorText += "Song: " + newSong.getAbsolutePath() + "\n";
			errorText += "Album: " + this.getAlbumTitle();
			CommonTools.processError(errorText);
		}
		
		//Always add the song to the Songs list
		mAlbumSongs.add(newSong);
		
		//Add the genre to the genres list if it's not already there
		if(!mAlbumGenres.contains(songGenre)){
			mAlbumGenres.add(songGenre);
		}
		
		//Add the artist to the artists list if it's not already there
		if(!mAlbumArtists.contains(songArtist)){
			mAlbumArtists.add(songArtist);
		}
		
		//Add the year to the years list if it's not already there
		if(!mAlbumYears.contains(songYear)){
			mAlbumYears.add(songYear);
		}
		
		if(!mAlbumDirectories.contains(songDirectory)){
			mAlbumDirectories.add(songDirectory);
		}
	}
	
	public String getAlbumTitle(){
		return mAlbumTitle;
	}
	
	public String getFirstAlbumArtist(){
		String toReturn = "";
		
		if(mAlbumArtists.size() > 0){
			toReturn = mAlbumArtists.get(0);
		}
		
		return toReturn;
	}
	
	public String getFirstAlbumGenre(){
		String toReturn = "";
		
		if(mAlbumGenres.size() > 0){
			toReturn = mAlbumGenres.get(0);
		}
		
		return toReturn;
	}
	
	public String getFirstAlbumYear(){
		String toReturn = "";
		
		if(mAlbumYears.size() > 0){
			toReturn = mAlbumYears.get(0);
		}
		
		return toReturn;
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
		
		toReturn += this.getFirstAlbumArtist() + " - ";
		toReturn += this.getAlbumTitle() + ", ";
		toReturn += mAlbumSongs.size() + " songs";
		
		//If there are conflicts, then display them
		if(!conflictsString.isEmpty()){
			toReturn += ", conflicts: " + conflictsString;
		}
		
		return toReturn;
	}
	
	private enum albumConflictType{
		Genres, Artists, Years, Directories
	}
	
	private ArrayList<albumConflictType> getConflicts(){
		ArrayList<albumConflictType> conflicts = new ArrayList<albumConflictType>();
		
		if(mAlbumGenres.size() > 1){
			conflicts.add(albumConflictType.Genres);
		}
		if(mAlbumArtists.size() > 1){
			conflicts.add(albumConflictType.Artists);
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
	
	public void doStuffAlbum(){
		//Do stuff
	}
	
	public void doStuffEachSong(){
		for(Song loopSong : mAlbumSongs){
			loopSong.doStuff();
		}
	}
	
	/*
	 * TODO Once the class is written, compare the performance of
	 * having get functions that actively go through the songs for
	 * lists of each tag, rather than storing the lists as a property
	 * of the album
	 */
}
