package com.thatmg393.esmanager.utils;

import io.github.rosemoe.sora.text.Content;
import io.github.rosemoe.sora.text.ContentIO;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

public class FileUtils {
	public static FileInputStream openFile(String path) throws IOException {
		return new FileInputStream(path);
	}
	
	public static Content openFileAsContent(String path) throws IOException {
		return openFileAsContent(openFile(path));
	}
	
	public static Content openFileAsContent(InputStream is) throws IOException {
		return ContentIO.createFrom(is);
	}
	
	public static void writeToFile(String path, String contents) throws IOException {
		FileOutputStream fos = new FileOutputStream(path);
		fos.write(contents.getBytes());
	}
	
	public static void writeToFileUsingContent(Content text, String path) throws IOException {
		ContentIO.writeTo(text, new FileOutputStream(path), true);
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
