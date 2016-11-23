//Issac "Askumi" O'Hara
//Created:		2016-11-16
//Last Edited:	2016-11-16
package ca.askumi.editor;
import java.util.ArrayList;
import java.util.Optional;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;
import javafx.scene.layout.BorderPane;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.*;

public class MapProperties extends Stage{

	private Map map;
	private TableView<MapModel> table;
	private BorderPane borderpane = new BorderPane();
	private Scene scene = new Scene(borderpane);
	
	@SuppressWarnings("unchecked")
	public MapProperties(Map m){
		map = m;
		table = new TableView<MapModel>();
		table.setEditable(true);
		TableColumn<MapModel, String> property = new TableColumn<MapModel,String>("Property");
		TableColumn<MapModel, String> value = new TableColumn<MapModel,String>("Value");
		ArrayList<MapModel>models = new ArrayList<MapModel>();
		models.add(new MapModel("Name", map.getName()));
		models.add(new MapModel("Map Width", ""+map.getX()));
		models.add(new MapModel("Map Height", ""+map.getY()));
		table.setItems(FXCollections.observableArrayList(models));
		property.setCellValueFactory(new PropertyValueFactory<MapModel, String>("Property"));
		property.setEditable(false);
		property.setResizable(false);
		property.setSortable(false);
		property.setMinWidth(140);
		value.setCellValueFactory(new PropertyValueFactory<MapModel, String>("Value"));
		value.setEditable(true);
		value.setResizable(false);
		value.setSortable(false);
		value.setCellFactory(TextFieldTableCell.forTableColumn());
		value.setMinWidth(140);
		value.setOnEditCommit(e -> {
			((MapModel) e.getTableView().getItems().get(e.getTablePosition().getRow()))
			.setValue(e.getNewValue());
		});
		table.getColumns().addAll(property, value);
		setTitle(String.format("Properties (%s)", map.getName()));
		borderpane.setCenter(table);
		setWidth(300);
		setResizable(false);
		setScene(scene);
		setOnCloseRequest(e -> saveProperties());
	}
	
	//Update map properties when filled out the map properties window
	public void saveProperties() {
		String newname = null;
		int newX = 0;
		int newY = 0;
		ObservableList<TableColumn<MapModel, ?>> cols = table.getColumns();
		for(TableColumn<MapModel, ?> tc : cols){
			if(tc.getText() == "Value") {
				newname = (String) tc.getCellData(0);
				newX = Integer.parseInt((String) tc.getCellData(1));
				newY = Integer.parseInt((String) tc.getCellData(2));
			}
		}
		if(newX < 1 || newY < 1 || newname == null){
			Alert alert = new Alert(Alert.AlertType.ERROR, "Could not save map properties", ButtonType.OK);
			alert.show();
			return;
		}
		if(map.getX() > newX || map.getY() > newY){
			String message = "You are shrinking the map, some tiles outside of the new map will be permanently lost. Do you wish to continue?";
			ButtonType yes = ButtonType.YES;
			ButtonType no = ButtonType.NO;
			Alert alert = new Alert(Alert.AlertType.WARNING, message, yes, no);
			alert.setTitle("Tiles will be lost");
			Optional<ButtonType> result = alert.showAndWait();
			if(result.get() == no){
				return;
			}
		}
		//Change the size of the map while keeping the tiles we have and adding id 0 for new ones
		map.setName(newname);
		map.setX(newX);
		map.setY(newY);
		for(int l = 0; l < map.getLayerCount(); l++){
			int[][] newlayer = new int[newY][newX];
			int[][] thislayer = map.getLayer(l);
			for(int row = 0; row < newY; row++){
				for(int col = 0; col < newX; col++){
					try{
						newlayer[row][col] = thislayer[row][col];
					}catch(IndexOutOfBoundsException e){
						newlayer[row][col] = 0;
					}
				}
			}
			map.setLayer(l, newlayer);
		}
		Editor.closeMapProperties(map);
	}
	
	//table model
	public static class MapModel {
	    private final SimpleStringProperty property;
	    private final SimpleStringProperty value;
	 
	    private MapModel(String p, String v) {
	    	property = new SimpleStringProperty(p);
	    	value = new SimpleStringProperty(v);
	    }
	 
	    public String getProperty() {
	        return property.get();
	    }
	    public String getValue() {
	        return value.get();
	    }
	    public void setProperty(String newProperty) {
	    	property.set(newProperty);
	    } 
	    public void setValue(String newValue) {
	    	value.set(newValue);
	    }
	}
}
