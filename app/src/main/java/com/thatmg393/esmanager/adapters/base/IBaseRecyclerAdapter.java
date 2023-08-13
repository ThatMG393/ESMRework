package com.thatmg393.esmanager.adapters.base;

import java.util.List;

public interface IBaseRecyclerAdapter<T> {
	public void addData(T data);
	public void updateData(List<T> data);
	public void clearData();
}
