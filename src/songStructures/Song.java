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

import org.apache.commons.io.FilenameUtils;

public class Song {
	
	/*
	 * TODO Break out the logic of which files to rename by tag into their own public
	 * boolean function so that renameByTag serves are more general purpose
	 */
	
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
	
	public String getDiscNumber(){
		return mTag.getFirst(FieldKey.DISC_NO);
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
		return this.getArtist() + " - " + this.getAlbum() + ", " + this.getTrack() + " - " + this.getTitle() + "|" + mAudioFile.getFile().getName();
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
	
	public String splitTagNumber(String delim, boolean commit){
		String trackNumberTag = this.getTrack();
		String replaceTrackNumber;
		String toReturn = "";
		int delimIndex;
		
		if(trackNumberTag.contains(delim)){
			delimIndex = trackNumberTag.indexOf(delim);
			replaceTrackNumber = trackNumberTag.substring(0,  delimIndex);
			toReturn = replaceTrackNumber + "\n";
			
			if(commit){
				try {
					mTag.setField(FieldKey.TRACK, replaceTrackNumber);
					mAudioFile.commit();
				} catch (CannotWriteException | KeyNotFoundException | FieldDataInvalidException e) {
					CommonTools.processError("Error writing tag for " + this.toString());
				}
			}
		}
		
		return toReturn;
	}
	
	
	public String renameByTag(boolean commit){
		int trackNumber = this.getTrackAsInt();
		int maxFileNameLength = 42;
		int fileNameTrimLength;
		boolean renameResult;
		String parentDirectoryPath = mAudioFile.getFile().getParentFile().getAbsolutePath();
		String origFileName = mAudioFile.getFile().getName();
		String fileExtension = FilenameUtils.getExtension(origFileName);
		String newFileName;
		String trackNumberAsString = trackNumber + "";
		String songTitle = this.getTitle();
		String toReturn = "";
		File originalFile = mAudioFile.getFile();
		File renameFile = null;
		
		//Check track number String
		if(trackNumber == 0){
			CommonTools.processError("Invalid Track Number");
		}
		
		//Add a leading 0 so that track number string is always at least 2 digits
		if(trackNumber > 0 && trackNumber <= 9){
			trackNumberAsString = "0" + trackNumber;
		}
		
		//Generate new filename (before adding extension).  Convert title to digits and letters to prevent
		//invalid filename issues.
		newFileName = trackNumberAsString + "_" + CommonTools.toDigitsEnglishChars(songTitle);
		
		/*
		 * If the generated filename (with period and extension added) is too long, then
		 * decide on the length to trim it to (based on extension length) and trim accordingly
		 */
		if((newFileName + "." + fileExtension).length() > maxFileNameLength){
			fileNameTrimLength = maxFileNameLength - fileExtension.length() - ".".length();
			newFileName = newFileName.substring(0, fileNameTrimLength);
		}
		
		//Add the extension to the generated filename, regardless of whether it was trimmed or not.
		newFileName = newFileName + "." + fileExtension;
		
		//We only care to continue processing if the existing file doesn't start with
		//the decided-upon track number string and the new filename is different from the current filename
		if(!origFileName.startsWith(trackNumberAsString) && !newFileName.equals(origFileName)){
			renameFile = new File(parentDirectoryPath + "/" + newFileName);
			
			//If a file with the new filename already exists, then crash and manually investigate
			if(renameFile.exists()){
				CommonTools.processError("Potential rename collision for " + this.toString());
			}
			
			toReturn = newFileName + "|length:" + newFileName.length() + "|" + this.toString();
			
			if(commit){
				renameResult = originalFile.renameTo(renameFile);
				if(renameResult == false){
					CommonTools.processError("Rename failed for " + this.toString());
				}
			}
		}
		
		if(!toReturn.trim().isEmpty()){
			toReturn += "\n";
		}
		
		return toReturn;
	}
	
	public String doCommonReplacements(boolean commit){
		String operationResult;
		
		//Title comparisons
		operationResult = this.findReplaceTag(FieldKey.TITLE, "(Album Version)", "", false, commit);
		operationResult += this.findReplaceTag(FieldKey.TITLE, "(Album Version - Explicit)", "", false, commit);
		operationResult += this.findReplaceTag(FieldKey.TITLE, "(Album Version - Parental Advisory)", "", false, commit);
		operationResult += this.findReplaceTag(FieldKey.TITLE, "(Lp Version)", "", false, commit);
		operationResult += this.findReplaceTag(FieldKey.TITLE, "(Remastered)", "", false, commit);
		operationResult += this.findReplaceTag(FieldKey.TITLE, "(Remaster)", "", false, commit);
		operationResult += this.findReplaceTag(FieldKey.TITLE, "(Remastered Version)", "", false, commit);
		operationResult += this.trimTag(FieldKey.TITLE, commit);
		
		//Album comparisons
		operationResult += this.findReplaceTag(FieldKey.ALBUM, "(Remastered)", "", false, commit);
		operationResult += this.trimTag(FieldKey.ALBUM, commit);
		
		return operationResult;
	}
	
	
	public void doStuff(){
		//Do stuff
	}
}
