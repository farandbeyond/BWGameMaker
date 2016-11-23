//Creared By:   Connor "farandbeyond" Hadley
//Edited By:	Issac "Askumi" O'Hara
//Created:		2016-11-22
//Last Edited:	2016-11-23
package ca.askumi.editor;
import javafx.scene.canvas.*;
import javafx.scene.input.*;

public class Palette{
	//tiles: tile list. can be replaced with int[][] and whatever draw method you've got
	//root: just saved into instance level stuff, for outer reference
	//tileID: replaced with your paintedTileID, or whatever you call it (the one linked to LMB-press on map)
	private int[][] tiles;
	private Canvas canvas;
	private int tileID;
	//TODO save and load canvas to project
	//TODO disable canvas when not in tile select mode
	//TODO save palette selections
	
	public Palette(int x, int y){
		//init
		tiles = new int[y][x]; //auto-sets all to 0
		tileID = 0;
		//canvas to draw
		canvas = new Canvas(x*Tile.TILESIZE, y*Tile.TILESIZE);
		GraphicsContext gc =  canvas.getGraphicsContext2D();
		//the reason root is saved at instance level. i grab the canvas in drawTiles(button,double,double) from root directly, as a kind of repaint()
		updateTiles(gc);
		canvas.setOnMouseClicked(e -> alterTileAt(e, e.getButton(), e.getX(), e.getY()));
	}
	
	//Setting and getting tileID from grid on mouse click
	private void alterTileAt(MouseEvent event, MouseButton button, double x, double y){
		int row = (int) y/32;
		int col = (int) x/32;
		if(button.equals(MouseButton.PRIMARY)){
			tileID = tiles[row][col];
			Editor.setSelectedID(tileID);
		}
		else if(button.equals(MouseButton.SECONDARY))
			tiles[row][col] = tileID;
		else if(button.equals(MouseButton.MIDDLE))
			tiles[row][col] = 0;
		updateTiles(canvas.getGraphicsContext2D());
		System.out.println(tiles[row][col]);
	}
	
	private void updateTiles(GraphicsContext g){
		//TODO fix tiles drawing over older tiles
		//draws all tiles to the canvas. basically paintComponent(Graphics)
		for(int row = 0; row < tiles.length; row++){
			for(int col = 0; col < tiles[row].length; col++){
				g.drawImage(Tile.getByID(tiles[row][col]).getImage(), (double)col*Tile.TILESIZE, (double)row*Tile.TILESIZE);
			}
		}
		//TODO draw transparency background
	}
	
	private void updateTiles(){
		updateTiles(canvas.getGraphicsContext2D());
	}
	
	public void setTileID(int newID){
		tileID = newID;
	}
	
	public Canvas getCanvas(){
		updateTiles();
		return canvas;
	}
	/*
	private class Tile extends Rectangle{
		//all this is is a basic representation of your tile class. displayNumber is tileID. the rest is display (as a rect) from an earlier version
		int displayNumber;
		Tile(int x, int y){
			super(x,y,32,32);
			super.setFill(Color.RED);
		}
		@SuppressWarnings("unused")
		Tile(int x, int y, int displayNumber){
			this(x,y);
			this.displayNumber = displayNumber;
		}
	}
	public static void main(String[] args) {
		//start the program. duh
		launch(args);
	}
	*/
}
