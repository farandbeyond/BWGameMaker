//Issac "Askumi" O'Hara
//Created:		2016-11-03
//Last Edited:	2016-11-23
package ca.askumi.editor;

import java.io.File;
import java.util.ArrayList;
import java.util.Optional;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.stage.*;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.scene.input.*;
import javafx.scene.paint.Color;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.control.*;
import javafx.scene.canvas.*;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.shape.Rectangle;

public class Editor extends Application{

	//JAVAFX
	private static Stage mainWindow, tileEditorWindow, mapPropertiesWindow;
	private static Scene mainScene;
	private static BorderPane mainlayout;
	//Main Window
	//Main Window - Map Canvas
	private static Canvas canvas;
	private static GraphicsContext graphics;
	//Main Window - Side Bar
	private static VBox sidebar = new VBox();
	private static Menu sidemenu = new Menu("None Selected");
	private static Menu layermenu = new Menu("Background");
	private static ScrollBar scrollbar = new ScrollBar();
	private static Palette palette;
	//TODO FIX SCROLLING THE CANVAS
	//TODO the sidebar is updating WAY TOO MUCH
	private static TextField filter = new TextField();
	//Selected ID Trackers
	private static final int MODE_NONE = 0;
	private static final int MODE_TILE = 1;
	private static final int MODE_TRAVERSE = 2;
	private static int selectedMode = MODE_NONE;
	private static int selectedID = -1; //current location on the panel, when you swap to a new panel, it moves to that panels last location
	private static int selectedTileID = -1; //last location on the tile panel
	private static int selectedTraverseID = -1; //last location on the traverse panel
	//Tracking Booleans
	private static boolean bGrid = true;
	private static boolean tileEditorOpen = false;
	private static boolean mapPropertiesOpen = false;
	private static boolean mapChanged = false;
	//Current Loaded Map
	private static Map map;
	private static int currentLayer; //Current layer we are editing

	//Main
	public static void main(String[] args){
		try {
			Tile.setup();
			//palette = new Palette(5,5);
                        //try to load the default palette first. then create a blank one if error occurred
                        palette = Palette.load();
                        if(palette == null)
                            palette = new Palette(9,5);
			Traverse.setup();
			launch(args);
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	@Override
	//JAVAFX Main
	public void start(Stage mainStage) throws Exception {
		mainWindow = mainStage;
		//Setup Window
		mainWindow.setTitle("Game Editor");
		mainWindow.setOnCloseRequest(e -> close());
		mainWindow.setResizable(false);
		mainWindow.setWidth(Screen.getPrimary().getBounds().getWidth()-100);
		mainWindow.setHeight(Screen.getPrimary().getBounds().getHeight()-100);
		//Setup Scene
		mainlayout = new BorderPane();
		filter.setPromptText("Search");
		filter.setOnKeyTyped(e -> updateFilter());
		//Set Default Scene
		setupMenus();
		mainScene = new Scene(mainlayout);
		mainWindow.setScene(mainScene);
		mainWindow.show();
		//Setup Map
		map = null;
	}

	//Helper functions - Menu Setup
	private void setupMenus(){
		//TOP MENU
		//Menu for file,map,tools,etc
		VBox top = new VBox(); //Container
		MenuBar menubar = new MenuBar(); //Top bar
		//File Menu
		Menu fileMenu = new Menu("File");
		MenuItem file_exit = new MenuItem("Exit");
		file_exit.setOnAction(action -> close());
		fileMenu.getItems().addAll(file_exit);
		//Map Menu
		Menu mapMenu = new Menu("Map");
		MenuItem map_new = new MenuItem("New Map");
		map_new.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN));
		MenuItem map_load = new MenuItem("Load Map");
		map_load.setAccelerator(new KeyCodeCombination(KeyCode.L, KeyCombination.CONTROL_DOWN));
		MenuItem map_save = new MenuItem("Save Map");
		map_save.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
		map_new.setOnAction(action -> newMap());
		map_load.setOnAction(action -> loadMap());
		map_save.setOnAction(action -> saveMap());
		mapMenu.getItems().addAll(map_new, map_load, map_save);
		//Tools Menu
		Menu toolsMenu = new Menu("Edit");
		MenuItem tools_tile = new MenuItem("Open Tile Editor");
		tools_tile.setAccelerator(new KeyCodeCombination(KeyCode.T, KeyCombination.CONTROL_DOWN));
		MenuItem tools_mapprop = new MenuItem("Map Properties");
		tools_mapprop.setAccelerator(new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_DOWN));
		tools_tile.setOnAction(action -> openTileEditor());
		tools_mapprop.setOnAction(action -> openMapProperties(map));
		toolsMenu.getItems().addAll(tools_tile, tools_mapprop);
		//Options Menu
		Menu optionsMenu = new Menu("Options");
		MenuItem options_grid = new MenuItem("Toggle Grid");
		options_grid.setAccelerator(new KeyCodeCombination(KeyCode.G, KeyCombination.CONTROL_DOWN));
		options_grid.setOnAction(action -> toggleGrid());
		optionsMenu.getItems().addAll(options_grid);
		//Add menus together
		top.getChildren().add(menubar); //Top bar inside container
		menubar.getMenus().addAll(fileMenu, mapMenu, toolsMenu, optionsMenu);
		mainlayout.setTop(top); //Set top as top
		
		//SIDEBAR
		//Sidebar for selecting tools on the right side
		BorderPane sidebarlayout = new BorderPane();
		//Sidebar-Menu
		top = new VBox();
		menubar = new MenuBar();
		//TODO sprite edit mode
		//Tile Selector
		MenuItem sidemenuTiles = new MenuItem("Tile Selector");
		sidemenuTiles.setAccelerator(new KeyCodeCombination(KeyCode.T, KeyCombination.ALT_DOWN));
		sidemenuTiles.setOnAction(action -> {filter.setText("");sideMenu_SelectTiles();});
		//Traverse Selector
		MenuItem sidemenuTraverse = new MenuItem("Traversability");
		sidemenuTraverse.setAccelerator(new KeyCodeCombination(KeyCode.R, KeyCombination.ALT_DOWN));
		sidemenuTraverse.setOnAction(action -> {filter.setText("");sideMenu_SelectTraverse();});
		//Add menus together
		sidemenu.getItems().addAll(sidemenuTiles, sidemenuTraverse);
		menubar.getMenus().addAll(sidemenu, layermenu);
		top.getChildren().add(menubar);
		sidebarlayout.setTop(top);
		sidebarlayout.setBackground(new Background(new BackgroundFill(Color.GRAY, null,null)));
		sidebarlayout.setPrefWidth(mainWindow.getWidth()/5);
		//SCROLLBAR
		scrollbar = new ScrollBar();
		scrollbar.valueProperty().addListener(action -> sideMenu_SelectTiles());
		scrollbar.setMin(1);
		scrollbar.setMax(1);
		scrollbar.setValue(1);
		mainlayout.setOnScroll(action -> {
			if(action.getDeltaY() < 0 && scrollbar.getValue() < scrollbar.getMax())
				scrollbar.setValue(scrollbar.getValue()+1);
			else if(action.getDeltaY() > 0 && scrollbar.getValue() > scrollbar.getMin())
				scrollbar.setValue(scrollbar.getValue()-1);
		});
		scrollbar.setOrientation(Orientation.VERTICAL);
		mainlayout.setRight(sidebarlayout);
		//Main Tool Window
		sidebarlayout.setCenter(sidebar);
		VBox bottom = new VBox();
		bottom.getChildren().addAll(palette.getCanvas(), filter);
		sidebarlayout.setBottom(bottom);
		sidebarlayout.setRight(scrollbar);
	}

	//Sidemenu
	//Selection Functions
	private static void sideMenu_SelectTiles() {
                
		sidebar.getChildren().clear();
		selectedID = selectedTileID;
		sidemenu.setText("Tile Selector");
		String fil = filter.getText();
		ArrayList<Tile> tilesFollowingFilter = new ArrayList<Tile>();
		//get only the tiles that following the current filter rules
		for(Tile t : Tile.getAllTiles()){
			if(t.getName().toLowerCase().contains(fil.toLowerCase()) || t.getDescription().toLowerCase().contains(fil.toLowerCase()) || fil == "")
				tilesFollowingFilter.add(t);
		}
		//change the scrollbar to the correct size depending on the filter
		scrollbar.setMax(tilesFollowingFilter.size() - sidebar.getHeight()/36); //TODO hard coded
		//add the rows one at a time until we hit the cap we can fit in
		for(int rows = 0; rows < sidebar.getHeight()/36; rows++){ //TODO hard coded
			//TODO DEBUG: green.png can appear twice at the top
			Tile t;
			try{
				t = tilesFollowingFilter.get((int) (rows+scrollbar.getValue()-1));
			}catch(IndexOutOfBoundsException e){
				//No tiles remaining
				return;
			}
			HBox row = null;
			row = new HBox();
			row.getChildren().addAll(new ImageView(t.getImage()), new VBox(new Label(t.getName()), new Label(t.getDescription())));
			if(selectedID == t.getID())
				row.setBackground(new Background(new BackgroundFill(Color.LIGHTPINK, null,null)));
			else if(rows%2==0)
				row.setBackground(new Background(new BackgroundFill(Color.LIGHTCYAN, null,null)));
			else
				row.setBackground(new Background(new BackgroundFill(Color.LIGHTBLUE, null,null)));
			row.setOnMouseClicked(e -> {
				selectedID = t.getID();
				selectedTileID = t.getID();
				sideMenu_SelectTiles();
				palette.setSelectedTileID(t.getID());
			});
			sidebar.getChildren().add(row);
			selectedMode = MODE_TILE;
		}
		//updateMap();
	}
	//Filtering
	private static void updateFilter() {
		scrollbar.setValue(scrollbar.getMin());
		switch(selectedMode){
			case MODE_TILE: sideMenu_SelectTiles();break;
			case MODE_TRAVERSE: sideMenu_SelectTraverse();break;
		}
	}
	private static void sideMenu_SelectTraverse() {
		sidebar.getChildren().clear();
		selectedID = selectedTraverseID;
		sidemenu.setText("Traversability");
		String fil = filter.getText();
		ArrayList<Traverse> traveseFollowingFilter = new ArrayList<Traverse>();
		//get only the tiles that following the current filter rules
		for(Traverse t : Traverse.getAllTraverses()){
			if(t.getName().toLowerCase().contains(fil.toLowerCase()) || t.getDescription().toLowerCase().contains(fil.toLowerCase()) || fil == "")
				traveseFollowingFilter.add(t);
		}
		//change the scrollbar to the correct size depending on the filter
		scrollbar.setMax(traveseFollowingFilter.size() - sidebar.getHeight()/36); //TODO hard coded 36
		//add the rows one at a time until we hit the cap we can fit in
		for(int rows = 0; rows < sidebar.getHeight()/36; rows++){ //TODO hard coded 36
			Traverse t;
			try{
				t = traveseFollowingFilter.get((int) (rows+scrollbar.getValue()-1));
			}catch(IndexOutOfBoundsException e){
				//No traverse's remaining
				return;
			}
			HBox row = null;
			row = new HBox();
			Pane p = new Pane();
			Rectangle r = new Rectangle(Tile.TILESIZE, Tile.TILESIZE, t.getColor());
			p.getChildren().add(r);
			row.getChildren().addAll(p, new VBox(new Label(t.getName()), new Label(t.getDescription())));
			if(selectedID == t.getID())
				row.setBackground(new Background(new BackgroundFill(Color.LIGHTPINK, null,null)));
			else if(rows%2==0)
				row.setBackground(new Background(new BackgroundFill(Color.LIGHTCYAN, null,null)));
			else
				row.setBackground(new Background(new BackgroundFill(Color.LIGHTBLUE, null,null)));
			row.setOnMouseClicked(e -> {
				selectedID = t.getID();
				selectedTraverseID = t.getID();
				sideMenu_SelectTraverse();
			});
			sidebar.getChildren().add(row);
			selectedMode = MODE_TRAVERSE;
			updateMap();
		}
	}
	//Selection from canvas
	public static void setSelectedID(int tileID) {
		selectedID = tileID;
		selectedTileID = tileID;
	}
	
	//Map Functions
	//Map Menu
	private static void newMap(){
		if(mapChanged)
			if(!askSave())
				return;
		//Dialog setup
		Dialog<?> dialog = new Dialog<>();
		dialog.setTitle("Create New Map");
		dialog.setHeaderText("Please enter map properties");
		ButtonType ok = ButtonType.OK;
		ButtonType cancel = ButtonType.CANCEL;
		dialog.getDialogPane().getButtonTypes().addAll(ok, cancel);
		//Grid setup
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 150, 10, 10));
		//Buttons and labels
		TextField mapname = new TextField();
		mapname.setPromptText("Untitled");
		TextField width = new TextField();
		width.setPromptText("Map Width");
		TextField height = new TextField();
		height.setPromptText("Map Height");
		grid.add(new Label("Map Name:"), 0, 0);
		grid.add(mapname, 1, 0);
		grid.add(new Label("Map Width:"), 0, 1);
		grid.add(width, 1, 1);
		grid.add(new Label("Map Height:"), 0, 2);
		grid.add(height, 1, 2);
		dialog.getDialogPane().setContent(grid);
		dialog.setResultConverter(dialogButton -> {
		    if (dialogButton == ok) {
		    	try{
		    		map = new Map(mapname.getText(), Integer.parseInt(width.getText()), Integer.parseInt(height.getText()));
					setupMap();
		    	}catch(NumberFormatException e){
		    		return null;
		    	}
		        return null;
		    }
		    return null;
		});
		dialog.showAndWait();
	}
	private static void loadMap(){
		if(mapChanged)
			if(!askSave())
				return;
		FileChooser filechooser = new FileChooser();
		filechooser.setTitle("Open Map File");
		filechooser.getExtensionFilters().add(new ExtensionFilter("Map Files", "*.map"));
		filechooser.setInitialDirectory(new File(System.getProperty("user.dir")+"/res/maps"));;
		File selectedFile = filechooser.showOpenDialog(mainWindow);
		if(selectedFile != null){
			map = Map.load(selectedFile.getName());
			mainWindow.setTitle("Game Editor : "+map.getName());
			setupMap();
		}
	}
	private static void saveMap(){
            map.save();
            palette.save();
	    mapChanged = false;
	    if(mainWindow.getTitle().endsWith("*"))
	    	mainWindow.setTitle(mainWindow.getTitle().substring(0, mainWindow.getTitle().length() - 1));
	}
	private static boolean askSave(){
		//returns false is cancel is pressed
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle("Unsaved Changes to "+ map.getName());
		alert.setHeaderText("You have unsaved changes to your map");
		alert.setContentText("Would you like to save changes?");
		ButtonType yes = new ButtonType("Yes");
		ButtonType no = new ButtonType("No");
		ButtonType cancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
		alert.getButtonTypes().setAll(yes, no, cancel);
		Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == yes){
			saveMap();
		    return true;
		} else if (result.get() == no) {
		    return true;
		} else {
		    return false;
		}
	}
	//Load a map into the canvas
	private static void setupMap() {
		//Map Canvas in the middle
		canvas = new Canvas(map.getWidth(),map.getHeight());
		canvas.setOnMousePressed(e -> clickevent(e.getX(), e.getY(), e.getButton(), false));
		canvas.setOnMouseDragged(e -> clickevent(e.getX(), e.getY(), e.getButton(), true));
		canvas.setOnMouseReleased(e -> releaseevent(e.getButton()));
		canvas.setOnMouseDragReleased(e -> releaseevent(e.getButton()));
		graphics = canvas.getGraphicsContext2D();
		mainlayout.setCenter(canvas);
		updateMap();
		updateLayerMenu();
	}
	private static void updateLayerMenu(){
		layermenu.getItems().clear();
		for(int layer = 0; layer < map.getLayerCount(); layer++){
			int thisLayer = layer;
			MenuItem thisItem = new MenuItem(map.getLayerName(thisLayer));
			thisItem.setOnAction(action -> selectLayer(thisLayer));
			if(layer < 10)
				thisItem.setAccelerator(new KeyCodeCombination(getKeyNumber(thisLayer), KeyCombination.ALT_DOWN));
			layermenu.getItems().add(thisItem);
			if(layer != 0){
				MenuItem thisDeleteItem = new MenuItem("Delete "+map.getLayerName(thisLayer));
				thisDeleteItem.setOnAction(action -> {
					map.deleteLayer(thisLayer);
					if(currentLayer == thisLayer){
						selectLayer(0);
					}
					updateMap();
					updateLayerMenu();
				});
				layermenu.getItems().add(thisDeleteItem);
			}
		}
		MenuItem newLayer = new MenuItem("Add Layer");
		newLayer.setOnAction(action -> createLayer());
		layermenu.getItems().add(newLayer);
	}
	private static void selectLayer(int layernum){
		currentLayer = layernum;
		layermenu.setText(map.getLayerName(layernum));
	}
	private static KeyCode getKeyNumber(int number){
		switch(number){
		case 0: return KeyCode.DIGIT0;
		case 1: return KeyCode.DIGIT1;
		case 2: return KeyCode.DIGIT2;
		case 3: return KeyCode.DIGIT3;
		case 4: return KeyCode.DIGIT4;
		case 5: return KeyCode.DIGIT5;
		case 6: return KeyCode.DIGIT6;
		case 7: return KeyCode.DIGIT7;
		case 8: return KeyCode.DIGIT8;
		}
		return KeyCode.DIGIT9;
	}
	//Called when you want to add a new layer
	private static void createLayer(){
		TextInputDialog dialog = new TextInputDialog("");
		dialog.setTitle("Create New Layer");
		dialog.setHeaderText("Enter a layer name");
		Optional<String> result = dialog.showAndWait();
		result.ifPresent(name -> {
			if(name.equals(""))
				name = "Layer "+map.getLayerCount();
			map.addLayer(name);
			updateLayerMenu();
		});
		selectLayer(map.getLayerCount()-1);
	}
	//Update the canvas to show the current map
	private static void updateMap(){
                if(map == null)
                    return;
		graphics.setFill(Color.BLACK);
		graphics.fillRect(0, 0, map.getWidth(),map.getHeight());
		for(int row = 0; row < map.getY(); row++){
			for(int col = 0; col < map.getX(); col++){
				for(int i = 0;  i < map.getLayerCount(); i++){
					if(map.getID(i,row,col) != 0) //Do not draw ID 0
						graphics.drawImage(Tile.getByID(map.getID(i,row,col)).getImage(), col*Tile.TILESIZE, row*Tile.TILESIZE);
				}
				//if traverse mode isenabled
				if(selectedMode == MODE_TRAVERSE){
					Color c = Traverse.getTraverse(map.getTraverse(row,col)).getColor();
					graphics.setFill(new Color(c.getRed(), c.getGreen(), c.getBlue(), 0.4));
					graphics.fillRect(col*Tile.TILESIZE, row*Tile.TILESIZE,Tile.TILESIZE,Tile.TILESIZE);
				}
			}
		}
		//If grid is enabled
		if(bGrid){
			graphics.setStroke(Color.RED);
			for(int x = 0; x <= map.getX(); x++){
				graphics.strokeLine(x*Tile.TILESIZE, 0, x*Tile.TILESIZE, map.getHeight());
			}
			for(int y = 0; y <= map.getY(); y++){
				graphics.strokeLine(0,y*Tile.TILESIZE, map.getWidth(), y*Tile.TILESIZE);
			}
		}
	}
	
	//Handles when you click on the canvas
	private static boolean masspressed = false;
	private static void clickevent(double x, double y, MouseButton button, boolean dragged){
		int gridX = (int) (x / Tile.TILESIZE);
		int gridY = (int) (y / Tile.TILESIZE);
		switch(selectedMode){
			case MODE_TILE:{
				if(button == MouseButton.SECONDARY)
					if(masspressed)
						map.MassSetTile(currentLayer, gridY, gridX, 0);
					else
						map.setTile(currentLayer, gridY, gridX, 0);
				else if(button == MouseButton.MIDDLE & !dragged){
					masspressed = true;
				}
				else if(button == MouseButton.PRIMARY){
					if(masspressed)
						map.MassSetTile(currentLayer, gridY, gridX, selectedID);
					else
						map.setTile(currentLayer, gridY, gridX, selectedID);
				}
				updateMap();
				if(!mapChanged){
					mainWindow.setTitle(mainWindow.getTitle()+"*");
					mapChanged = true;
				}
			}break;
			case MODE_TRAVERSE:{
				if(button == MouseButton.SECONDARY)
					if(masspressed)
						map.MassSetTraverse(gridY, gridX, 0);
					else
						map.setTraverse(gridY, gridX, 0);
				else if(button == MouseButton.MIDDLE & !dragged){
					masspressed = true; //this is getting called by pressing mouse1
				}
				else if(button == MouseButton.PRIMARY){
					if(masspressed)
						map.MassSetTraverse(gridY, gridX, selectedID);
					else
						map.setTraverse(gridY, gridX, selectedID);
				}
				updateMap();
				if(!mapChanged){
					mainWindow.setTitle(mainWindow.getTitle()+"*");
					mapChanged = true;
				}
			}
		}
	}
	private static void releaseevent(MouseButton button){
		if(button == MouseButton.MIDDLE)
			masspressed = false;
	}
	//Tool Menu Functions
	//TileEditor
	private static void openTileEditor(){
		if(!tileEditorOpen){
			tileEditorOpen = true;
			tileEditorWindow = new TileEditor();
		}
		tileEditorWindow.requestFocus();
	}
	protected static void closeTileEditor(){
		tileEditorOpen = false;
		tileEditorWindow.close();
		filter.setText("");
		sideMenu_SelectTiles();
		scrollbar.setMax(Tile.getAllTiles().size());
	}
	private void openMapProperties(Map currentmap){
		if(mapPropertiesOpen || map == null)
			return;
		mapPropertiesOpen = true;
		mapPropertiesWindow = new MapProperties(currentmap);
		mapPropertiesWindow.initModality(Modality.APPLICATION_MODAL);
		mapPropertiesWindow.initOwner(mainWindow);
		mapPropertiesWindow.show();
	}
	protected static void closeMapProperties(Map newmap){
		map = newmap;
		mapPropertiesOpen = false;
		mapPropertiesWindow.close();
		setupMap();
	}

	//Options Menu Functions
	private static void toggleGrid() {
		bGrid = !bGrid;
		updateMap();
	}
	
	public static Stage getTileEditor(){
		return tileEditorWindow;
	}
	//Called to close the program
	private static void close(){
                palette.save();
		if(mapChanged)
			if(!askSave())
				return;
		System.exit(0);
	}
}
