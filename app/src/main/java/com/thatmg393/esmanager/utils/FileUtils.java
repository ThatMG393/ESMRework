package com.thatmg393.esmanager.utils;

import io.github.rosemoe.sora.text.Content;
import io.github.rosemoe.sora.text.ContentIO;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

public class FileUtils {
	public static FileInputStream openFile(String path) {
		try {
			return new FileInputStream(path);
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace(System.err);
			return null;
		}
	}
	
	public static Content openFileAsContent(String path) {
		return openFileAsContent(openFile(path));
	}
	
	public static Content openFileAsContent(InputStream is) {
		try {
			return ContentIO.createFrom(is);
		} catch (IOException ioe) {
			ioe.printStackTrace(System.err);
			return null;
		}
	}
	
	public static void writeToFile(String path, String contents) {
		try {
	  	  FileOutputStream fos = new FileOutputStream(path);
			fos.write(contents.getBytes());
		} catch (IOException e) {
   		 e.printStackTrace();
   	 }
	}
	
	public static boolean writeToFileUsingContent(Content text, String path) {
		try {
			ContentIO.writeTo(text, new FileOutputStream(path), true);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static void appendToFile(String path, String contents) {
		try (FileWriter fw = new FileWriter(path, true);
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter out = new PrintWriter(bw)
		) {
   		 out.println(contents);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
