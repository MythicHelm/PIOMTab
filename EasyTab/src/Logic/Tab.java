package Logic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;




public class Tab {
	
	//variables of the class Tab
	String[] tuning;
	HashMap<String, ArrayList<String>> notes;
	int numNotesAllowed;
	
	//useful for notifying that no frets should be played for the given note
	public static String EMPTY = "-";
	public static int NUMSTRINGS = 6;
	public static int DEFAULTNOTESALLOWED = 100;
	
	/**
	 * simple constructor, sets things to defaults
	 * plan to possibly extend this to allow for tunings other than standard
	 */
	public Tab(){
		this.tuning = Tunings.STANDARD;
		this.notes = new HashMap<String, ArrayList<String> >();
		
		//initialize the six strings arrays
		for(int i = 0; i < NUMSTRINGS; i++){
			String currString = this.tuning[i];
			this.notes.put(currString, new ArrayList<String>());
		}
		
		//as of right now, I have the max number of notes to be 50
		//initializes them all to default empty value
		
		this.numNotesAllowed = DEFAULTNOTESALLOWED;
		for (ArrayList<String> currString : this.notes.values()){
			for(int i = 0; i < this.numNotesAllowed; i++){
				currString.add(EMPTY);
			}
		}
		
	}

	/**
	 * adds the given note
	 * @param string - the string representation of the guitar string on which this note was played
	 * @param fret - the string fret on which this note was played
	 * @param index - the index of the given note (e.g. is it the first quarter note in the second measure)
	 */
	public void addNote(String string, String fret, int index){
		ArrayList<String> currString = this.notes.get(string);
		while (index >= this.numNotesAllowed){
			this.resize();
		}
		currString.set(index, fret);
	}
	
	/**
	 * removes the given note
	 * @param string - the string representation of the guitar string on which the to-be removed note was played
	 * @param index - the index of the to-be removed note (e.g. is it the first quarter note in the second measure)
	 */
	public void removeNote(String string, int index){
		ArrayList<String> currString = this.notes.get(string);
		while (index >= this.numNotesAllowed){
			this.resize();
		}
		currString.set(index, EMPTY);
	}
	
	/**
	 * returns the note at the location represented by the inputs
	 */
	public String getNote(String currString, int index){
		return this.notes.get(currString).get(index);
	}
	
	/**
	 * returns the entire line for the given string
	 * @param string the string representation of the guitar string requested
	 * @return 
	 */
	public String getString(String currString){
		return getSubString(currString, 0, numNotesAllowed);
	}
	
	/**
	 * returns only part of the notes for the given string
	 * @param currString the string requested
	 * @param start the starting index on the string of the requested notes
	 * @param end the ending index on the string of the requested notes
	 * @return
	 */
	public String getSubString(String currString, int start, int end){
		String result = "";
		ArrayList<String> reqString = this.notes.get(currString);
		for (int i = start; i < end; i++){
			String currChar = reqString.get(i);
			result+=currChar;
		}
		
		return result;
	}
	
	/**
	 * getter for the number of notes allowed on the tab
	 * @return this tabs max number of notes
	 */
	public int getNotesAllowed(){
		return this.numNotesAllowed;
	}
	
	/**
	 * if the current max amount of notes isn't enough, then this function will add 50 more spaces for notes
	 * just resizes the arraylist for each string
	 * @param
	 */
	public void resize(){
		int additionalSpace = 50;
		for (ArrayList<String> currString : this.notes.values()){
			for (int i = 0; i < additionalSpace; i++){
				currString.add(EMPTY);
			}
		}
		this.numNotesAllowed += additionalSpace;
	}
	
	
	/**
	 * saves the tab to a file
	 * @throws FileNotFoundException 
	 */
	public void saveToFile() throws FileNotFoundException{
		
		PrintWriter file = new PrintWriter("mytab.txt");
		file.write(printHelper());
		file.close();
	}
	
	/**
	 * prints the tab, useful for debugging
	 */
	public void print(){
		System.out.print(printHelper());
	}
	
	/**
	 * prints the tab, useful for debugging
	 * @return res the tab in string form, formatted nicely
	 */
	public String printHelper(){
		String res = "";
		
		for (int i = 0; i < this.numNotesAllowed; i++){
			res+=("\t" + i);
		}
		res+=("\n");
		
		for (int stringIndex = 0; stringIndex < NUMSTRINGS; stringIndex++){
			res+=(this.tuning[stringIndex] + "\t");
			ArrayList<String> currString = this.notes.get(this.tuning[stringIndex]);
			for (int i = 0; i < this.numNotesAllowed; i++){
				String currNote = currString.get(i);
				if (currNote == EMPTY){
					res+=("---\t");
				}
				else{
					res+=(currNote + "\t");
				}
			}
			res+=("\n");
		}
		
		return res;
		
	}
	
	/**
	 * creates a new Tab data structure from the given file
	 * Right now, it assumes too much about the structure of the text in the file
	 * It really is only capable of loading from files this app had saved
	 * @param file the file you want to load from
	 * @throws IOException 
	 */
	public static Tab createTabFromFile(File file) throws IOException{
		Tab retTab = new Tab();
		
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line;
		
		/**
		 * this variable is used as an offset variable
		 * the tab on the android device basically extends as far as it is long horizontally,
		 * but when it saves it to a file, it splits up the lines, so when parsing it we're going to have to do
		 * say half of the tab for string 'e', half for string 'B', and so on... then we'll come back
		 * and do the second half of 'e'.
		 */
		int tabIndex = 0;
		boolean incrementTabIndex = false;
		while ((line = br.readLine()) != null) {
		   String currString = "";
			if (line.contains("e")){
			   currString="e";
		   }
		   else if (line.contains("B")){
			   currString="B";
		   }
		   else if (line.contains("G")){
			   currString="G";
		   }
		   else if (line.contains("D")){
			   currString="D";
		   }
		   else if (line.contains("A")){
			   currString="A";
		   }
		   else if (line.contains("E")){
			   currString="E";
			   incrementTabIndex = true;
		   }
			
			//make sure you only add notes when you are actually looking at the tab,
			//and not just the empty space
			if (!currString.isEmpty()){
				//start at 3, end when you hit a '|' since the tabs are saved to file like: 'e||------|'
				for(int i = 3; line.charAt(i) != '|'; i++){
					retTab.addNote(currString, line.substring(i, i+1), i-3+(50*tabIndex));
				}
			}
			
			//As of now, you can count on the tab being formatted a certain way,
			//so every time you see the sixth string, you want to increment the tab index
			if (incrementTabIndex){
				tabIndex++;
				incrementTabIndex = false;
			}
			
		}
		br.close();
		
		return retTab;
	}
	
}
