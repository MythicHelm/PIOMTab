package com.example.easytab;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Stack;

import Logic.Tab;
import Logic.Tunings;
import Logic.UserEdit;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends Activity {

	/**
	 * the list of numbers representing the possible fret numbers
	 */
	ListView mylist;
	
	/**
	 * Hold all the textviews relevant to the tab 
	 */
	ArrayList<TextView> tab = new ArrayList<TextView>();
	
	/**
	 * global to hold the fret number for the last time a list element was touched
	 */
	String currentFret = "0";
	
	/**
	 * only used once really, explained when it is assigned
	 */
	int textViewOffset;
	
	/**
	 * data structure for keeping track of all the relevant data
	 */
	Tab myTab = new Tab();
	
	/**
	 * this value is one higher than the highest number allowed to be placed on the tab
	 */
	int MAXFRET = 21;
	
	/**
	 * these are helpful in displaying the list information
	 */
	ArrayList<String> listItems = new ArrayList<String>();
	ArrayAdapter<String> adapter;
	
	/**
	 * queue to keep track of user's edits, used when undo-ing things
	 */
	Stack<UserEdit> userEdits = new Stack<UserEdit>();
	
	/**
	 * Runs automatically, was automatically added to this file
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		//set up everything
		setUpListView();
		setUpTextViewForTabs();
		
	}
	
	/**
	 * Runs automatically, was automatically added to this file
	 * sets up the menu
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	/**
	 * called automatically, deals with the item selection in the menu
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    	case R.id.menu_left:
	    		/**
	    		 * if they choose left, scroll the screen for the tab to the left
	    		 */
	    		ScrollView s = (ScrollView) findViewById(R.id.horizontalScrollView1);
	    		try{
		    		s.scrollTo(-20,  0);
	    		}
	    		catch (Exception e) {
	    			new AlertDialog.Builder(this).setTitle("Lol").setMessage("Failed!").setNeutralButton("Close", null).show();
	    		}
	    		return true;
	    		
	    	case R.id.menu_right:
	    		/**
	    		 * if they chose right, scroll the screen for the tab to the right
	    		 */
	    		new AlertDialog.Builder(this).setTitle("Lol").setMessage("Failed!").setNeutralButton("Close", null).show();
	    		
	    		return true;
	    
	        case R.id.menu_save:
	        	/**
	        	 * in the case where the user picked save, make the popup for them to
	        	 * enter a filename and save it
	        	 */
	        	startPopUp(false);
	        	return true;
	        	
	        case R.id.menu_new:
	        	/**
	        	 * in the case where the user picked new, erase the current tab
	        	 * and start a new one
	        	 */ 
	        	makeNewTab();
	        	return true;
	        	
	        case R.id.menu_load:
	        	/**
	        	 * in the case where the user picked load, start a popup that will ask the user
	        	 * for the file name
	        	 */
	        	startPopUp(true);
	        	return true;
	    }
	    return true;
	}
	
	/**
	 * This function changes the function of the back button
	 * when back is pressed, it should remove the last tab that was changed
	 * the user should be able to undo every action since the app was opened
	 */
	@Override
	public void onBackPressed(){
		
		if (!userEdits.empty()){
			UserEdit previousEdit = userEdits.pop();
			setFret(previousEdit.getTextView(), previousEdit.getIndexEdited(), previousEdit.getPrevious(), true);
			
			//if the current value is multiple digits, but the previous value was only one digit,
			//don't forget to remove that second digit!
			if (previousEdit.getPrevious().length() < previousEdit.getCurrent().length()){
				setFret(previousEdit.getTextView(), previousEdit.getIndexEdited()+1, Tab.EMPTY, true);
			}
		}
	}
	
	/**
	 * This creates items relevant for the ListView element
	 * Sets up the array adapter and onclicklistener for the list so that
	 * the buttons are clickable
	 */
	public void setUpListView(){
		
		mylist = (ListView)findViewById(R.id.listView1);
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems);
		mylist.setAdapter(adapter);
		
		//create an onclicklistener that just sets the global variable for the fret
		//to the element in the list that was touched
		mylist.setOnItemClickListener(new OnItemClickListener(){
			public void onItemClick(AdapterView<?> parent, View v, int position, long id){
				currentFret = adapter.getItem(position);
			}
		});
		
		populateListView();
	}
	
	
	/**
	 * for populating the scroll bar with numbers to resemble the frets
	 * right now, it populates it with numbers 0-20, then adds other
	 */
	public void populateListView(){
		
		for (int i = 0; i < MAXFRET; i++){
			listItems.add(Integer.toString(i));
		}
		listItems.add("-");
		listItems.add("x");
		listItems.add("\\");
		listItems.add("p");
		listItems.add("h");
		adapter.notifyDataSetChanged();
	}
	
	/**
	 * sets the fret
	 * @param v - The textview that was touched
	 * @param index - the character in the string in the textview that was touched
	 * @param fret - the number that will replace the character at index in the textview's string
	 * @param undoingAnEdit - this is true when the user pressed the back button, used so that this function
	 * 			can be called from the back, and so that the "undo" moves won't be added to the stack of moves
	 */
	public void setFret(View v, int index, String fret, boolean undoingAnEdit){
		
		TextView currentView = (TextView) findViewById(v.getId());
		
		//used a stringbuilder here because they allow for easy insertion/replacement of characters in a string
		StringBuilder currString = new StringBuilder(currentView.getText().toString());
		
		//to get an index from the id, and since the ids of the relevant textviews were sequential,
		//subtract the id of the first textview, then divide by two since they were incremented in two's
		String stringInTab = Tunings.STANDARD[(v.getId() - R.id.textView1)/2];
		
		//grab the previous value, so we can keep track of the user edits in the stack
		String previous = myTab.getNote(stringInTab, index);
		
		currString.replace(index, index+fret.length(), fret);
		myTab.addNote(stringInTab, fret, index);
		
		//don't forget to keep track of the user edits by adding to the stack
		//only add to the stack if you are NOT undoing an edit
		if (!undoingAnEdit){
			userEdits.add(new UserEdit(currentView, stringInTab, index, previous, fret));
		}
		currentView.setText(currString.toString());
	}
	
	/**
	 * Setups up the textView for the tabs, adds the onClicklistener to each textview
	 * so that it can be clickable, sets the default text to '---' for an empty space
	 * Right now, the implementation is as a grid of textviews
	 * The grid has 6 rows and 50 columns, so that's 300 textViews
	 */
	public void setUpTextViewForTabs(){
		
		//set up the ontouchlistener to find exactly which character in which textview was touched
		OnTouchListener myTouch = new OnTouchListener(){
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				Layout layout = ((TextView) v).getLayout();
			    int x = (int)event.getX();
			    int y = (int)event.getY();
			    if (layout!=null){
			        int line = layout.getLineForVertical(y);
			        int offset = layout.getOffsetForHorizontal(line, x);
			        setFret(v, offset, currentFret, false);
			    }
			    return false;
			}
		};

		//add all the textviews to our array, 6 editable (1-6) and 6 non-editable (7-12)
		tab.add((TextView) findViewById(R.id.textView1));
		tab.add((TextView) findViewById(R.id.textView2));
		tab.add((TextView) findViewById(R.id.textView3));
		tab.add((TextView) findViewById(R.id.textView4));
		tab.add((TextView) findViewById(R.id.textView5));
		tab.add((TextView) findViewById(R.id.textView6));
		tab.add((TextView) findViewById(R.id.textView7));
		tab.add((TextView) findViewById(R.id.textView8));
		tab.add((TextView) findViewById(R.id.textView9));
		tab.add((TextView) findViewById(R.id.textView10));
		tab.add((TextView) findViewById(R.id.textView11));
		tab.add((TextView) findViewById(R.id.textView12));
		
		//set the textview offset, represents the index of the first textview in the array that is not an editable one
		textViewOffset = 6;
		
		for (int i = 0; i < tab.size(); i++){
			//if the textview corresponds to one of the uneditable textviews, just set the text
			if(i >= textViewOffset){
				tab.get(i).setText(Tunings.STANDARD[i-textViewOffset] + ": ");
			}
			else{
				tab.get(i).setOnTouchListener(myTouch);
			}
		}
	}
	
	/**
	 * creates a popup that displays a editable text field and a button
	 * the user is supposed to enter some text for the file name and then click
	 * the button to save/load it
	 * As of now, doesn't do any checking of the user's input
	 * @param isALoad if this was called by pressing the 'load' button. will be true when 
	 *        called due to the 'load' button being pressed, false when called from the 'save' button
	 */
	public void startPopUp(boolean isALoad){
		LayoutInflater inflater = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View layout = inflater.inflate(R.layout.popup, (ViewGroup) findViewById(R.id.popup_element));
		
		//find the height and width of the screen
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int maxHeight = metrics.heightPixels;
		int maxWidth = metrics.widthPixels;
		
		//want the popup to be about 1/3 of the screen widthwise and 1/2 of the screen height wise
		final PopupWindow pwindo = new PopupWindow(layout, maxWidth/3, maxHeight/2, true);
		pwindo.showAtLocation(layout, Gravity.CENTER_HORIZONTAL, 0, 0);
		
		//the top button is the save/load button, so if isALoad is true, you want to
		//make sure it
		Button topButton = (Button) layout.findViewById(R.id.button1);
		if (isALoad) topButton.setText("Load");
		Button exitButton = (Button) layout.findViewById(R.id.button2);
		
		//create the onclick listener for the save button
		OnClickListener saveListener = new OnClickListener(){
			public void onClick(View v){
				EditText editTextBox = (EditText) layout.findViewById(R.id.editText1);
				String userInput = editTextBox.getText().toString();
				String filename = "mytab";
				
				//if the user actually has entered something, then change the file name to that
				//else use the default filename
				if (!userInput.isEmpty()){
					filename = userInput;
				}
				try {
					saveToDisk(filename);
					pwindo.dismiss();
		        	(new AlertDialog.Builder(MainActivity.this)).setMessage("Saved Successfully!").setNeutralButton("Close", null).show();

				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			}
		};
		
		//create the onclick listener for the exit button
		OnClickListener exitListener = new OnClickListener(){
			public void onClick(View v){
				pwindo.dismiss();
			}
		};
		
		//create the onclick listener for the load button
		OnClickListener loadListener = new OnClickListener(){
			public void onClick(View v){
				EditText editTextBox = (EditText) layout.findViewById(R.id.editText1);
				String userInput = editTextBox.getText().toString();
				String filename = "mytab";
				
				//if the user actually has entered something, then change the file name to that
				//else use the default filename
				if (!userInput.isEmpty()){
					filename = userInput;
				}
				boolean check = false;
				try {
					check = loadTab(filename+".txt");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				pwindo.dismiss();
				if (check) {
					(new AlertDialog.Builder(MainActivity.this)).setMessage("Loaded Successfully!").setNeutralButton("Close", null).show();
				}
				else{
					(new AlertDialog.Builder(MainActivity.this)).setMessage("Load failed!!!").setNeutralButton("Close", null).show(); 
				}
			}
		};
		
		//check if it's null, app dies when trying to set a listener to a null button
		if (topButton != null){
			if (isALoad){
				topButton.setOnClickListener(loadListener);
			}
			else{
				topButton.setOnClickListener(saveListener);
			}
		}
		if (exitButton != null) exitButton.setOnClickListener(exitListener);
	}
	
	/**
	 * Function for saving a tab to memory
	 * @param filename the name that the user wants to name the file
	 * @throws FileNotFoundException 
	 */
	@SuppressLint("SdCardPath")
	public void saveToDisk(String filename) throws FileNotFoundException{
		
		//create a folder on the sd card for storing all the tabs
		File myDir = new File("/sdcard/EasyTab/");
		if (!myDir.exists()){
			myDir.mkdir();
		}
		PrintWriter output = new PrintWriter("/sdcard/EasyTab/"+ filename + ".txt");
		
		int notesPerLine = 50;
		
		for (int i = 0; i * notesPerLine < myTab.getNotesAllowed(); i++){
			for (int j = 0; j < Tab.NUMSTRINGS; j++){
				output.write(Tunings.STANDARD[j] + "||");
				output.write(myTab.getSubString(Tunings.STANDARD[j], i*notesPerLine, Math.min((i+1)*notesPerLine, myTab.getNotesAllowed())));
				output.write("|\n");
			}
			output.write("\n\n");
		}
		
		output.close();
	}
	
	/**
	 * resets the tab data structure as well as the textviews
	 */
	public void makeNewTab(){
		myTab = new Tab();
		for (int i = 0; i < Tab.NUMSTRINGS; i++){
			tab.get(i).setText(R.string.initializeString);
		}
	}
	
	/**
	 * called when the user presses the 'load' button after pressing 'load' in the menu
	 * this just creates a new tab structure from a file, sets the global tab to that new one
	 * and then updates the textviews
	 * @param fileName the name of the text file containing the tab you want to open
	 * @return true when the file exists and is a tab file, false otherwise
	 * @throws IOException 
	 */
	public boolean loadTab(String fileName) throws IOException{
		File fileToOpen = new File("sdcard/EasyTab/"+fileName);
		if (!fileToOpen.exists()){
			return false;
		}
		
		myTab = Tab.createTabFromFile(fileToOpen);
		for (int i = 0; i < Tab.NUMSTRINGS; i++){
			tab.get(i).setText(myTab.getString(Tunings.STANDARD[i]));
		}
		
		return true;
	}
	
}
