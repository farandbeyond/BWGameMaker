//Issac "Askumi" O'Hara
//Created:		2016-11-03
//Last Edited:	2016-11-11
package ca.askumi.editor;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import javafx.scene.image.*;

public class Tile{
	
	public static final int TILESIZE = 32;
	public static final String TILEINFOFILE = "res/tiles/tileinfo";
	public static final String ERROR_IMAGE_FILE = "tiles/black.png";
	
	//STATIC TILE - Used to keep track of all tiles
	private static ArrayList<Tile> alltiles;
	
	public static void setup() throws Exception{
		alltiles = new ArrayList<Tile>();
		BufferedReader br = new BufferedReader(new FileReader(TILEINFOFILE));
		Tile thisTile = new Tile();
		String line = null;
		boolean inTile = false;
		line = br.readLine();
		while(line != null){
			line = line.replace("\t", "").trim();
			//If you are inside a tile
			if(inTile){
				if(line.equals("}")){
					inTile = false;
					alltiles.add(thisTile);
				}
				else if(line.startsWith("name:")){
					try{
						thisTile.setName(line.substring(6));
					}catch(Exception e){
						thisTile.setName("");
					}
				}
				else if(line.startsWith("desc:")){
					try{
						thisTile.setDescription(line.substring(6));
					}catch(Exception e){
						thisTile.setDescription("");
					}
				}
				else if(line.startsWith("file:")){
					thisTile.setImage(line.substring(6));
				}
				else if(line.startsWith("offx:")){
					thisTile.setOffsetX(line.substring(6));
				}
				else if(line.startsWith("offy:")){
					thisTile.setOffsetY(line.substring(6));
				}
			}
			//if you are not reading inside a tile
			else{
				if(line.endsWith(":{")){
					inTile = true;
					thisTile = new Tile();
					thisTile.setID(Integer.parseInt(line.substring(0,line.length()-2)));
				}
			}
			line = br.readLine();
		}
		br.close();
	}

	public static void save(){
		//Saves a tile to the tileinfo file
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(TILEINFOFILE, false));
			for(Tile t : alltiles){
				bw.write(String.format("%d:{%n", t.getID()));
				bw.write(String.format("\tname: %s%n", t.getName()));
				bw.write(String.format("\tdesc: %s%n", t.getDescription()));
				bw.write(String.format("\tfile: %s%n", t.getImageFile()));
				bw.write(String.format("\toffx: %d%n", t.getOffsetX()));
				bw.write(String.format("\toffy: %d%n", t.getOffsetY()));
				bw.write("}\n");
			}
			bw.close();
			setup(); //reload the data
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	
	public static void update(int tileID, Tile t) throws TileException{
		Tile thisTile = getByID(tileID);
		thisTile.setName(t.getName());
		thisTile.setDescription(t.getDescription());
		thisTile.setOffsetX(t.getOffsetX());
		thisTile.setOffsetY(t.getOffsetY());
		if(t.getImageFile()!= null){
			thisTile.setImage(t.getImageFile());
		}
		save();
	}
	
	public static void setAllTiles(ArrayList<Tile> newTileList){
		alltiles = newTileList;
	}
	public static ArrayList<Tile> getAllTiles(){
		return alltiles;
	}
	public static Tile getByID(int searchID){
		for(Tile t : alltiles)
			if(t.getID() == searchID)
				return t;
		return null;
	}
	public static Tile CreateNewTile(){
		Tile newTile = new Tile();
		newTile.generateID();
		newTile.setName("");
		newTile.setDescription("");
		alltiles.add(newTile);
		return newTile;
	}
	public static Tile CreateNewTile(String filepath){
		Tile newTile = CreateNewTile();
		try {
			newTile.setImage(filepath);
		} catch (TileException e) {
			e.printStackTrace();
		}
		return newTile;
	}

	//Remove a tile 
	public static void deleteTile(int tileID) {
		for(Tile t : alltiles){
			if(t.getID() == tileID){
				alltiles.remove(t);
				save();
				return;
			}
		}
	}
	
	
	//Class Tile
	private int id;
	private int offsetX = 0, offsetY = 0;
	private String name;
	private String description;
	private String imageFile;
	private Image image;
	
	private void setID(int newID) throws TileException{
		for(Tile t : alltiles){
			if(t.getID() == newID)
				throw new TileException("Duplicate tileID : "+newID);
		}
		id = newID;
	}
	public void generateID() {
		//find an id that is not used
		int attemptid = alltiles.size();
		if(attemptid == 0)
			attemptid++;
		boolean loop = true;
		while(loop){
			try {
				setID(attemptid++);
				loop = false;
			} catch (TileException e) {
				loop = true;
			}
		}
	}
	
	//Setters and Getters
	public void setName(String newName){
		name = newName;
	}
	public void setDescription(String newDescription){
		description = newDescription;
	}
	public void setImage(String newImagePath) throws TileException{
		Image newimage = null; //the full image
		WritableImage newimagesmall = null; //the modified 32*32 image
		try{
			newimage = new Image(newImagePath);
		}catch(Exception e){
			try{
				System.out.println("Invalid Image, using default");
				newimage = new Image(ERROR_IMAGE_FILE);
			}catch(Exception e2){
				e2.printStackTrace();
				System.exit(0);
			}
		}
		//Force images to all be the correct size
		if(newimage.getHeight() % TILESIZE != 0 || newimage.getWidth() % TILESIZE != 0){
			throw new TileException("Image is incorrect size : "+newImagePath);
		}
		imageFile = newImagePath;
		newimagesmall = new WritableImage(TILESIZE,TILESIZE);
		newimagesmall.getPixelWriter().setPixels(0, 0, TILESIZE, TILESIZE, newimage.getPixelReader(), TILESIZE*offsetX, TILESIZE*offsetY);
		image = newimagesmall;
	}
	public String getImageFile(){
		return imageFile;
	}
	public void setOffsetX(String substring){
		try{
			offsetX = Integer.parseInt(substring);
			setImage(imageFile);
		}catch(Exception e){
			System.out.println("Offset X error.");
		}
	}
	public void setOffsetY(String substring){
		try{
			offsetY = Integer.parseInt(substring);
			setImage(imageFile);
		}catch(Exception e){
			System.out.println("Offset Y error.");
		}
	}
	public void setOffsetX(int x){
		offsetX = x;
	}
	public void setOffsetY(int y){
		offsetY = y;
	}
	
	public int getID(){
		return id;
	}
	public String getName(){
		return name;
	}
	public String getDescription(){
		return description;
	}
	public Image getImage(){
		return image;
	}
	public int getOffsetX(){
		return offsetX;
	}
	public int getOffsetY(){
		return offsetY;
	}
	
	public class TileException extends Exception{
		private static final long serialVersionUID = 1L;
		public TileException(String message){
			super(message);
		}
	}
}
