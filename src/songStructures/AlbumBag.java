package songStructures;

import java.io.BufferedWriter;
import java.io.Serializable;
import java.util.ArrayList;

public class AlbumBag implements Serializable {
	
	private static final long serialVersionUID = 8852937180442568962L;
	private ArrayList<Album> mAlbums = new ArrayList<Album>();
	
	public void addSong(Song mySong){
		boolean addedToExistingAlbum = false;
		Album newAlbum = null;
		
		/*
		 * Check to see if the song belongs to an album
		 * that's already been added, from latest to first added
		 */
		for(int i = mAlbums.size()-1;i>=0;i--){
			//If the new song album matches this iteration's
			//album title, then add it.
			if(mySong.getAlbum().equals(mAlbums.get(i).getAlbumTitle())){
				mAlbums.get(i).addSong(mySong);
				addedToExistingAlbum = true;
				break;
			}
		}
		
		//If the song's album wasn't found, then create a new
		//album and add it to the list
		if(addedToExistingAlbum == false){
			newAlbum = new Album(mySong);
			mAlbums.add(newAlbum);
		}
		
	}
	
	public String toString(){
		String toReturn = "";
		
		for(Album loopAlbum : mAlbums){
			toReturn += loopAlbum.toString() + "\n";
		}
		
		return toReturn;
	}
	
	public String toStringConflictingAlbums(){
		String toReturn = "";
		
		for(Album loopAlbum : mAlbums){
			if(loopAlbum.hasConflicts()){
				toReturn += loopAlbum.toString() + "\n";
			}
		}
		
		return toReturn;
	}
	
	public void doStuffEachAlbum(boolean writeToFile, BufferedWriter fileOut){
		for(Album loopAlbum : mAlbums){
			loopAlbum.doStuffAlbum(writeToFile, fileOut);
		}
	}
	
	public void doStuffEachSong(){
		for(Album loopAlbum : mAlbums){
			loopAlbum.doStuffEachSong();
		}
	}
	
	/*
	 * TODO Add Serialization to save the albumbag to file
	 */
}
