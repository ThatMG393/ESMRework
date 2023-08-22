package com.thatmg393.esmanager.adapters.base;

import com.thatmg393.esmanager.interfaces.IOnRecyclerItemClickListener;
import java.util.ArrayList;

public interface IBaseRecyclerAdapter<T> {
	public void addData(T data);
	public void updateData(ArrayList<T> data);
	public void clearData();
	public ArrayList<T> getDataList();
	
	public void setItemClickListener(IOnRecyclerItemClickListener listener);
}
