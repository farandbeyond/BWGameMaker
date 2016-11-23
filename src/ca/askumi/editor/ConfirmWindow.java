//Issac "Askumi" O'Hara
//Created:		2016-11-16
//Last Edited:	2016-11-16
//Pops up a window with a prompt yes/no options
package ca.askumi.editor;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ConfirmWindow extends Stage{

	//layout
	private BorderPane bp = new BorderPane();
	private HBox hbox = new HBox();
	private Scene popup = new Scene(bp);
	private Button yes = new Button("Yes");
	private Button no = new Button("No");
	//prompt
	private Label label;
	private boolean result = false;
	private boolean waiting = false;

	public ConfirmWindow(String prompt, Stage parent){
		yes.setOnAction(e -> yesAction());
		no.setOnAction(e -> noAction());
		setOnCloseRequest(e -> noAction());
		hbox.getChildren().addAll(yes, no);
		label = new Label(prompt);
		label.setBorder(new Border(new BorderStroke(Color.LIGHTGRAY, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(5))));
		label.setFont(new Font(16));
		yes.prefWidthProperty().bind(hbox.widthProperty());
		no.prefWidthProperty().bind(hbox.widthProperty());
		bp.setCenter(label);
		bp.setBottom(hbox);
		initModality(Modality.APPLICATION_MODAL);
		initOwner(parent);
		setTitle("Warning");
		setScene(popup);
		setResizable(false);
		show();
	}
	
	private void yesAction() {
		waiting = false;
		result = true;
		close();
	}
	private void noAction() {
		waiting = false;
		result = false;
		close();
	}
	
	public boolean waitingForAction(){
		return waiting;
	}
	
	public boolean getAction(){
		return result;
	}
}
