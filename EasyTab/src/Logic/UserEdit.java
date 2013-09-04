package Logic;

import android.widget.TextView;

public class UserEdit {

	/**
	 * the string that was edited by the user
	 */
	private String stringEdited;
	
	/**
	 * the index on the string that was edited
	 */
	private int indexEdited;
	
	/**
	 * what the fret was before the user edited it
	 */
	private String previous;
	
	/**
	 * what the fret was changed to
	 */
	private String current;
	
	/**
	 * the textview that this belongs to
	 */
	private TextView viewEdited;
	
	public UserEdit(){}
	
	/**
	 * constructor, passes in the three relevant values
	 * @param currString the String representation of the guitar string that was edited
	 * @param currIndex the index on that string that was edited
	 * @param previous the value that was previously at that point (the previous fret value)
	 */
	public UserEdit(TextView view, String currString, int currIndex, String previous, String newcurrent){
		this.viewEdited = view;
		this.stringEdited = currString;
		this.indexEdited = currIndex;
		this.previous = previous;
		this.current = newcurrent;
	}
	
	
	/**
	 * setter for the string
	 * @param currString string that was edited
	 */
	public void setStringEdited(String currString){
		this.stringEdited = currString;
	}
	
	/**
	 * setter for index
	 * @param currIndex index on the string that was edited
	 */
	public void setIndexEdited(int currIndex){
		this.indexEdited = currIndex;
	}
	
	/**
	 * setter for previous
	 * @param previous what the 'fret' was previously before the user edit
	 */
	public void setPrevious(String previous){
		this.previous = previous;
	}
	
	/**
	 * setter for textview variable
	 * @param v the textview that you want the data structure to remember
	 */
	public void setTextView(TextView v){
		this.viewEdited = v;
	}
	
	/**
	 * setter for current
	 */
	public void setCurrent(String newCurrent){
		this.current = newCurrent;
	}
	
	/**
	 * getter for string
	 * @return the string edited for this structure
	 */
	public String getStringEdited(){
		return this.stringEdited;
	}
	
	/**
	 * getter for index
	 * @return the index on the string that was edited
	 */
	public int getIndexEdited(){
		return this.indexEdited;
	}
	
	/**
	 * getter for previous
	 * @return the previous of this structure
	 */
	public String getPrevious(){
		return this.previous;
	}
	
	/**
	 * getter for the textview
	 * @return the textview of this structure
	 */
	public TextView getTextView(){
		return this.viewEdited;
	}
	
	/**
	 * getter for current
	 * @return what the fret was changed to
	 */
	public String getCurrent(){
		return this.current;
	}
	
	
}
