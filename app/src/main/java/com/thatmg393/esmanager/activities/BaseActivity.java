package com.thatmg393.esmanager.activities;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.CallSuper;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.thatmg393.esmanager.GlobalConstants;
import com.thatmg393.esmanager.utils.FileUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

public class BaseActivity extends AppCompatActivity {
	private Thread.UncaughtExceptionHandler thrUEH;
	private boolean changeConfig;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		init(savedInstanceState);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (isFinishing()) unsetUEH();
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
				
				System.out.println(curThr.getState().name());
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
	
	private final void unsetUEH() {
		if (thrUEH == null) {
			Log.w("BaseActivity", "Attempt to unset 'Thread.UncaughtExceptionHandler' even though it's already unset!");
			return;
		}
		
		Thread.setDefaultUncaughtExceptionHandler(thrUEH);
		thrUEH = null;
	}
	
	// Always override this for a lot of initiaizing stuff
	@CallSuper
	public void init(@Nullable Bundle savedInstanceState) {
		setUEH();
	}
	
	public final boolean isRecreating() {
		return this.changeConfig;
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
