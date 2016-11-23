//Creared By:   Connor "farandbeyond" Hadley
//Edited By:	Issac "Askumi" O'Hara
//Created:		2016-11-22
//Last Edited:	2016-11-23
package ca.askumi.editor;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import javafx.scene.paint.Color;
import javafx.scene.canvas.*;
import javafx.scene.input.*;

public class Palette{
        //io info
        private static final String PALETTEPATH = "res/tiles/tilesets/";
        private static String loadedPalette = "palette.pal";//this can be changed later when we get to loading multiple
	//tiles: tile list. can be replaced with int[][] and whatever draw method you've got
	//root: just saved into instance level stuff, for outer reference
	//tileID: replaced with your paintedTileID, or whatever you call it (the one linked to LMB-press on map)
	private final int cols;
	private final int rows;
	//private final Color transparentColor = Color.GREEN;
        private int tileBG;
	private int[][] tiles;
	private Canvas canvas;
	private int tileID;
	//TODO disable canvas when not in tile select mode
	//TODO save palette selections
	//TODO option to add entire tile chunks to palette (for multi-tile tiles)
	
	public Palette(int x, int y){
		//init
		cols = x;
		rows = y;
		tiles = new int[y][x]; //auto-sets all to 0
		tileID = 0;
		//canvas to draw
		canvas = new Canvas(x*Tile.TILESIZE, y*Tile.TILESIZE);
		GraphicsContext gc =  canvas.getGraphicsContext2D();
		//the reason root is saved at instance level. i grab the canvas in drawTiles(button,double,double) from root directly, as a kind of repaint()
		updateTiles(gc);
		canvas.setOnMouseClicked(e -> alterTileAt(e.getButton(), e.getX(), e.getY()));
	}
	
	//Setting and getting tileID from grid on mouse click
	private void alterTileAt( MouseButton button, double x, double y){
		int row = (int) y/32;
		int col = (int) x/32;
		if(button.equals(MouseButton.PRIMARY)){
			tileID = tiles[row][col];
			Editor.setSelectedID(tileID);
		}
		else if(button.equals(MouseButton.SECONDARY))
			setTileAt(row,col,tileID);
		else if(button.equals(MouseButton.MIDDLE))
			setBGTile(tileID);
		updateTiles(canvas.getGraphicsContext2D());
		System.out.println(tiles[row][col]);
	}
	
	private void updateTiles(GraphicsContext g){
		g.clearRect(0, 0, cols * Tile.TILESIZE, rows * Tile.TILESIZE);
		g.setFill(Color.BLACK);
		g.fillRect(0, 0, cols * Tile.TILESIZE, rows * Tile.TILESIZE);
		//draws all tiles to the canvas. basically paintComponent(Graphics)
		for(int row = 0; row < tiles.length; row++){
			for(int col = 0; col < tiles[row].length; col++){
                                //draw a tile image in the back. then if the placed id != 0, draw the tile on top 
                                g.drawImage(Tile.getByID(tileBG).getImage(), (double)col*Tile.TILESIZE, (double)row*Tile.TILESIZE);
                                if(tiles[row][col]!=0){
                                    g.drawImage(Tile.getByID(tiles[row][col]).getImage(), (double)col*Tile.TILESIZE, (double)row*Tile.TILESIZE);
                                }
			}
		}
	}
	private void updateTiles(){
		updateTiles(canvas.getGraphicsContext2D());
	}
        //setters
	public void setSelectedTileID(int newID){
		tileID = newID;
	}
	public void setTiles(int[][] ids){
            tiles = ids;
        }
        public void setTileAt(int row, int col, int id){
            tiles[row][col] = id;
        }
        public void setBGTile(int id){
            tileBG = id;
        }
	//getters
	public Canvas getCanvas(){
		updateTiles();
		return canvas;
	}
        public int getSizeX(){
            return cols;
        }
        public int getSizeY(){
            return rows;
        }
        //Palette IO
        protected void save(){
            try{
                BufferedWriter bw = new BufferedWriter(new FileWriter(String.format("%s%s", PALETTEPATH, loadedPalette), false));
                bw.write("meta:{\n");
                bw.write(String.format("\tx:%s%n",cols));
                bw.write(String.format("\ty:%s%n",rows));
                bw.write(String.format("\tb:%s%n",tileBG));
                bw.write("}\n");
                bw.write("tiles:{\n");
                for(int row=0;row<rows;row++){
                    String line = String.format("\trow%s:",row);
                    for(int col=0;col<cols;col++){
                        line += tiles[row][col]+",";
                    }
                    line+="\n";
                    bw.write(line);
                }
                bw.write("}\n");
                
                bw.close();
            }catch(Exception e){
                System.out.println("Failed to save Tile Palette");
            }
        }
        protected static Palette load(){
            BufferedReader br;
            try {
                if(!loadedPalette.endsWith(".pal"))
                    loadedPalette = loadedPalette + ".pal";
                br = new BufferedReader(new FileReader(String.format("%s%s", PALETTEPATH, loadedPalette)));
                String line = br.readLine();
                //instance of palette
                Palette p = new Palette(0,0);
                int x=0,y=0,bg=0;
                int row;
                //declare Enumerators
                final int ENUM_META = 0;
                final int ENUM_TILES = 1;
                int block = ENUM_META;
                while(line!=null){
                    line = line.trim();
                    switch(block){
                        case ENUM_META:
                            if(line.startsWith("x")){
                                line = line.substring(2);
                                x = Integer.parseInt(line);
                            }else if(line.startsWith("y")){
                                line = line.substring(2);
                                y = Integer.parseInt(line);
                            }else if(line.startsWith("b")){
                                line = line.substring(2);
                                bg = Integer.parseInt(line);
                            }else if(line.startsWith("tiles:")){
                                block = ENUM_TILES;
                                p = new Palette(x,y);
                            }
                            break;
                        case ENUM_TILES:
                            //all lines in this section start with 'row<index>:'
                            if(line.startsWith("}"))
                                break;
                            line = line.substring(3);
                            row = Integer.parseInt(line.split(":")[0]);
                            String[] ids = line.split(":")[1].split(",");//line.split(:)[1] returns <id>,<id>, ...
                            for(int i=0;i<ids.length;i++){ //-1 to compensate for the last ,
                                p.setTileAt(row, i, Integer.parseInt(ids[i]));
                            }
                            break;
                    }
                    line = br.readLine();
                }
                p.setBGTile(bg);
                return p;
            }catch(Exception e){
                System.out.println("Error Loading Palette");
                e.printStackTrace();
                return null;
            }
        }
}
