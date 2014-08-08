package songStructures;

import java.io.File;
import java.util.List;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.KeyNotFoundException;
import org.jaudiotagger.tag.Tag;

import commonTools.CommonTools;

public class Song {
	
	private AudioFile mAudioFile;
	private Tag mTag;
	
	public Song(AudioFile mySong){
		mAudioFile = mySong;
		mTag = mAudioFile.getTag();
	}
	
	public String writeTag(FieldKey tagKey, String newTag, boolean commit){
		String currentTag = mTag.getFirst(tagKey);
		String toReturn = "";

		toReturn = this.toString() + " | " + currentTag + ", " + newTag + "\n";
		
		//If we want to commit and the new tag is different than the current one, then write the tag
		if(commit && currentTag.equals(newTag) == false){
			try {
				mTag.setField(tagKey, newTag);
				mAudioFile.commit();
			} catch (KeyNotFoundException | FieldDataInvalidException | CannotWriteException e) {
				CommonTools.processError("Error writing tag for " + currentTag);
			}
		}
		
		return toReturn;
	}
	
	public String findReplaceTag(FieldKey tagKey, String findString, 
			String replaceString, boolean exactMatchOnly, boolean commit){
		String currentTag = mTag.getFirst(tagKey);
		String newTag = null;
		String toReturn = "";
		
		newTag = currentTag.replace(findString, replaceString);
		
		if(currentTag.equals(newTag)==false && (exactMatchOnly == false || currentTag.equals(findString))){
			toReturn = currentTag + ", " + newTag + "\n";
			
			//Only write if specified.  Otherwise we run in a read-only mode to check the comparison before writing changes.
			if(commit){
				try {
					mTag.setField(tagKey, newTag);
					mAudioFile.commit();
				} catch (KeyNotFoundException | FieldDataInvalidException | CannotWriteException e) {
					CommonTools.processError("Error writing tag for " + currentTag);
				}
			}
		}
		
		return toReturn;
	}
	
	public String trimTag(FieldKey tagKey, boolean commit){
		String currentTag = mTag.getFirst(tagKey);
		String newTag = null;
		String toReturn = "";
		
		if(!currentTag.trim().equals(currentTag)){
			newTag = currentTag.trim();
			toReturn = currentTag + ", " + newTag + "\n";
			
			//Only write if specified.  Otherwise we run in a read-only mode to check the comparison before writing changes.
			if(commit){
				try {
					mTag.setField(tagKey, newTag);
					mAudioFile.commit();
				} catch (KeyNotFoundException | FieldDataInvalidException | CannotWriteException e) {
					CommonTools.processError("Error writing tag for " + currentTag);
				}
			}
		}
		
		return toReturn;
	}
	
	private List<String> getTags(FieldKey tagKey){
		List<String> myTags = mTag.getAll(tagKey);
		return myTags;
	}
	
	public String getTagListString(FieldKey tagKey){
		List<String> tagList = this.getTags(tagKey);
		String toReturn = "";
		
		for(String loopTag: tagList){
			toReturn += loopTag + "/n";
		}
		
		return toReturn;
	}
	
	public int countTags(FieldKey tagKey){
		return this.getTags(tagKey).size();
	}
	
	public File getParentDirectory(){
		return mAudioFile.getFile().getAbsoluteFile().getParentFile();
	}
	
	public String getParentDirectoryPath(){
		return this.getParentDirectory().toString();
	}
	
	public String getAbsolutePath(){
		return mAudioFile.getFile().getAbsolutePath();
	}
	
	public String getGenre(){
		return mTag.getFirst(FieldKey.GENRE);
	}
	
	public String getArtist(){
		return mTag.getFirst(FieldKey.ARTIST);
	}
	
	public String getAlbumArtist(){
		return mTag.getFirst(FieldKey.ALBUM_ARTIST);
	}
	
	public String getAlbum(){
		return mTag.getFirst(FieldKey.ALBUM);
	}
	
	public String getTitle(){
		return mTag.getFirst(FieldKey.TITLE);
	}
	
	public String getTrack(){
		return mTag.getFirst(FieldKey.TRACK);
	}
	
	public String getYear(){
		return mTag.getFirst(FieldKey.YEAR);
	}
	
	public String toString(){
		return this.getArtist() + " - " + this.getAlbum() + ", " + this.getTrack() + " - " + this.getTitle();
	}
	
	private boolean hasTrack(){
		return !this.getTrack().trim().isEmpty();
	}
	
	private int getTrackAsInt(){
		return CommonTools.intParse(this.getTrack());
	}
	
	public boolean hasInvalidTrack(){
		if(this.hasTrack() && this.getTrackAsInt() == 0){
			return true;
		}
		return false;
	}
	
	/*
	public String renameByTag(boolean commit){
		int trackNumber = this.getTrackAsInt();
		String trackNumberAsString = trackNumber + "";
		String fileExtension = mAudioFile.getFile().
		
		if(trackNumber == 0){
			CommonTools.processError("Invalid Track Number");
		}
		
		if(trackNumber > 0 && trackNumber <= 9){
			trackNumberAsString = "0" + trackNumber;
		}
	}
	*/
	
	public void doStuff(){
		//Do stuff
	}
}
