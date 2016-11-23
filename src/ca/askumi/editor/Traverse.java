//Issac "Askumi" O'Hara
//Created:		2016-11-17
//Last Edited:	2016-11-17
package ca.askumi.editor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

import javafx.scene.paint.Color;

public class Traverse {
	//This class deals with traversability on tiles
	//Maps have an int arraylist holding the ID's of these and the game has an arraylist of these objects, like how tiles work
	//Traverse objects can be made and edited on a per game basis depending on the need
	//default will have 2 objects already made, blocked and open
	
	//Static
	private static ArrayList<Traverse> allTraverses;
	public static final String TRAVERSEINFOFILE = "res/game/traverse";
	private static Color[] defColors = {Color.GREEN, Color.RED, Color.BLUE, Color.YELLOW, Color.MAGENTA, Color.ORANGE, Color.ORANGERED, Color.YELLOWGREEN, Color.BLUEVIOLET};

	public static void setup() throws Exception{
		allTraverses = new ArrayList<Traverse>();
		BufferedReader br = new BufferedReader(new FileReader(TRAVERSEINFOFILE));
		Traverse thisTraverse = null;
		String line = null;
		boolean inTraverse = false;
		boolean setUnedit = false;
		line = br.readLine();
		while(line != null){
			line = line.replace("\t", "").trim();
			//If you are inside a traverse
			if(inTraverse){
				if(line.equals("}")){
					inTraverse = false;
					if(setUnedit)
						thisTraverse.setUneditable();
					allTraverses.add(thisTraverse);
				}
				else if(line.startsWith("name:")){
					try{
						thisTraverse.setName(line.substring(6));
					}catch(Exception e){
						thisTraverse.setName("");
					}
				}
				else if(line.startsWith("desc:")){
					try{
						thisTraverse.setDescription(line.substring(6));
					}catch(Exception e){
						thisTraverse.setDescription("");
					}
				}
				else if(line.equals("uneditable")){
					setUnedit = true;
				}
			}
			//if you are not reading inside a traverse
			else{
				if(line.endsWith(":{")){
					inTraverse = true;
					setUnedit = false;
					thisTraverse = new Traverse(allTraverses.size(), null, null, true);
					thisTraverse.setID(Integer.parseInt(line.substring(0,line.length()-2)));
				}
			}
			line = br.readLine();
		}
		br.close();
	}
	public static ArrayList<Traverse> getAllTraverses(){
		return allTraverses;
	}
	public static Traverse getTraverse(int id){
		return allTraverses.get(id);
	}
	public static Traverse CreateTraverse(String newName, String newDesc){
		Traverse t = new Traverse(allTraverses.size(), newName, newDesc);
		allTraverses.add(t);
		return t;
	}
	
	//Object
	private int id;
	private String name;
	private String description;
	private boolean editable;
	private Color color;
	
	public Traverse(int newID, String newName, String newDescription){
		this(newID, newName, newDescription, false);
	}
	public Traverse(int newID, String newName, String newDescription, boolean edit){
		id = newID;
		name = newName;
		description = newDescription;
		color = defColors[id%9];
		editable = edit;
	}
	
	//Setters and getters
	public int getID(){
		return id;
	}
	public String getName(){
		return name;
	}
	public String getDescription(){
		return description;
	}
	public boolean isEditable(){
		return editable;
	}
	public Color getColor(){
		return color;
	}
	public void setID(int newID) throws Exception{
		if(!editable)
			throw new Exception("Traverse is not editable");
		id = newID;
	}
	public void setName(String newName) throws Exception{
		if(!editable)
			throw new Exception("Traverse is not editable");
		name = newName;
	}
	public void setDescription(String newDescription) throws Exception{
		if(!editable)
			throw new Exception("Traverse is not editable");
		description = newDescription;
	}
	public void setUneditable(){
		editable = false;
	}
	public void setColor(Color newColor){
		color = newColor;
	}
}
