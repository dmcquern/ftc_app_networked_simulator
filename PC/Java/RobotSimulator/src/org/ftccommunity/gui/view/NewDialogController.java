package org.ftccommunity.gui.view;

import org.ftccommunity.simulator.modules.BrickSimulator;
import org.ftccommunity.simulator.modules.LegacyBrickSimulator;
import org.ftccommunity.simulator.modules.MotorBrickSimulator;
import org.ftccommunity.simulator.modules.ServoBrickSimulator;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Dialog to edit details of a Motor Controller.
 *
 * @author Hagerty High
 */
public class NewDialogController {

    @FXML
    private ChoiceBox<String> brickChoiceBox;


    private Stage dialogStage;
    private BrickSimulator brickHolder[];
    private boolean okClicked = false;

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {

    }

    /**
     * Sets the stage of this dialog.
     *
     * @param dialogStage
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;

        // Set the dialog icon.
        this.dialogStage.getIcons().add(new Image("file:resources/images/edit.png"));
    }

    @FXML
    public void initChoiceBox() {
    	brickChoiceBox.setItems(FXCollections.observableArrayList("Core Legacy Module","Core Motor Controller","Core Servo Controller"));
    	brickChoiceBox.getSelectionModel().selectFirst();
    }

    /**
     * Sets the brick to be edited in the dialog.
     *
     * @param brick
     */
    public void setBrick(BrickSimulator[] brickHolder) {
        this.brickHolder = brickHolder;
    }

    /**
     * Returns true if the user clicked OK, false otherwise.
     *
     * @return
     */
    public boolean isOkClicked() {
        return okClicked;
    }

    /**
     * Called when the user clicks ok.
     */
    @FXML
    private void handleOk() {
        if (isInputValid()) {

        	switch (brickChoiceBox.getValue()) {
        	case "Core Legacy Module":
        		brickHolder[0] = new LegacyBrickSimulator();
        		break;
        	case "Core Motor Controller":
        		brickHolder[0] = new MotorBrickSimulator();
        		break;
        	case "Core Servo Controller":
        		brickHolder[0] = new ServoBrickSimulator();
        		break;
        	}

            okClicked = true;
            dialogStage.close();
        }
    }

    /**
     * Called when the user clicks cancel.
     */
    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    /**
     * Validates the user input in the text fields.
     *
     * @return true if the input is valid
     */
    private boolean isInputValid() {
        String errorMessage = "";

        if (errorMessage.length() == 0) {
            return true;
        } else {
            // Show the error message.
            Alert alert = new Alert(AlertType.ERROR);
            alert.initOwner(dialogStage);
            alert.setTitle("Invalid Fields");
            alert.setHeaderText("Please correct invalid fields");
            alert.setContentText(errorMessage);

            alert.showAndWait();

            return false;
        }
    }
}