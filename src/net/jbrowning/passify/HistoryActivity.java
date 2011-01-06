package net.jbrowning.passify;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.database.Cursor;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.text.ClipboardManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.SimpleCursorAdapter;
import android.util.Log;

public class HistoryActivity extends ListActivity {

	// Menu keys
	private static final int MENU_CLEAR_HISTORY = 0;

	// Dialog keys
	private static final int DIALOG_CONFIRM_HISTORY_DELETE_KEY = 0;

	// Logger tag
	private static final String TAG = "HistoryActivity";

	private TextView historyActivityTitleTV;
	private TextView historyActivitySubtitleTV;
	private ListView historyLV;
	
	private PasswordDbAdapter mDbHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		Log.d(TAG, ">>>>>> In onCreate <<<<<<");

        setContentView(R.layout.history_activity);

		// Set up the DB
		mDbHelper = new PasswordDbAdapter(this);
		mDbHelper.open();
		
		// Set up the widgets
		historyLV = getListView();
		historyActivityTitleTV = (TextView) findViewById(R.id.history_activity_titleTV);
		historyActivitySubtitleTV = (TextView) findViewById(R.id.history_activity_subtitleTV);
		
	}
	
	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG, ">>>>>> In onResume <<<<<<");
		
		// Set up the DB adapter
		if (mDbHelper.isClosed()) {
			mDbHelper.open();
		}
		
		// Populate the password history list
		populateList();
	}

	@Override
	public void onPause() {
		super.onPause();
		mDbHelper.close();
	}
	
	@Override
    public void onStop() {
    	super.onStop();
		Log.d(TAG, ">>>>>> In onStop <<<<<<");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuItem clearHistoryMI = menu.add(0, MENU_CLEAR_HISTORY, 0, R.string.history_clear_button);
		clearHistoryMI.setIcon(android.R.drawable.ic_menu_delete);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) {
        case MENU_CLEAR_HISTORY:
			AlertDialog confirmDialog = createDialog(DIALOG_CONFIRM_HISTORY_DELETE_KEY);
			confirmDialog.show();
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
       	ClipboardManager clipManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
		//String selectedPassword = v.getText();
		TextView selectedView = (TextView) v;
		clipManager.setText(selectedView.getText());
		Toast confirmCopy = Toast.makeText(getApplicationContext(), "Password copied to clipboard", Toast.LENGTH_SHORT);
		confirmCopy.show();
    }

	private void populateList() {
		//Cursor passwordsCursor = mDbHelper.getAllPasswordsDescending();
		
		Cursor passwordsCursor = mDbHelper.getAllOldPasswordsDescending();
		
		startManagingCursor(passwordsCursor);
		
		String[] from = new String[]{PasswordDbAdapter.KEY_PASSWORD};
		int[] to = new int[]{R.id.passwordRowTV};
		
		//SimpleCursorAdapter passwordsAdapter = new SimpleCursorAdapter(this, R.layout.password_row, passwordsCursor, from, to);
		PasswordHistoryCursorAdapter passwordsAdapter = new PasswordHistoryCursorAdapter(this, R.layout.password_row, passwordsCursor, from, to);
		setListAdapter(passwordsAdapter);
	}
	
	private AlertDialog createDialog(int dialogKey) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		switch(dialogKey) {
			case DIALOG_CONFIRM_HISTORY_DELETE_KEY:
				return builder.setMessage(R.string.history_confirm_history_delete_dialog)
				.setTitle(R.string.history_confirm_history_delete_dialog_title)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {	
						mDbHelper.clearPasswordsTable();
						finish();
					}					
				}).setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// Nothing
					}
				}).create();
		}
		// Return null if there's no match
		return null;	
	}
	
}