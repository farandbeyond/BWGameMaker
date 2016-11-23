//Issac "Askumi" O'Hara
//Created:		2016-11-03
//Last Edited:	2016-11-11
package ca.askumi.editor;
import java.io.File;
import java.util.ArrayList;
import java.util.Optional;

import javax.swing.JOptionPane;

import ca.askumi.editor.Tile.TileException;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.scene.image.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.*;

public class TileEditor extends Stage{

	public static final int WIDTH = 800;
	public static final int HEIGHT = 500;

	private BorderPane layout;
	private Scene scene;
	private Menu functionsMenu;
	private Menu tileMenu;
	private VBox menuBar;
	@SuppressWarnings("rawtypes")
	private TableView table;
	private TextField filter;

	private Tile openTile = null;
	
	//TODO file chooser
	//TODO massimport
	//TODO stop table from updating on delete
	//TODO multi-edit
	//TODO keycombinations
	//TODO fix tags
	//TODO auto refesh on large import
	//TODO dont let you delete ID 0 (The green tile)
	public TileEditor(){
		super();
		//Setup Window
		setOnCloseRequest(action -> Editor.closeTileEditor());
		setWidth(WIDTH);
		setHeight(HEIGHT);
		setTitle("Tile Editor");
		//Setup Scenes
		setupMenuBar();
		filter = new TextField();
		filter.promptTextProperty().set("Search Tiles");
		filter.setOnKeyTyped(e -> showTiles());
		setupScene();
		functionsMenu.setDisable(false);
		tileMenu.setDisable(true);
		show();
	}
	
	private void setupMenuBar(){
		layout = new BorderPane();
		menuBar = new VBox();
		MenuBar menubar = new MenuBar(); //Top bar
		menuBar.getChildren().add(menubar); //Top bar inside container
		//Functions Menu
		functionsMenu = new Menu("Functions");
		MenuItem func_add = new MenuItem("Add New Tile");
		MenuItem func_reload = new MenuItem("Reload Tile List");
		MenuItem func_close = new MenuItem("Close Tile Editor");
		MenuItem func_bigimage = new MenuItem("Import large image");
		func_bigimage.setOnAction(action -> addBigImage());
		func_add.setOnAction(action -> openTileCreator());
		func_reload.setOnAction(action -> {
			ArrayList<Tile> oldList = Tile.getAllTiles();
			//If we fail to reload tiles, keep with our old list
			try {
				Tile.setup();
				showTiles();
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, "An error has occured while reload tiles: "+e.getMessage());
				Tile.setAllTiles(oldList);
			}
		});
		
		func_close.setOnAction(action -> Editor.closeTileEditor());
		functionsMenu.getItems().addAll(func_add, func_reload, func_bigimage, func_close);
		//Tile Menu
		tileMenu = new Menu("Tile");
		MenuItem tile_save = new MenuItem("Save Tile and Close");
		MenuItem tile_close = new MenuItem("Close Without Saving");
		tile_save.setOnAction(action -> saveTile());
		tile_close.setOnAction(action -> closeEditScene());
		tileMenu.getItems().addAll(tile_save, tile_close);
		//Add menus
		menubar.getMenus().addAll(functionsMenu, tileMenu);
		layout.setTop(menuBar);
	}
	
	//Initial setup of the scene
	private void setupScene(){
		scene = new Scene(layout);
		showTiles();
		setScene(scene);
	}
	
	//Change the window to show new/edit tile mode
	@SuppressWarnings("unchecked")
	private void enterEditScene(){
		Label idLabel = new Label("Editing TileID : "+ openTile.getID());
		table = new TableView<EditTileModel>();
		table.setEditable(true);
		TableColumn<EditTileModel,String> CProperty = new TableColumn<EditTileModel,String>("Property");
		TableColumn<EditTileModel,String> CValue = new TableColumn<EditTileModel,String>("Value");
		//Setup data
		ArrayList<EditTileModel> models = new ArrayList<EditTileModel>();
		models.add(new EditTileModel("Name", openTile.getName()));
		models.add(new EditTileModel("Description", openTile.getDescription()));
		models.add(new EditTileModel("Image File", openTile.getImageFile()));
		models.add(new EditTileModel("Offset X", ""+openTile.getOffsetX()));
		models.add(new EditTileModel("Offset Y", ""+openTile.getOffsetY()));
		//Display Data
		table.setItems(FXCollections.observableArrayList(models));
		CProperty.setCellValueFactory(new PropertyValueFactory<EditTileModel, String>("Property"));
		CProperty.setResizable(false);
		CProperty.setEditable(false);
		CProperty.setSortable(false);
		CProperty.setMinWidth(100);
		CValue.setCellValueFactory(new PropertyValueFactory<EditTileModel, String>("Value"));
		CValue.setResizable(false);
		CValue.setEditable(true);
		CValue.setSortable(false);
		CValue.setMinWidth(WIDTH-120);
		CValue.setCellFactory(TextFieldTableCell.forTableColumn());
		CValue.setOnEditCommit(e -> {
			((EditTileModel) e.getTableView().getItems().get(e.getTablePosition().getRow()))
			.setValue(e.getNewValue());
		});
		//Add table to scene
		table.getColumns().addAll(CProperty, CValue);
		layout.setBottom(idLabel);
		layout.setCenter(table);
	}
	
	//Change the window to show a table of all tiles we have
	@SuppressWarnings("unchecked")
	private void showTiles(){
		//Setup Table
		table = new TableView<TileDataModel>();
		table.setEditable(false);
		TableColumn<TileDataModel,String> CID = new TableColumn<TileDataModel,String>("ID");
		TableColumn<TileDataModel,ImageView> CImage = new TableColumn<TileDataModel,ImageView>("Image");
		TableColumn<TileDataModel,String> CName = new TableColumn<TileDataModel,String>("Name");
		TableColumn<TileDataModel,String> CDescription = new TableColumn<TileDataModel,String>("Description");
		TableColumn<TileDataModel,Button> CEdit = new TableColumn<TileDataModel,Button>("Edit");
		TableColumn<TileDataModel,Button> CDelete = new TableColumn<TileDataModel,Button>("Delete");
		//Setup data
		ArrayList<TileDataModel> models = new ArrayList<TileDataModel>();
		for(Tile t : Tile.getAllTiles()){
			//If filter matches name OR filter matches description OR filter is empty
			if(t.getName().toLowerCase().contains(filter.getText().toLowerCase())
				|| t.getDescription().toLowerCase().contains(filter.getText().toLowerCase())
				|| filter.getText().equals(""))
				models.add(new TileDataModel(t, this));
		}
		table.setItems(FXCollections.observableArrayList(models));
		CID.setCellValueFactory(new PropertyValueFactory<TileDataModel, String>("ID"));
		CID.setResizable(false);
		CImage.setCellValueFactory(new PropertyValueFactory<TileDataModel, ImageView>("image"));
		CImage.setResizable(false);
		CName.setCellValueFactory(new PropertyValueFactory<TileDataModel, String>("name"));
		CName.setResizable(false);
		CName.setMaxWidth(80);
		CName.setMinWidth(80);
		CDescription.setCellValueFactory(new PropertyValueFactory<TileDataModel, String>("description"));
		CDescription.setMinWidth(300);
		CDescription.setResizable(false);
		CEdit.setCellValueFactory(new PropertyValueFactory<TileDataModel, Button>("button"));
		CEdit.setResizable(false);
		CDelete.setCellValueFactory(new PropertyValueFactory<TileDataModel, Button>("delete"));
		CDelete.setResizable(false);
		//Add table to scene
		table.getColumns().addAll(CID,CImage,CName,CDescription,CEdit,CDelete);
		layout.setCenter(table);
		layout.setBottom(filter);
	}
	
	//Tile Editing
	public void openTileCreator() {
		openTile = Tile.CreateNewTile();
		openEditScene(openTile);
	}
	public void openEditScene(Tile t) {
		openTile = t;
		functionsMenu.setDisable(true);
		tileMenu.setDisable(false);
		enterEditScene();
	}
	public void closeEditScene() {
		openTile = null;
		functionsMenu.setDisable(false);
		tileMenu.setDisable(true);
		showTiles();
	}
	
	//Adding multiple images
	private void addBigImage(){
		FileChooser filechooser = new FileChooser();
		filechooser.setTitle("Open Image File");
		filechooser.getExtensionFilters().add(new ExtensionFilter("Image Files", "*.png"));
		filechooser.setInitialDirectory(new File(System.getProperty("user.dir")+"/res/tiles"));;
		File selectedFile = filechooser.showOpenDialog(this);
		Tile newTile = null;
		TextInputDialog dialog = new TextInputDialog("");
		dialog.setTitle("Enter Tags");
		dialog.setHeaderText("Please enter some search tags for your new tiles.");
		Optional<String> result = dialog.showAndWait();
		if(selectedFile != null){
			Image image = new Image("tiles/"+selectedFile.getName());
			if(image.getWidth() % Tile.TILESIZE != 0 || image.getHeight() % Tile.TILESIZE != 0){
				System.out.println("Invalid image size");
				return;
			}
			int loop = 0;
			for(int x = 0; x < image.getWidth(); x += Tile.TILESIZE){
				for(int y = 0; y < image.getHeight(); y += Tile.TILESIZE){
					newTile = Tile.CreateNewTile("tiles/"+selectedFile.getName());
					newTile.setName(String.format("%s-%d",selectedFile.getName(),loop));
					newTile.setOffsetX(x/Tile.TILESIZE);
					newTile.setOffsetY(y/Tile.TILESIZE);
					if(result.isPresent()){
						newTile.setDescription(result.get());
					}
					loop++;
				}
			}
		}
		Tile.save();
	}
	
	@SuppressWarnings("unchecked")
	public void saveTile(){
		ObservableList<TableColumn<EditTileModel,String>> cols = table.getColumns();
		for(TableColumn<EditTileModel,String> tc : cols){
			if(tc.getText() == "Value") {
				try {
					openTile.setName(tc.getCellData(0));
					openTile.setDescription(tc.getCellData(1));
					try{
						openTile.setImage(tc.getCellData(2));
					}catch(Exception e){
						e.printStackTrace();
					}
					openTile.setOffsetX(tc.getCellData(3));
					openTile.setOffsetY(tc.getCellData(4));
					Tile.update(openTile.getID(), openTile);
				} catch (TileException e) {
					e.printStackTrace();
				}
				Tile.save();
			}
		}
		closeEditScene();
	}

	//Function to delete a tile
	public void delete(Tile t) {
		Tile.deleteTile(t.getID());
		showTiles();
	}
	
	//Classes used to display table info
	public static class TileDataModel {
	    private final SimpleStringProperty ID;
	    private final SimpleStringProperty name;
	    private final SimpleStringProperty description;
		private final ImageView image;
	    private final Button button, delete;
	 
	    private TileDataModel(Tile t, TileEditor te) {
	        ID = new SimpleStringProperty(t.getID()+"");
	        name = new SimpleStringProperty(t.getName());
	        description = new SimpleStringProperty(t.getDescription());
	    	image = new ImageView(t.getImage());
	    	button = new Button("Edit");
	    	button.setOnAction(action -> te.openEditScene(t));
	    	delete = new Button("Delete");
	    	delete.setOnAction(action -> te.delete(t));
	    }
	 
	    public String getID() {
	        return ID.get();
	    }
	    public String getName() {
	        return name.get();
	    }
	    public String getDescription() {
	        return description.get();
	    }
	    public ImageView getImage(){
	    	return image;
	    }
	    public Button getButton(){
	    	return button;
	    }
	    public Button getDelete(){
	    	return delete;
	    }
	    public void setID(int newID) {
	    	ID.set(newID+"");
	    } 
	    public void setName(String newName) {
	    	name.set(newName);
	    }
	    public void setDescription(String newDescription) {
	    	description.set(newDescription);
	    }
	}
	
	public static class EditTileModel {
	    private final SimpleStringProperty property;
	    private final SimpleStringProperty value;
	    private final SimpleStringProperty offsetX;
	    private final SimpleStringProperty offsetY;
	 
	    private EditTileModel(String property, String value) {
	    	this.property = new SimpleStringProperty(property);
	    	this.value = new SimpleStringProperty(value);
	    	this.offsetX = new SimpleStringProperty("0");
	    	this.offsetY = new SimpleStringProperty("1");
	    }
	 
	    public String getProperty() {
	        return property.get();
	    }
	    public String getValue() {
	        return value.get();
	    }
	    public String getOffsetX() {
	        return offsetX.get();
	    }
	    public String getOffsetY() {
	        return offsetY.get();
	    }
	    public void setValue(String newValue){
	    	value.set(newValue);
	    }
	    public void setOffsetX(String newValue){
	    	offsetX.set(newValue);
	    }
	    public void setOffsetY(String newValue){
	    	offsetY.set(newValue);
	    }
	}
}
