package net.jtmcgee.projects.simpleconfigeditor;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Properties;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class Editor extends Activity {

	private ListView mOptionsList;
	private Properties mConfigValues;
	private ArrayAdapter<String> mAdapter;
	private String mPath;
	private String APPTAG = "configEditor";
	private boolean saved = true;
	
	private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			final String key = mAdapter.getItem(position);
			String value = mConfigValues.getProperty(mAdapter.getItem(position));
			final String onValue = "1"; // for future expandability, e.g. t/f, true/false, what ever
			final String offValue = "0";
			
			if(value.equals(onValue) || value.equals(offValue)) {
				final Dialog settingsDialog = new Dialog(Editor.this);
				//settingsDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
				settingsDialog.setContentView(R.layout.dialog_radiobuttons);
				final RadioButton onButton = (RadioButton) settingsDialog.findViewById(R.id.onButton);
				final RadioButton offButton = (RadioButton) settingsDialog.findViewById(R.id.offButton);
				((TextView) settingsDialog.findViewById(R.id.dialogTitle)).setText(key);
				settingsDialog.show();
				
				if(value.equals(onValue))
					onButton.setChecked(true);
				else
					offButton.setChecked(true);
				
				((Button) settingsDialog.findViewById(R.id.ok)).setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) { // Ok, save value
						if(onButton.isChecked())
							mConfigValues.setProperty(key, onValue);
						else
							mConfigValues.setProperty(key, offValue);
						settingsDialog.dismiss();
						saved = false;
					}
				});
				
				((Button) settingsDialog.findViewById(R.id.cancel)).setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) { // Cancel
						settingsDialog.dismiss();
					}
				});
			
			} else {
				final Dialog settingsDialog = new Dialog(Editor.this);
				//settingsDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
				settingsDialog.setContentView(R.layout.dialog_textfield);
				final EditText field = (EditText) settingsDialog.findViewById(R.id.textField);
				((TextView) settingsDialog.findViewById(R.id.dialogTitle)).setText(key);
				field.setText(value);
				
				((Button) settingsDialog.findViewById(R.id.ok)).setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) { // Ok, save value
						mConfigValues.setProperty(key, field.getText().toString());
						settingsDialog.dismiss();
						saved = false;
					}
				});
				
				((Button) settingsDialog.findViewById(R.id.cancel)).setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) { // Cancel
						settingsDialog.dismiss();
					}
				});
				
				settingsDialog.show();
			}
		}
	};
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_editor);
        mOptionsList = (ListView) findViewById(R.id.optionsList);
        mOptionsList.setOnItemClickListener(mOnItemClickListener);
        Intent intent = getIntent();
        if(intent.getExtras() != null && intent.getExtras().containsKey("path")) {
        	mPath = intent.getExtras().getString("path");
        	loadConfigValuesToList(mPath);
        } else if (intent.getData() != null) { // was loaded in astro or something
        	mPath = intent.getData().getPath();
        	loadConfigValuesToList(mPath);
        } else {
        	Log.e(APPTAG, "Editor -- Failed to load path variable.");
        	Toast.makeText(this, "Failed to load config file.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_editor, menu);
        return true;
    }
    
    @SuppressWarnings("deprecation")
	@Override
    public void onBackPressed() {
    	if(!saved) {
    		AlertDialog dialog = new AlertDialog.Builder(this).create();
    		dialog.setTitle("Finish");
    		dialog.setMessage("You have unsaved changes. Save them?");
    		dialog.setButton("Save", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					onSave();
					finish();
				}
			});
    		dialog.setButton2("Discard", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					finish();	
				}
			});
    		dialog.show();
    	} else 
    		finish();
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()) {
    		case R.id.menu_save:
    			onSave();
    			return true;
    		case R.id.menu_close:
    			finish();
    			return true;
    	}
    	return false;
    }
    
    /**
     * Load config files first into a Java Properties object, and from there, an Array to be put in the 
     * Array adaptor and then the List.
     * @param path Path to a parseable config file.
     */
    @SuppressWarnings("unchecked")
	private void loadConfigValuesToList(String path) {
		addFileToRecently(path);
        try {
        	mConfigValues = new Properties();
        	mConfigValues.load(new FileInputStream(path));
        	
        	Enumeration<String> keys = (Enumeration<String>) mConfigValues.propertyNames();
        	mAdapter = new ArrayAdapter<String>(this, R.layout.simplerow);
 
        	while(keys.hasMoreElements()) {
        		mAdapter.add(keys.nextElement());
        	}
        	
        	mOptionsList.setAdapter(mAdapter);
        } catch (Exception e) {
        	Log.e(APPTAG, "Editor -- Failed to load config file.", e);
        }
    }
    
    /**
     * Saves the Properties out to the same location.
     */
    private void onSave() {
    	saved = true;
    	try {
    		PrintStream fOut = new PrintStream(new FileOutputStream(mPath));
    		
    		Enumeration<Object> enums = mConfigValues.keys();
    		while(enums.hasMoreElements()) {
    			String key = (String) enums.nextElement();
    			fOut.println(key + " " + mConfigValues.getProperty(key));
    		}
    		
    		fOut.close();
    	} catch (IOException ioe) {
    		Log.e(APPTAG, "Failed to save.", ioe);
    		Toast.makeText(this, "Failed to save settings.", Toast.LENGTH_LONG).show();
		}
    }
    
    private void addFileToRecently(String path) {
    	/*    	StringTokenizer st = new StringTokenizer(getPref(RECENTLY_OPENED_KEY), ",");
    	    	StringBuilder results = new StringBuilder();
    	    	
    	    	if(st.countTokens() == 5) {
    	    		st.nextToken();
    	    		
    	    		while(st.hasMoreTokens()) {
    	    			results.append("," + st.nextToken());
    	    		}
    	    		results.append("," + getPref(RECENTLY_OPENED_KEY));
    	    		savePref(RECENTLY_OPENED_KEY, results.toString());
    	    	} else {
    	    		savePref(RECENTLY_OPENED_KEY, getPref(RECENTLY_OPENED_KEY) + "," + path);
    	    	}*/
    	   	
    	    	
    	    	SharedPreferences settings = getSharedPreferences(MainActivity.PREFERENCES_FILE, 0);
    			SharedPreferences.Editor editor = settings.edit();
    			
    	    	if(path.equals(settings.getString(MainActivity.RECENTLY_OPENED_KEY + 3, "")) || path.equals(settings.getString(MainActivity.RECENTLY_OPENED_KEY + 2, "")) || 
    	    			path.equals(settings.getString(MainActivity.RECENTLY_OPENED_KEY + 1, "")) || path.equals(settings.getString(MainActivity.RECENTLY_OPENED_KEY + 0, "")))
    	    		return;

    			
    			editor.putString(MainActivity.RECENTLY_OPENED_KEY + "3", settings.getString(MainActivity.RECENTLY_OPENED_KEY + "2", ""));
    			editor.putString(MainActivity.RECENTLY_OPENED_KEY + "2", settings.getString(MainActivity.RECENTLY_OPENED_KEY + "1", ""));
    			editor.putString(MainActivity.RECENTLY_OPENED_KEY + "1", settings.getString(MainActivity.RECENTLY_OPENED_KEY + "0", ""));
    			editor.putString(MainActivity.RECENTLY_OPENED_KEY + "0", path);

    			editor.commit();
    }
}
