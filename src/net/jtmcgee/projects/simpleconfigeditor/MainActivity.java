package net.jtmcgee.projects.simpleconfigeditor;

import android.app.Activity;
import android.content.Context;
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
	private final int REQ_BROWSER = 1;	
	public final static String RECENTLY_OPENED_KEY = "recentlyOpened";
	public final static String PREFERENCES_FILE = "mahPrefs";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        fileBrowserButton = (Button) findViewById(R.id.openFileBrowserButton);
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
    	for(int i = 0; i < 4; i++) {
    		String tmp = settings.getString(RECENTLY_OPENED_KEY + i, "");
    		if(!tmp.equals("")) {
    			recentlyAdapter.add(tmp);
    		}
    	}
        recentlyList.setAdapter(recentlyAdapter);
    }

    @Override
    protected void onResume() {
    	super.onResume();
        recentlyAdapter = new ArrayAdapter<String>(this, R.layout.simplerow);
    	SharedPreferences settings = getSharedPreferences(PREFERENCES_FILE, 0);
    	for(int i = 0; i < 4; i++) {
    		String tmp = settings.getString(RECENTLY_OPENED_KEY + i, "");
    		if(!tmp.equals("")) {
    			recentlyAdapter.add(tmp);
    		}
    	}
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
    				intent.setData(data.getData());
    				startActivity(intent);
    				break;
    		}
    	}
    }
}
