package net.jbrowning.passify;

import java.util.List;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.ClipboardManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.util.Log;

public class GenerateActivity extends Activity {
    /** Called when the activity is first created. */
	
	// Log Tag
	private static final String TAG = "GenerateActivity";
	
	// Activity constants
	private static final int HISTORY_ACTIVITY = 0;
	private static final int PREFERENCES_ACTIVITY = 1;
	
	// Menu item IDs
	private static final int MENU_PREFERENCES = 0;
	
	// Prefs
	private SharedPreferences sharedPrefs;
	private static final String PREFS_NAME = "Passify-Generate-Prefs";
	private static final String PREFS_PASS_LENGTH_KEY = "passLength";
	private static final String PREFS_MIXED_CASE_KEY = "mixedCase";
	private static final String PREFS_SYMBOLS_KEY = "symbols";
	private static final String PREFS_NUMBERS_KEY = "numbers";
	private static final String PREFS_LAST_PASS_KEY = "lastPass";
	
	// Dialog IDs
	private static final int PASS_LENGTH_DIALOG = 1;
	
	// Default values
	private boolean mMixedCase;
	private boolean mSymbols;
	private boolean mNumbers;
	private int mPassLength;
	private String mCurrentPass = "";
	private List<String> mHistoryList;
	
	private boolean mKeepHistory;
	private int mHistoryLimit;
	
    // Set up widgets
    private Button generateBT;
	private Button copyBT;
    private Spinner passLengthSP;
	private Button historyBT;
    private TextView passTV;
    private CheckBox mixedCaseCB;
    private CheckBox symbolsCB;
    private CheckBox numbersCB;
	
	// DB Helper
	private PasswordDbAdapter mDbHelper;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.generate_activity);
		Log.d(TAG, ">>>>>> In onCreate <<<<<<");
		
		// Set default pref values on first launch
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		
		// Get shared preferences
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        
		// Set up initial history list
		//mHistoryList = new ArrayList<String>();
		
		// Set up DB connection
		mDbHelper = new PasswordDbAdapter(this);
		mDbHelper.open();

        // Set up all widgets
        passTV = (TextView) findViewById(R.id.passTV);
        passLengthSP = (Spinner) findViewById(R.id.passLengthSP);
		copyBT = (Button) findViewById(R.id.copyBT);
        generateBT = (Button) findViewById(R.id.generateBT);
        mixedCaseCB = (CheckBox) findViewById(R.id.mixedCaseCB);
        symbolsCB = (CheckBox) findViewById(R.id.symbolsCB);
        numbersCB = (CheckBox) findViewById(R.id.numbersCB);
		historyBT = (Button) findViewById(R.id.historyBT);
        
        // Get prefs for the generation options
        SharedPreferences generatePrefs = getSharedPreferences(PREFS_NAME, 0);
        mPassLength = generatePrefs.getInt(PREFS_PASS_LENGTH_KEY, 8);
        mMixedCase = generatePrefs.getBoolean(PREFS_MIXED_CASE_KEY, true);
        mSymbols = generatePrefs.getBoolean(PREFS_SYMBOLS_KEY, false);
        mNumbers = generatePrefs.getBoolean(PREFS_NUMBERS_KEY, false);
        
        // Set initial state of widgets
        mixedCaseCB.setChecked(mMixedCase);
        symbolsCB.setChecked(mSymbols);
        numbersCB.setChecked(mNumbers);
		//passLengthBT.setText(String.valueOf(mPassLength));
        
		// Set up the password length spinner
		ArrayAdapter<CharSequence> passLengthAdapter = ArrayAdapter.createFromResource(this,
			R.array.pass_length_items, android.R.layout.simple_spinner_item);
		passLengthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		passLengthSP.setAdapter(passLengthAdapter);
		passLengthSP.setSelection(mPassLength-4);
        
		// Set up listeners
        generateBT.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
				genPass();
        	}
        });

		copyBT.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				ClipboardManager clipManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
				clipManager.setText(mCurrentPass);
				Toast confirmCopy = Toast.makeText(getApplicationContext(), R.string.pass_copied_toast, Toast.LENGTH_SHORT);
				confirmCopy.show();
			}
		});
        
		historyBT.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				pushHistory();
			}
		});

		passLengthSP.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				TextView selectedTV = (TextView) view;
				if (selectedTV != null) {
					int prevPassLength = mPassLength;
					mPassLength = Integer.parseInt((String) selectedTV.getText());
					if (prevPassLength != mPassLength) {
						genPass();
					}
				}
			}
			
			public void onNothingSelected(AdapterView<?> parent) {
				// Nothing
			}
		});
        
        mixedCaseCB.setOnCheckedChangeListener(new OnCheckedChangeListener() {
        	public void onCheckedChanged(CompoundButton button, boolean newValue) {
        		//mixedCaseCB.setChecked(newValue);
        		mMixedCase = newValue;
        		genPass();
        	}
        });
        
        symbolsCB.setOnCheckedChangeListener(new OnCheckedChangeListener() {
        	public void onCheckedChanged(CompoundButton button, boolean newValue) {
        		//symbolsCB.setChecked(newValue);
        		mSymbols = newValue;
        		genPass();
        	}
        });
        
        numbersCB.setOnCheckedChangeListener(new OnCheckedChangeListener() {
        	public void onCheckedChanged(CompoundButton button, boolean newValue) {
        		//mixedCaseCB.setChecked(newValue);
        		mNumbers = newValue;
        		genPass();
        	}
        });
        
		// Generate initial pass if activity wasn't automatically restarted
		final String lastPass = (String) getLastNonConfigurationInstance();
		if (lastPass == null & mCurrentPass.length() == 0) {
			Log.d(TAG, "Not setting previous pass");
        	genPass();
		} else {
			Log.d(TAG, "Setting previous pass");
			setPassword(lastPass);
		}
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuItem preferencesMI = menu.add(0, MENU_PREFERENCES, 0, R.string.generate_menu_preferences);
		preferencesMI.setIcon(android.R.drawable.ic_menu_preferences);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) {
        case MENU_PREFERENCES:
			pushPreferences();
            return true;
        }
       
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public void onResume() {
    	/** called when the activity is being resumed from a "paused" state" */
    	super.onResume();
		Log.d(TAG, ">>>>>> In onResume <<<<<<");
		
		// Restore the password history list
		/* mHistoryList.clear();
		try {
			mHistoryList.addAll(mDbHelper.getAllPasswordsAsList());
		} catch (java.lang.IllegalStateException e) {
			mDbHelper.open();
			mHistoryList.addAll(mDbHelper.getAllPasswordsAsList());
		}
		*/
		
		if (mDbHelper.isClosed()) {
			mDbHelper.open();
		}
		
		/*
		int passCount = 0;
		try {
			passCount = mDbHelper.getCount();
		} catch (java.lang.IllegalStateException e) {
			mDbHelper.open();
			passCount = mDbHelper.getCount();
		}*/
		// Disable the history button if there aren't enough items
		if (mDbHelper.getCount() <= 1) {
			historyBT.setEnabled(false);
		} 
    }
    
	@Override
	public Object onRetainNonConfigurationInstance() {
		/** Called when the activity os restarted automatically by Android */
		Log.d(TAG, ">>>>>> In onRetainNonConfigurationInstance <<<<<<");
		SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0);
		return prefs.getString(PREFS_LAST_PASS_KEY, null);
	}
    
    @Override
    public void onStop() {
    	super.onStop();
		Log.d(TAG, ">>>>>> In onStop <<<<<<");		
		
    }
    
	@Override
	public void onPause() {
		super.onPause();
		Log.d(TAG, ">>>>>> In onPause <<<<<<");
		SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0);
    	SharedPreferences.Editor prefsEditor = prefs.edit();
    	prefsEditor.putInt(PREFS_PASS_LENGTH_KEY, mPassLength);
    	prefsEditor.putBoolean(PREFS_MIXED_CASE_KEY, mMixedCase);
    	prefsEditor.putBoolean(PREFS_SYMBOLS_KEY, mSymbols);
    	prefsEditor.putBoolean(PREFS_NUMBERS_KEY, mNumbers);
		prefsEditor.putString(PREFS_LAST_PASS_KEY, mCurrentPass);
    	prefsEditor.commit();
		
		// Close the DB connection
		mDbHelper.close();
	}
    
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		switch(requestCode) {
			/*case PREFERENCES_ACTIVITY:
				int histSize = Integer.parseInt(sharedPrefs.getString(PreferencesActivity.PREFS_HISTORY_LIMIT_KEY, "5"));
				Log.d(TAG, "In onActivityResult - histSize is " + histSize);
				while (histSize < mHistoryList.size()) {
					mHistoryList.remove(0);
				}*/
		}
	}
	
	private void pushHistory() {
		Intent intent = new Intent(this, HistoryActivity.class);
		startActivityForResult(intent, HISTORY_ACTIVITY);
	}
	
	private void pushPreferences() {
		Intent intent = new Intent(this, PreferencesActivity.class);
		startActivityForResult(intent, PREFERENCES_ACTIVITY);
	}

    private void genPass() {
		String newPass = PasswordGenerator.generate(mPassLength, mMixedCase, mSymbols, mNumbers);
		boolean keepHistory = sharedPrefs.getBoolean(PreferencesActivity.PREFS_HISTORY_KEEP_KEY, true);
		setPassword(newPass, keepHistory);
		if (mDbHelper.getCount() > 1 & historyBT.isEnabled() == false) {
			historyBT.setEnabled(true);
		}
    }
    
    private void setPassword(String newPass) {
    	setPassword(newPass, false);
    }
    
    private void setPassword(String newPass, boolean addToHistory) {
    	mCurrentPass = newPass;
		passTV.setText(mCurrentPass);
		if (addToHistory) {
			mDbHelper.createPassword(mCurrentPass);
		}
    }
}