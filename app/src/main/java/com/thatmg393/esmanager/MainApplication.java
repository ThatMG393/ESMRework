package com.thatmg393.esmanager;

import android.app.Application;
import android.content.Context;
import android.os.StrictMode;
import android.util.Log;

import com.thatmg393.esmanager.managers.editor.language.LanguageManager;
import com.thatmg393.esmanager.utils.EditorUtils;

import com.thatmg393.esmanager.utils.FileUtils;
import io.github.rosemoe.sora.langs.textmate.registry.FileProviderRegistry;
import io.github.rosemoe.sora.langs.textmate.registry.GrammarRegistry;
import io.github.rosemoe.sora.langs.textmate.registry.provider.AssetsFileResolver;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

public class MainApplication extends Application {
	private Thread.UncaughtExceptionHandler thrUEH;
	
	@Override
	public void onCreate() {
		super.onCreate();
		FileProviderRegistry.getInstance().addFileProvider(
			new AssetsFileResolver(getAssets())
		);
		
		try {
			GrammarRegistry.getInstance().loadGrammars("tm/languages/languages.json");
			EditorUtils.loadTMThemes();
			LanguageManager.getInstance().registerLanguages();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		setUEH();
	}
	
	@Override
	public void onTerminate() {
		super.onTerminate();
		unsetUEH();
	}
	
	
	private final void setUEH() {
		if (thrUEH != null) {
			Log.w("BaseActivity", "Attempt to initialize 'Thread.UncaughtExceptionHandler' even though it's already initialized!");
			return;
		}
		
		thrUEH = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread curThr, Throwable ex) {
				Log.e("BaseActivity", curThr + " made an exception! " + fullStacktrace(ex));
				FileUtils.appendToFile(GlobalConstants.getInstance().getESMRootFolder() + "/crash.txt", fullStacktrace(ex));
				
				android.os.Process.killProcess(android.os.Process.myPid());
					
				/*
				PendingIntent pdInt = PendingIntent.getActivity(getApplicationContext(), 69, caInt, PendingIntent.FLAG_CANCEL_CURRENT);
				AlarmManager aMan = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
				aMan.set(AlarmManager.RTC_WAKEUP, 10, pdInt);
				finishAffinity();
				android.os.Process.killProcess(android.os.Process.myPid());
				System.exit(2);
				*/
				
				// thrUEH.uncaughtException(curThr, ex);
			}
		});
	}
	
	public void unsetUEH() {
		if (thrUEH == null) {
			Log.w("BaseActivity", "Attempt to unset 'Thread.UncaughtExceptionHandler' even though it's already unset!");
			return;
		}
		
		Thread.setDefaultUncaughtExceptionHandler(thrUEH);
		thrUEH = null;
	}
	
	private final String fullStacktrace(Throwable err) {
		final Writer result = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(result);
		
		try {
			int loopTimes = 0;
			
			Throwable cause = err;
			while (cause != null && loopTimes <= 45) {
				cause.printStackTrace(printWriter);
				cause = cause.getCause();
				loopTimes++;
			}
		
			printWriter.close();
			result.close();
		} catch (IOException e) { }
		
		return result.toString();
	}
}
