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
	
	/*
	 * -Add a function to get the absolutepath of the file itself
	 * -Add a function to get the absolutepath of the parent directory
	 * -Add a function to count the number of instances of a particular tag
	 */
	
	/*
	 * TODO Add get methods for common tags such as artist, title, album, genre, track, year
	 * TODO Update existing functions to use those get methods, rather than reading the tag directly
	 */
	
	public void findReplaceTag(FieldKey tagKey, String findString, 
			String replaceString, boolean exactMatchOnly, 
			boolean useRegex, boolean commit){
		String currentTag = mTag.getFirst(tagKey);
		String newTag = null;
		
		if(useRegex){
			newTag = currentTag.replaceFirst(findString, replaceString);
		}
		else {
			newTag = currentTag.replace(findString, replaceString);
		}
		
		if(currentTag.equals(newTag)==false && (exactMatchOnly == false || currentTag.equals(findString))){
			System.out.println(currentTag + ", " + newTag);
			
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
	}
	
	public void trimTag(FieldKey tagKey, boolean commit){
		String currentTag = mTag.getFirst(tagKey);
		String newTag = null;
		
		if(!currentTag.trim().equals(currentTag)){
			newTag = currentTag.trim();
			System.out.println(currentTag + ", " + newTag);
			
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
	
	private File getParentDirectory(){
		return mAudioFile.getFile().getAbsoluteFile().getParentFile();
	}
	
	public String getParentDirectoryPath(){
		return this.getParentDirectory().toString();
	}
	
	public String getAbsolutePath(){
		return mAudioFile.getFile().getAbsolutePath();
	}
}
