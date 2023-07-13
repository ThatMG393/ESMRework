package com.thatmg393.esmanager.activities;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.thatmg393.esmanager.GlobalConstants;
import com.thatmg393.esmanager.managers.DRPCManager;
import com.thatmg393.esmanager.managers.LSPManager;
import com.thatmg393.esmanager.utils.ActivityUtils;
import com.thatmg393.esmanager.utils.FileUtils;
import com.thatmg393.esmanager.utils.StorageUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

public class BaseActivity extends AppCompatActivity {
	private static Thread.UncaughtExceptionHandler thrUEH;
	private boolean changeConfig;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (!changeConfig) {
			init();
		} else { changeConfig = false; }
    }
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if (!isChangingConfigurations()) {
			unsetUEH();
		} else { changeConfig = true; }
	}
	
    private final void setUEH() {
		if (thrUEH != null) { Log.w("BaseActivity", "Attempt to initialize 'Thread.UncaughtExceptionHandler' even though it's already initialized!"); return; }
		
        thrUEH = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread curThr, Throwable ex) {
                Log.e("BaseActivity", curThr + " made an exception! " + fullStacktrace(ex));
				FileUtils.appendToFile(GlobalConstants.getInstance().ESM_ROOT_FOLDER + "/err.txt", fullStacktrace(ex));
				
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
		Thread.setDefaultUncaughtExceptionHandler(thrUEH);
	}
	
	// Always override this for a lot initiaizing stuff
	// If initializing little things make sure "setUEH()" is present!
	// Always call "super.init()" when extending this method!
	public void init() {
		setUEH();
	}
	
	public void destroy() {
		LSPManager.getInstance().dispose();
		DRPCManager.getInstance().dispose();
		
		StorageUtils.dispose();
		ActivityUtils.dispose();
	}
	
	private static final String fullStacktrace(Throwable err) {
		final Writer result = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(result);
        
		try {
			int loopTimes = 0;
			
			Throwable cause = err;
			while (cause != null && loopTimes <= 15) {
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
