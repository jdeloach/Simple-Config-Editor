package net.jtmcgee.projects.simpleconfigeditor;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class MainActivity extends Activity {

	private Button fileBrowserButton;
	private ListView recentlyList;
	private ArrayAdapter<String> recentlyAdapter;
	private final static String RECENTLY_OPENED_KEY = "recentlyOpened";
	private final static String PREFERENCES_FILE = "mahPrefs";
	private final int REQ_BROWSER = 1;	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        fileBrowserButton = (Button) findViewById(R.id.openFileBrowser);
        fileBrowserButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
				intent.setType("text/*");
				startActivityForResult(intent, REQ_BROWSER);
			}
        });
        
        recentlyList = (ListView) findViewById(R.id.recentlyList);
        recentlyList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
				Intent intent = new Intent(MainActivity.this, Editor.class);
				intent.putExtra("path", recentlyAdapter.getItem(position));
				startActivity(intent);				
			}
        });
        
        recentlyAdapter = new ArrayAdapter<String>(this, R.layout.simplerow);
    	SharedPreferences settings = getSharedPreferences(PREFERENCES_FILE, 0);
        recentlyAdapter.add(settings.getString(RECENTLY_OPENED_KEY + "3", ""));
        recentlyAdapter.add(settings.getString(RECENTLY_OPENED_KEY + "2", ""));
        recentlyAdapter.add(settings.getString(RECENTLY_OPENED_KEY + "1", ""));
        recentlyAdapter.add(settings.getString(RECENTLY_OPENED_KEY + "0", ""));
        recentlyList.setAdapter(recentlyAdapter);
    }

    @Override
    protected void onResume() {
    	super.onResume();
        recentlyAdapter = new ArrayAdapter<String>(this, R.layout.simplerow);
    	SharedPreferences settings = getSharedPreferences(PREFERENCES_FILE, 0);
        recentlyAdapter.add(settings.getString(RECENTLY_OPENED_KEY + "3", ""));
        recentlyAdapter.add(settings.getString(RECENTLY_OPENED_KEY + "2", ""));
        recentlyAdapter.add(settings.getString(RECENTLY_OPENED_KEY + "1", ""));
        recentlyAdapter.add(settings.getString(RECENTLY_OPENED_KEY + "0", ""));
        recentlyList.setAdapter(recentlyAdapter);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if(resultCode == RESULT_OK) {
    		switch (requestCode) {
    			case REQ_BROWSER:
    				Intent intent = new Intent(this, Editor.class);
    				addFileToRecently(data.getData().getPath());
    				intent.putExtra("path", data.getData().getPath());
    				startActivity(intent);
    				break;
    		}
    	}
    }
    
    private void savePref(String key, String value) {
		SharedPreferences settings = getSharedPreferences(PREFERENCES_FILE, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(key, value);
		editor.commit();
    }
    
    private String getPref(String key) {
    	SharedPreferences settings = getSharedPreferences(PREFERENCES_FILE, 0);
    	return settings.getString(key, "");
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
   	
    	
    	SharedPreferences settings = getSharedPreferences(PREFERENCES_FILE, 0);
		SharedPreferences.Editor editor = settings.edit();
		
    	if(path.equals(settings.getString(RECENTLY_OPENED_KEY + 3, "")) || path.equals(settings.getString(RECENTLY_OPENED_KEY + 2, "")) || 
    			path.equals(settings.getString(RECENTLY_OPENED_KEY + 1, "")) || path.equals(settings.getString(RECENTLY_OPENED_KEY + 0, "")))
    		return;

		
		editor.putString(RECENTLY_OPENED_KEY + "3", settings.getString(RECENTLY_OPENED_KEY + "2", ""));
		editor.putString(RECENTLY_OPENED_KEY + "2", settings.getString(RECENTLY_OPENED_KEY + "1", ""));
		editor.putString(RECENTLY_OPENED_KEY + "1", settings.getString(RECENTLY_OPENED_KEY + "0", ""));
		editor.putString(RECENTLY_OPENED_KEY + "0", path);

		editor.commit();
    }
}
