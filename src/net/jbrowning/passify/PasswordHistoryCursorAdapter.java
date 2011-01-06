package net.jbrowning.passify;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.util.Log;

/**
 * A modified version of the SimpleCursorAdapter that does not return the first item in the Cursor
 */

public class PasswordHistoryCursorAdapter extends SimpleCursorAdapter {
	
	private static final String TAG = "PasswordHistoryCursorAdapter";

	public PasswordHistoryCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
		super(context, layout, c, from, to);
	}

	@Override
	public int getCount() {
		int count = super.getCount();
		Log.d(TAG, ">>>> in getCount - count is "  + count + " <<<<");
		if (count > 0) {
			return count - 1;
		}
		return count;
	}
	
	@Override
	public Object getItem(int position) {
		Log.d(TAG, ">>>> in getItem - requesting position " + position);
		return super.getItem(position + 1);
	}
	
	@Override
	public long getItemId(int position) {
		Log.d(TAG, ">>>> in getItemId - requesting position " + position + " <<<<");
		return super.getItemId(position);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Log.d(TAG, ">>>> in getView - requesting position " + position + " <<<<");
		return super.getView(position + 1, convertView, parent);
	}
	
}