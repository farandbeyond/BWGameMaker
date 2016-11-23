//Issac "Askumi" O'Hara
//Created:		2016-11-03
//Last Edited:	2016-11-17
package ca.askumi.editor;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Map {
	
	private static final String MAPPATH = "res/maps/";
	
	//Object
	private String name;
	private int rows,cols;
	private ArrayList<int[][]> layers = new ArrayList<int[][]>();
	private ArrayList<String> layerNames = new ArrayList<String>();
	private int[][] traversability;
	
	//Constructor used to create new maps
	public Map(String mapname, int width, int height){
		setName(mapname);
		rows = height;
		cols = width;
		layers = new ArrayList<int[][]>();
		addLayer(new int[rows][cols], "Background");
		traversability = new int[rows][cols];
	}
	
	//Constructor used to load existing maps
	public Map(String mapname, int width, int height, ArrayList<int[][]> tileIDs, ArrayList<String> layernames, int[][] traverse){
		setName(mapname);
		rows = height;
		cols = width;
		layers = tileIDs;
		layerNames = layernames;
		traversability = traverse;
	}
	//Layering
	public void addLayer(int[][] tileID, String name, int[][] traverse){
		layers.add(tileID);
		layerNames.add(name);
		traversability = traverse;
	}
	public void addLayer(int[][] tileID, String name){
		addLayer(tileID, name, new int[rows][cols]);
	}
	public void addLayer(String name){
		addLayer(new int[rows][cols], name);
	}
	public void setLayer(int layer, int[][] tileID){
		layers.set(layer, tileID);
	}
	public void setLayerName(int layer, String name){
		layerNames.set(layer, name);
	}
	public int[][] getLayer(int layer){
		return layers.get(layer);
	}
	public String getLayerName(int layer){
		return layerNames.get(layer);
	}
	public void deleteLayer(int layer) {
		layers.remove(layer);
		layerNames.remove(layer);
	}
	
	//Traversability
	public int[][] getTraversability(){
		return traversability;
	}
	public void setTraverse(int row, int col, int value){
		traversability[row][col] = value;
	}
	public int getTraverse(int row, int col){
		return traversability[row][col];
	}
	
	//Reading the map
	public int getID(int layer, int row, int col){
		return layers.get(layer)[row][col];
	}
        
        //Confirmation
        protected boolean isOutOfBounds(int row, int col){
            return row < 0 || col < 0 || row >= rows || col >= cols;
        }
	//Editing the map
	public void setTile(int layer, int row, int col, int newTileID){
            if(!isOutOfBounds(row,col))
		layers.get(layer)[row][col] = newTileID;
	}
	//Calls setTile() on a group of tiles all with the same ID
	private ArrayList<Integer> checkTileList;
	private void SetTiles(int layer, int row, int col, int newTileID, int startingID){
		//TODO add a toggleable setting to include diagnols, not only adjacent tiles
		int thisloc = row*10000+col;
		//stop if already checked this square or square is not in group
		if(isOutOfBounds(row,col) || checkTileList.contains(thisloc) || layers.get(layer)[row][col] != startingID)
			return;
		setTile(layer, row, col, newTileID);
		checkTileList.add(thisloc);
		SetTiles(layer, row + 1,col, newTileID, startingID);
		SetTiles(layer, row,col + 1, newTileID, startingID);
		SetTiles(layer, row - 1,col, newTileID, startingID);
		SetTiles(layer, row,col - 1, newTileID, startingID);
	}
	public void MassSetTile(int layer, int row, int col, int newTileID){
		int startingID = layers.get(layer)[row][col];
		checkTileList = new ArrayList<Integer>();
		SetTiles(layer, row, col, newTileID, startingID);
	}
	//traverse set mass
	private void setTraverses(int row, int col, int newTileID, int startingID){
		//TODO add a toggleable setting to include diagnols, not only adjacent tiles
		//TODO clean up these functions
		int thisloc = row*10000+col;
		if(row < 0 || col < 0 || row >= rows || col >= cols || checkTileList.contains(thisloc) || traversability[row][col] != startingID)
			return;
		setTraverse(row, col, newTileID);
		checkTileList.add(thisloc);
		setTraverses(row + 1,col, newTileID, startingID);
		setTraverses(row,col + 1, newTileID, startingID);
		setTraverses(row - 1,col, newTileID, startingID);
		setTraverses(row,col - 1, newTileID, startingID);
	}
	public void MassSetTraverse(int row, int col, int newTileID){
		int startingID = traversability[row][col];
		checkTileList = new ArrayList<Integer>();
		setTraverses(row, col, newTileID, startingID);
	}
	
	//Loading and Saving
	public static Map load(String mapname){
		int retX = 0;
		int retY = 0;
		ArrayList<int[][]> tiles = new ArrayList<int[][]>();
		ArrayList<String> names = new ArrayList<String>();
		int[][] traverse = null;
		
		BufferedReader br;
		try {
			if(!mapname.endsWith(".map"))
				mapname = mapname + ".map";
			br = new BufferedReader(new FileReader(String.format("%s%s", MAPPATH, mapname)));
			String line = br.readLine();
			//enums used to make it easier to read the loop
			final int BLOCK_OUTSIDE = 0;
			final int BLOCK_META = 1;
			final int BLOCK_LAYER = 2;
			final int BLOCK_TRAVERSE = 3;
			
			int layernum = 0;
			int block = BLOCK_OUTSIDE; //start outside a block
			while(line != null){
				line = line.trim();
				switch(block){
				case BLOCK_OUTSIDE:{ //if we are searching for a block	
					if(line.startsWith("meta:{"))
						block = BLOCK_META;
					else if(line.startsWith("layer")){
						layernum = Integer.parseInt(line.substring(5, line.length()-2));
						tiles.add(new int[retY][retX]);
						block = BLOCK_LAYER;
					}
					else if(line.startsWith("traverse")){
						traverse = new int[retY][retX];
						block = BLOCK_TRAVERSE;
					}
					break;
				}
				case BLOCK_META:{ // if we are in the meta block
					if(line.startsWith("x:"))
						retX = Integer.parseInt(line.substring(2));
					else if(line.startsWith("y:"))
						retY = Integer.parseInt(line.substring(2));
					else if(line.startsWith("}")){
						block = BLOCK_OUTSIDE;
					}
					break;
				}
				case BLOCK_LAYER:{ // if we are in a layer block
					if(line.startsWith("name:")){
						names.add(line.substring(5));
					}
					if(line.startsWith("row")){
						int rownum = Integer.parseInt(line.substring(3,line.indexOf(":"))); //the row number is the substring between 'row' and ':'
						String[] values = line.substring(line.indexOf(":")+1).split(",");
						int colnum = 0;
						for(String s : values){
							tiles.get(layernum)[rownum][colnum] = Integer.parseInt(s);
							colnum++;
						}
					}
					else if(line.startsWith("}")){
						block = BLOCK_OUTSIDE;
					}
					break;
				}
				case BLOCK_TRAVERSE:{ // if we are in the traverse block
					if(line.startsWith("row")){
						int rownum = Integer.parseInt(line.substring(3,line.indexOf(":"))); //the row number is the substring between 'row' and ':'
						String[] values = line.substring(line.indexOf(":")+1).split(",");
						int colnum = 0;
						for(String s : values){
							traverse[rownum][colnum] = Integer.parseInt(s);
							colnum++;
						}
					}
					else if(line.startsWith("}")){
						block = BLOCK_OUTSIDE;
					}
				} //end case
				} //end switch
				line = br.readLine();
			}
			br.close();
			return new Map(mapname, retX, retY, tiles, names, traverse);
		} catch (IOException e) {
			// TODO map not found or map failed to load
			e.printStackTrace();
		}
		return null;
	}
	
	public void save(){
		try {
			if(!name.endsWith(".map"))
				name = name + ".map";
			BufferedWriter bw = new BufferedWriter(new FileWriter(String.format("%s%s", MAPPATH, name), false));
			bw.write("meta:{\n");
			bw.write(String.format("\tx:%s%n",cols));
			bw.write(String.format("\ty:%s%n",rows));
			bw.write("}\n");
			for(int l = 0; l < layers.size(); l++){
			bw.write(String.format("layer%s:{%n", l));
			bw.write(String.format("\tname:%s%n", getLayerName(l)));
				for(int row = 0; row < rows; row++){
					String line = String.format("\trow%s:",row);
					for(int col = 0; col < cols; col++){
						line += layers.get(l)[row][col]+",";
					}
					line = line.substring(0, line.length()-1);
					bw.write(line+"\n");	
				}
				bw.write("}\n");
			}
			bw.write("traverse:{\n");
			for(int row = 0; row < rows; row++){
				String line = String.format("\trow%s:",row);
				for(int col = 0; col < cols; col++){
					line += getTraverse(row, col)+",";
				}
				line = line.substring(0, line.length()-1);
				bw.write(line+"\n");	
			}
			bw.write("}\n");
			bw.close();
		} catch (IOException e) {
			//TODO failed to save map message
			e.printStackTrace();
		}
	}
	
	//Setters and Getters
	public String getName(){
		return name;
	}
	public int getX(){
		return cols;
	}
	public int getY(){
		return rows;
	}
	public void setX(int newX){
		cols = newX;
	}
	public void setY(int newY){
		rows = newY;
	}
	public int getWidth(){
		return cols * Tile.TILESIZE;
	}
	public int getHeight(){
		return rows * Tile.TILESIZE;
	}
	public void setName(String newName){
		//TODO check if map with name already exists
		name = newName;
	}
	public int getLayerCount(){
		return layers.size();
	}
}
