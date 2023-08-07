package com.thatmg393.esmanager.fragments.main.base;

import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.CallSuper;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.thatmg393.esmanager.utils.ThreadPlus;

public class ListFragment extends Fragment {
	private ThreadPlus readerThread;
	private SwipeRefreshLayout refreshLayout;
	private RecyclerView recyclerView;
	private RelativeLayout loadingLayout;
	private RelativeLayout emptyLayout;
	
	private boolean isScanningWhileOnPause;
	
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
