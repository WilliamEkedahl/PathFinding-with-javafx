// PROG2 VT24, Inlämningsuppgift, del 2
// Grupp 159
// Max Lindberg mali7984
// William Ekedahl wiek0904
// Simon Lundqvist silu8199

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Optional;
import javafx.stage.WindowEvent;

public class PathFinder extends Application {
    private String imagePath = "file:europa.gif";
    private ImageView imageView = new ImageView(new Image(imagePath));
    private BorderPane root;
    private ListGraph<Location> graph = new ListGraph<Location>();
    private Button newPlaceButton = new Button("New Place");
    private Pane pane;
    private Location selectedLoc1;
    private Location selectedLoc2;
    private String nameConnectionEntered;
    private String timeConnectionEntered;
    private Button showConnectionButton;
    private Button findPathButton;
    private Button changeConnectionButton;
    private Button newConnectionButton;
    private boolean saved = true;

    @Override
    public void start(Stage primaryStage) {

        root = new BorderPane();
        MenuBar menu = new MenuBar();
        menu.setId("menu");
        Menu fileMenu = new Menu("File");
        fileMenu.setId("menuFile");

        FlowPane flowPane = new FlowPane();
        flowPane.setPadding(new Insets(5, 0, 0, 10));
        flowPane.setHgap(10);
        findPathButton = new Button("Find Path");
        showConnectionButton = new Button("Show connection");
        newConnectionButton = new Button("New Connection");
        changeConnectionButton = new Button("Change Connection");
        pane = new Pane();
        pane.setId("outputArea");
        root.setBottom(pane);
        flowPane.getChildren().addAll(findPathButton, showConnectionButton, newPlaceButton, newConnectionButton, changeConnectionButton);
        root.setCenter(flowPane);
        MenuItem newMapItem = new MenuItem("New Map");
        newMapItem.setId("menuNewMap");
        MenuItem saveItem = new MenuItem("Save");
        saveItem.setId("menuSaveFile");
        MenuItem openItem = new MenuItem("Open");
        openItem.setId("menuOpenFile");
        MenuItem saveImageItem = new MenuItem("Save Image");
        saveImageItem.setId("menuSaveImage");
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setId("menuExit");
        fileMenu.getItems().addAll(newMapItem, saveItem, openItem, saveImageItem, exitItem);
        exitItem.setOnAction(new ClickHandlerExit(primaryStage));
        newMapItem.setOnAction(new ClickHandlerNewMap());
        saveItem.setOnAction(new ClickHandlerSave());
        saveImageItem.setOnAction(new SaveImageHandler());
        openItem.setOnAction(new ClickHandlerOpen());
        changeConnectionButton.setDisable(true);
        findPathButton.setDisable(true);
        showConnectionButton.setDisable(true);
        newConnectionButton.setDisable(true);
        newPlaceButton.setDisable(true);
        newPlaceButton.setOnAction(new ClickHandlerNewPlace());
        newConnectionButton.setOnAction(new ClickHandlerNewConnection());
        changeConnectionButton.setOnAction(new ClickHandlerChangeConnection());
        findPathButton.setOnAction(new ClickHandlerFindPath());
        showConnectionButton.setOnAction(new ClickHandlerShowConnection());
        menu.getMenus().add(fileMenu);
        root.setTop(menu);

        newPlaceButton.setId("btnNewPlace");
        findPathButton.setId("btnFindPath");
        showConnectionButton.setId("btnShowConnection");
        changeConnectionButton.setId("btnChangeConnection");
        newConnectionButton.setId("btnNewConnection");


        Scene scene = new Scene(root, 618, 729);
        primaryStage.setScene(scene);
        primaryStage.widthProperty().addListener(
                (obs, oldO, newO) -> imageView.setFitWidth(primaryStage.getWidth())
        );
        primaryStage.heightProperty().addListener(
                (ob, old, nev) -> imageView.setFitHeight(primaryStage.getHeight())
        );
        primaryStage.setTitle("PathFinder");
        primaryStage.show();

    }

    class ClickHandlerNewMap implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            saved = false;
            graph = new ListGraph<>();
            pane.getChildren().clear();
            imageView = new ImageView(new Image("file:europa.gif"));
            pane.getChildren().add(imageView);
            root.setBottom(pane);
            changeConnectionButton.setDisable(false);
            findPathButton.setDisable(false);
            showConnectionButton.setDisable(false);
            newConnectionButton.setDisable(false);
            newPlaceButton.setDisable(false);
            selectedLoc1 = null;
            selectedLoc2 = null;
        }
    }

    class ClickHandlerOpen implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            if (!saved) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Unsaved changes");
                alert.setTitle("WARNING!");
                Optional<ButtonType> res = alert.showAndWait();
                if (res.isPresent() && res.get().equals(ButtonType.CANCEL)) {
                    event.consume();
                    return;
                }
            } else {
                saved = true;
            }

            selectedLoc1 = null;
            selectedLoc2 = null;
            root.setBottom(pane);
            changeConnectionButton.setDisable(false);
            findPathButton.setDisable(false);
            showConnectionButton.setDisable(false);
            newConnectionButton.setDisable(false);
            newPlaceButton.setDisable(false);
            HashMap<String, Location> locByName = new HashMap<>();


            try {
                BufferedReader reader = new BufferedReader(new FileReader("europa.graph"));
                pane.getChildren().clear();

                graph = new ListGraph<>();

                String line;
                line = reader.readLine();
                imagePath = line;

                if (imagePath.trim().isEmpty()){
                    imageView = new ImageView(new Image("file:europa.gif"));
                }
                else {
                    imageView = new ImageView(new Image(imagePath));
                }

                pane.getChildren().add(imageView);
                line = reader.readLine();
                String[] parts = line.split(";");
                for (int i = 0; i < parts.length; i += 3) {
                    String city = parts[i];
                    double xcor = Double.parseDouble(parts[i + 1]);
                    double ycor = Double.parseDouble(parts[i + 2]);
                    Location newLoc = new Location(city, xcor, ycor);
                    Label namePlaceLabel = new Label(city);
                    namePlaceLabel.setDisable(true);
                    namePlaceLabel.relocate(xcor - 5, ycor + 10);

                    newLoc.setId(city);
                    graph.add(newLoc);
                    locByName.put(city, newLoc);
                    pane.getChildren().addAll(newLoc, namePlaceLabel);
                    newLoc.setOnMouseClicked(new MouseHandlerSelectPlace());
                }
                while ((line = reader.readLine()) != null) {
                    String[] conParts = line.split(";");
                    Location fromLoc = locByName.get(conParts[0]);
                    Location toLoc = locByName.get(conParts[1]);
                    String conName = conParts[2];
                    int conWeight = Integer.parseInt(conParts[3]);
                    try{
                        if (graph.getEdgeBetween(fromLoc, toLoc) == null){
                            graph.connect(fromLoc, toLoc, conName, conWeight);
                            Line conLine = new Line(fromLoc.getXcor(), fromLoc.getYcor(), toLoc.getXcor(), toLoc.getYcor());
                            conLine.setDisable(true);
                            pane.getChildren().add(conLine);
                        }

                    }catch (NoSuchElementException e){
                        e.printStackTrace();
                    }
                    catch (NullPointerException npe){
                        npe.getMessage();
                    }
                }
                saved = true;
            } catch (FileNotFoundException fileE) {
                fileE.printStackTrace();
                Alert fileNotFoundAlert = new Alert(Alert.AlertType.ERROR);
                fileNotFoundAlert.setHeaderText("File does not exist");
                saved = true;
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage());
                alert.setTitle("Error!");
                alert.showAndWait();
            }
        }
    }


    class ClickHandlerSave implements EventHandler<ActionEvent> {
        @Override

        public void handle(ActionEvent event) {
            File file = new File("europa.graph");
            try {
                FileWriter wr = new FileWriter("europa.graph");
                wr.write(imagePath + "\n");
                StringBuilder sb1 = new StringBuilder();
                StringBuilder sb2 = new StringBuilder();
                int counter = 0;
                for (Location oneLoc : graph.getNodes()) {
                    double xcor = oneLoc.getXcor();
                    double ycor = oneLoc.getYcor();
                    if (counter < graph.getNodes().size() - 1){
                        sb1.append(oneLoc.getCityName()).append(";").append(xcor).append(";").append(ycor).append(";");
                    }else if (counter == graph.getNodes().size() - 1){
                        sb1.append(oneLoc.getCityName()).append(";").append(xcor).append(";").append(ycor);
                    }
                    counter++;

                    for (Edge<Location> oneEdge : graph.getEdgesFrom(oneLoc)) {
                        String destination = oneEdge.getDestination().getCityName();
                        String edgeName = oneEdge.getName();
                        String edgeWeight = String.valueOf(oneEdge.getWeight());
                        sb2.append(oneLoc.getCityName()).append(";").append(destination).append(";").append(oneEdge.getName()).append(";").append(oneEdge.getWeight()).append("\n");
                    }
                }
                sb1.append("\n");
                wr.write(sb1.toString());
                wr.write(sb2.toString());
                wr.close();
                saved = false;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class SaveImageHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            try {
                WritableImage image = root.snapshot(null, null);
                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
                ImageIO.write(bufferedImage, "png", new File("capture.png"));
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Error message " + e.getMessage());
                alert.showAndWait();
            }
        }
    }
    private void systemExit(Stage stage) {
        stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
    }

    class ClickHandlerExit implements EventHandler<ActionEvent> {
        private Stage stage;

        ClickHandlerExit(Stage stage) {
            this.stage = stage;
        }

        @Override
        public void handle(ActionEvent event) {
            if (!saved) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("WARNING!");
                alert.setHeaderText("Warning!");
                alert.setContentText("Unsaved changes, exit anyway?");
                Optional<ButtonType> res = alert.showAndWait();
                if (res.isPresent() && res.get() == ButtonType.CANCEL) {
                    event.consume();
                } else if (res.isPresent() && res.get() == ButtonType.OK) {
                    systemExit(stage);
                }
            } else {
                systemExit(stage);
            }
        }
    }

    class ClickHandlerNewPlace implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            saved = false;
            newPlaceButton.setDisable(true);
            root.setCursor(Cursor.CROSSHAIR);
            pane.setOnMouseClicked(new MouseHandlerPlaceLocation());
        }
    }

    class MouseHandlerPlaceLocation implements EventHandler<MouseEvent> {

        @Override
        public void handle(MouseEvent event) {
            saved = false;
            Alert newPlaceAlert = new Alert(Alert.AlertType.CONFIRMATION);
            FlowPane content = new FlowPane();
            content.setHgap(10);
            newPlaceAlert.setTitle("Name");
            Label newPlaceLabel = new Label("Name of place:");
            TextField newPlaceTextField = new TextField();
            content.getChildren().addAll(newPlaceLabel, newPlaceTextField);
            newPlaceAlert.getDialogPane().setContent(content);
            Optional<ButtonType> keyPressed = newPlaceAlert.showAndWait();
            if (!newPlaceTextField.getText().trim().isEmpty() && keyPressed.isPresent() && keyPressed.get() == ButtonType.OK) {
                String namePlace = newPlaceTextField.getText().trim();
                double xcor = event.getX();
                double ycor = event.getY();
                Location newLoc = new Location(namePlace, xcor, ycor);
                newLoc.setId(namePlace);
                Label namePlaceLabel = new Label(namePlace);
                namePlaceLabel.relocate(xcor - 5, ycor + 10);
                namePlaceLabel.setDisable(true);

                pane.getChildren().addAll(newLoc, namePlaceLabel);
                graph.add(newLoc);
                newLoc.setOnMouseClicked(new MouseHandlerSelectPlace());
            }
            root.setCursor(Cursor.DEFAULT);
            newPlaceButton.setDisable(false);
            pane.setOnMouseClicked(null);

        }

    }

    class MouseHandlerSelectPlace implements EventHandler<MouseEvent> {
        @Override
        public void handle(MouseEvent event) {
            Location tmpLoc = (Location) event.getSource();
            if (selectedLoc1 == null && tmpLoc != selectedLoc2) {
                selectedLoc1 = tmpLoc;
                selectedLoc1.setFill(Color.RED);
            } else if (selectedLoc2 == null && tmpLoc != selectedLoc1) {
                selectedLoc2 = tmpLoc;
                selectedLoc2.setFill(Color.RED);
            } else if (event.getSource() == selectedLoc1) {
                selectedLoc1.setFill(Color.BLUE);
                selectedLoc1 = null;
            } else if (event.getSource() == selectedLoc2) {
                selectedLoc2.setFill(Color.BLUE);
                selectedLoc2 = null;
            }
        }
    }

    class ClickHandlerNewConnection implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            saved = false;

            if (selectedLoc2 == null || selectedLoc1 == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error!");
                alert.setHeaderText("Two places must be selected!");
                alert.showAndWait();
            }
            else if (graph.getEdgeBetween(selectedLoc1, selectedLoc2) != null){
                Alert alreadyConnectionAlert = new Alert(Alert.AlertType.ERROR);
                alreadyConnectionAlert.setTitle("ERROR");
                alreadyConnectionAlert.setHeaderText("Connection already exists!");
                alreadyConnectionAlert.showAndWait();

            }
            else if (graph.getEdgeBetween(selectedLoc1, selectedLoc2) == null) {
                Alert connAlert = new Alert(Alert.AlertType.CONFIRMATION);
                connAlert.setTitle("Connection");
                connAlert.setHeaderText("Connection from " + selectedLoc1.getCityName() + " to " + selectedLoc2.getCityName());
                Label name = new Label("Name: ");
                Label time = new Label("Time: ");
                TextField nameField = new TextField();
                TextField timeField = new TextField();
                GridPane grid = new GridPane();
                grid.setHgap(10);
                grid.setVgap(10);
                grid.add(name, 0, 0);
                grid.add(time, 1, 0);
                grid.add(nameField, 0, 1);
                grid.add(timeField, 1, 1);
                connAlert.getDialogPane().setContent(grid);
                connAlert.showAndWait();
                nameConnectionEntered = nameField.getText();
                timeConnectionEntered = timeField.getText();
                if (nameConnectionEntered.trim() == null || timeConnectionEntered.trim() == null && Valid.isValidInt(timeConnectionEntered)) {
                    Alert incorrect = new Alert(Alert.AlertType.ERROR);
                    incorrect.setHeaderText("You have entered information incorrectly");
                    incorrect.showAndWait();
                } else {
                    graph.connect(selectedLoc1, selectedLoc2, nameConnectionEntered, Integer.parseInt(timeConnectionEntered));
                    Line line = new Line(selectedLoc1.getXcor(), selectedLoc1.getYcor(), selectedLoc2.getXcor(), selectedLoc2.getYcor());
                    line.setStrokeWidth(1);
                    line.setDisable(true);
                    pane.getChildren().add(line);
                }
            }
        }
    }

    class ClickHandlerShowConnection implements EventHandler<ActionEvent> {
        public void handle(ActionEvent event) {
            if (selectedLoc1 == null || selectedLoc2 == null) {
                Alert selectedError = new Alert(Alert.AlertType.ERROR);
                selectedError.setTitle("Error!");
                selectedError.setHeaderText("You must select two cities to view their connection");
                selectedError.showAndWait();
            } else {
                Edge<Location> edge = graph.getEdgeBetween(selectedLoc1, selectedLoc2);
                if (edge != null) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Connection Information");
                    alert.setHeaderText("Connection from " + selectedLoc1.getCityName() + " to " + selectedLoc2.getCityName());
                    GridPane newGrid = new GridPane();
                    newGrid.setHgap(10);
                    newGrid.setVgap(10);
                    Label nameLabel = new Label("Name:");
                    Label timeLabel = new Label("Time:");
                    TextField nameText = new TextField();
                    nameText.setText(edge.getName());
                    nameText.setEditable(false);
                    TextField timeText = new TextField();
                    timeText.setEditable(false);
                    timeText.setText(String.valueOf(edge.getWeight()));
                    newGrid.add(nameLabel, 0, 0);
                    newGrid.add(timeLabel, 0, 1);
                    newGrid.add(nameText, 1, 0);
                    newGrid.add(timeText, 1, 1);
                    alert.getDialogPane().setContent(newGrid);

                    alert.showAndWait();
                } else {
                    Alert newAlert = new Alert(Alert.AlertType.ERROR);
                    newAlert.setTitle("Error!");
                    newAlert.setHeaderText("There is no connection between these two cities");
                    newAlert.showAndWait();

                }
            }
        }
    }
    class ClickHandlerChangeConnection implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            saved = false;
            if (selectedLoc1 == null || selectedLoc2 == null){
                Alert selectTwoCitiesAlert = new Alert(Alert.AlertType.ERROR);
                selectTwoCitiesAlert.setTitle("Error!");
                selectTwoCitiesAlert.setHeaderText("You have to select two cities to change the connection!");
                selectTwoCitiesAlert.showAndWait();

            } else if (graph.getEdgeBetween(selectedLoc1, selectedLoc2) == null) {
                Alert newAlert = new Alert(Alert.AlertType.ERROR);
                newAlert.setHeaderText("There is no connection between these two cities");
                newAlert.showAndWait();

            } else {
                Edge<Location> edge = graph.getEdgeBetween(selectedLoc1, selectedLoc2);

                Alert changeAlert = new Alert(Alert.AlertType.CONFIRMATION);
                changeAlert.setTitle("Connection");
                changeAlert.setHeaderText("Connection from " + selectedLoc1.getCityName() + " to " + selectedLoc2.getCityName());
                Label nameLabel = new Label("Name:");
                Label timeLabel = new Label("Time:");
                TextField nameText = new TextField(edge.getName());
                nameText.setEditable(false);
                TextField timeText = new TextField();
                GridPane grid = new GridPane();
                grid.setHgap(10);
                grid.setVgap(10);
                grid.add(nameLabel, 0, 0);
                grid.add(nameText, 1, 0);
                grid.add(timeLabel, 0, 1);
                grid.add(timeText, 1, 1);

                changeAlert.getDialogPane().setContent(grid);
                changeAlert.getDialogPane().setPadding(new Insets(10));
                Optional<ButtonType> result = changeAlert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    String time = timeText.getText().trim();

                    if (!time.isEmpty() && Valid.isValidInt(time)) {
                        int newWeight = Integer.parseInt(time);
                        graph.setConnectionWeight(selectedLoc1, selectedLoc2, newWeight);
                        graph.setConnectionWeight(selectedLoc2, selectedLoc1, newWeight);
                    }else {
                        Alert invalidInput = new Alert(Alert.AlertType.ERROR);
                        invalidInput.setHeaderText("Invalid input for time.");
                        invalidInput.showAndWait();
                    }
                }
            }
        }
    }

    class ClickHandlerFindPath implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            StringBuilder sb = new StringBuilder();
            if (selectedLoc1 == null || selectedLoc2 == null) {
                Alert alert1 = new Alert(Alert.AlertType.ERROR);
                alert1.setHeaderText("You have to enter two cities");
                alert1.showAndWait();
            } else {
                graph.getPath(selectedLoc1, selectedLoc2);
                Alert answer = new Alert(Alert.AlertType.INFORMATION);
                TextArea textArea = new TextArea();
                answer.setTitle("Message");
                String path = String.valueOf(graph.getPath(selectedLoc1, selectedLoc2));
                int totWeight = 0;
                for (Edge<Location> oneLoc : graph.getPath(selectedLoc1, selectedLoc2)) {
                    String name = oneLoc.getDestination().getCityName();
                    int weight = oneLoc.getWeight();
                    String nameEdge = oneLoc.getName();
                    totWeight += weight;
                    sb.append("to " + name + " by " + nameEdge + " takes " + weight + "\n");
                }
                sb.append("Total " + totWeight);
                textArea.appendText(sb.toString());
                answer.setHeaderText("The path from " + selectedLoc1.getCityName() + " to " + selectedLoc2.getCityName());
                answer.getDialogPane().setContent(textArea);
                answer.showAndWait();
            }
        }
    }
}