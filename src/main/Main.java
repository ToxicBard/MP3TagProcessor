package main;

import java.io.File;

import commonTools.CommonTools;
import commonTools.FileTools;

public class Main {
	/* 
	 * TODO Pick and reference the best mp3 tagging library
	 * for my purposes and then test it out.
	 */
	public static void main(String[] args) {
		runTagBot();
	}
	
	private static void runTagBot(){
		File rootDirectory = FileTools.selectSavedDirectory("Select MP3 Directory",  "cfg/mp3directory.cfg");
		
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
		System.out.println("File: " + myFile.getAbsolutePath());
	}
	
	private static void doStuffFolder(File myFolder){
		System.out.println("Folder: " + myFolder.getAbsolutePath());
	}

}
