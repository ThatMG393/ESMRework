package com.thatmg393.esmanager.fragments.main.base;

import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.CallSuper;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.thatmg393.esmanager.adapters.base.IBaseRecyclerAdapter;
import com.thatmg393.esmanager.utils.ThreadPlus;

import java.util.ArrayList;

/** ListFragment
 *
 * You must call {@link com.thatmg393.esmanager.fragments.main.base.ListFragment#setDataType())} first
 * then {@link com.thatmg393.esmanager.fragments.main.base.ListFragment#registerLayout())}
 * in order for the ListFragment to work!
 * 
 * @param <D> the data type of your adapter
 * @author ThatMG393
 */
public class ListFragment<D> extends Fragment {
	public static final String LIST_DATA_KEY = "savedListData";
	public final Gson GSON = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
	
	private ThreadPlus readerThread;
	private SwipeRefreshLayout refreshLayout;
	private RecyclerView recyclerView;
	private RelativeLayout loadingLayout;
	private RelativeLayout emptyLayout;
	
	private boolean isScanningWhileOnPause;
	private TypeToken dataType;
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		initViews();
		if (savedInstanceState != null) {
			ArrayList<Object> cachedData = GSON.fromJson(savedInstanceState.getString(LIST_DATA_KEY), dataType.getType());
			if (cachedData.size() > 0) {
				((IBaseRecyclerAdapter) recyclerView.getAdapter()).updateData(cachedData);
				updateViewStates(ReaderState.DONE);
			} else {
				refreshOrPopulateRecyclerView();
			}
		} else {
			refreshOrPopulateRecyclerView();
		}
	}
	
	/* This is used for saving the data of your adapter
	 * GSON needs this to cast the data to your model
	 *
	 * @param type the class of the type, you get the class by doing {TheClass.class} or {TheClass.getClass()}
	 */
	public final void setDataType(Class<D> type) {
		this.dataType = TypeToken.getParameterized(ArrayList.class, type);
	}
	
	public void registerLayouts(
		SwipeRefreshLayout refreshLayout,
		RecyclerView recyclerView,
		RelativeLayout loadingLayout,
		RelativeLayout emptyLayout,
		Runnable readerImpl
	) {
		this.refreshLayout = refreshLayout;
		this.recyclerView = recyclerView;
		this.loadingLayout = loadingLayout;
		this.emptyLayout = emptyLayout;
		this.readerThread = new ThreadPlus(readerImpl, false) {
			@Override
			public void start() {
				super.start();
				updateViewStates(ReaderState.LOADING);
			}
			
			@Override
			public void stop() {
				super.stop();
				if (isScanningWhileOnPause) updateViewStates(ReaderState.EMPTY);
			}
		};
	}
	
	@Override
	public void onPause() {
		super.onPause();
		if (readerThread != null && readerThread.isRunning()) {
			isScanningWhileOnPause = true;
			readerThread.stop();
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if (readerThread != null && isScanningWhileOnPause) {
			readerThread.start();
			isScanningWhileOnPause = false;
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (readerThread != null && !isScanningWhileOnPause) {
			readerThread.kill();
		}
	}
	
	@CallSuper
	public void refreshOrPopulateRecyclerView() {
		readerThread.start();
	}
	
	// @CallSuper
	public void initViews() { }
	
	public volatile ReaderState currentState = ReaderState.LOADING;
	public synchronized void setReaderState(ReaderState newState) {
		this.currentState = newState;
	}
	
	public synchronized void updateViews() {
		switch (this.currentState) {
			case EMPTY:
				recyclerView.post(() -> {
					recyclerView.setVisibility(View.GONE);
					loadingLayout.setVisibility(View.GONE);
					emptyLayout.setVisibility(View.VISIBLE);
					refreshLayout.setEnabled(true);
					refreshLayout.setRefreshing(false);
				});
				break;
			case LOADING:
				recyclerView.post(() -> {
					refreshLayout.setRefreshing(false);
					refreshLayout.setEnabled(false);
					recyclerView.setVisibility(View.GONE);
					emptyLayout.setVisibility(View.GONE);
					loadingLayout.setVisibility(View.VISIBLE);
				});
				break;
			case DONE:
				recyclerView.post(() -> {
					emptyLayout.setVisibility(View.GONE);
					loadingLayout.setVisibility(View.GONE);
					recyclerView.setVisibility(View.VISIBLE);
					refreshLayout.setEnabled(true);
					refreshLayout.setRefreshing(false);
				});
				break;
		}
	}
	
	public final synchronized void updateViewStates(ReaderState newState) {
		setReaderState(newState);
		updateViews();
	}
	
	public enum ReaderState {
		EMPTY, LOADING, DONE
	}
}
