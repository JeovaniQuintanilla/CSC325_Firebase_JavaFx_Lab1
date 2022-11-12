package modelview;

import com.mycompany.mvvmexample.App;
import viewmodel.AccessDataViewModel;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.mycompany.mvvmexample.FirestoreContext;
import com.mycompany.mvvmexample.FirestoreContext;
import com.mycompany.mvvmexample.FirestoreContext;
import com.mycompany.mvvmexample.FirestoreContext;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import models.Person;

public class AccessFBView {

    @FXML
    private TextField nameField;
    @FXML
    private TextField majorField;
    @FXML
    private TextField ageField;
    @FXML
    private Button writeButton;
    @FXML
    private Button readButton;
    
    private boolean key;
    private ObservableList<Person> listOfUsers = FXCollections.observableArrayList();
    private Person person;
    Person rowToDelete;
    
    @FXML
    private TableView<Person> tableVW;
    @FXML
    private TableColumn<Person, String> NameCol;
    @FXML
    private TableColumn<Person, String> MajorCol;
    @FXML
    private TableColumn<Person, Integer> AgeCol;
    @FXML
    private Button deleteBtn;
    @FXML
    private Label selectionLabel;
    @FXML
    private TextField newNameField;
    @FXML
    private TextField newMajorField;
    @FXML
    private TextField newAgeField;

    public ObservableList<Person> getListOfUsers() {

        return listOfUsers;
    }

    void initialize() {

        AccessDataViewModel accessDataViewModel = new AccessDataViewModel();
        nameField.textProperty().bindBidirectional(accessDataViewModel.userNameProperty());
        majorField.textProperty().bindBidirectional(accessDataViewModel.userMajorProperty());
        writeButton.disableProperty().bind(accessDataViewModel.isWritePossibleProperty().not());
    }

    @FXML
    private void addRecord(ActionEvent event) {
        addData();
    }

    @FXML
    private void readRecord(ActionEvent event) {

        readFirebase();
    }

    public void addData() {

        DocumentReference docRef = App.fstore.collection("References").document(UUID.randomUUID().toString());
        // Add document data  with id "alovelace" using a hashmap
        Map<String, Object> data = new HashMap<>();
        data.put("Name", nameField.getText());
        data.put("Major", majorField.getText());
        data.put("Age", Integer.parseInt(ageField.getText()));
        //asynchronously write data
        nameField.setText(null);
        majorField.setText(null);
        ageField.setText(null);
        ApiFuture<WriteResult> result = docRef.set(data);
    }

    public boolean readFirebase() {
        key = false;

        //asynchronously retrieve all documents
        ApiFuture<QuerySnapshot> future = App.fstore.collection("References").get();
        // future.get() blocks on response
        List<QueryDocumentSnapshot> documents;
        try {
            documents = future.get().getDocuments();
            if (documents.size() > 0) {
                tableVW.getItems().clear();
                System.out.println("Outing....");
                for (QueryDocumentSnapshot document : documents) {

                    person = new Person(String.valueOf(document.getData().get("Name")),
                            document.getData().get("Major").toString(),
                            Integer.parseInt(document.getData().get("Age").toString()));

                    listOfUsers.add(person);
                    NameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
                    MajorCol.setCellValueFactory(new PropertyValueFactory<>("major"));
                    AgeCol.setCellValueFactory(new PropertyValueFactory<>("age"));
                    tableVW.setItems(listOfUsers);

                }

            } else {
                System.out.println("No data");
            }
            key = true;

        } catch (InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
        }
        return key;
    }
 
    @FXML
    private void deleteData(ActionEvent event) {
        
         
        Person rowToDelete = tableVW.getSelectionModel().getSelectedItem();
        ApiFuture<QuerySnapshot> future = App.fstore.collection("References").get();
     
        ApiFuture<WriteResult> writeResult;
        List<QueryDocumentSnapshot> documents;
        try {
            documents = future.get().getDocuments();
            //Integer age = rowToDelete.getAge();

            for (QueryDocumentSnapshot document : documents) {
                if (document.getData().get("Name").equals(rowToDelete.getName()) && document.getData().get("Major").equals(rowToDelete.getMajor())) {
                    writeResult = App.fstore.collection("References").document(document.getId()).delete();
                    selectionLabel.setText("Row Deleted, Re-Read To See New Table");
                    nameField.setText(null);majorField.setText(null);ageField.setText(null);
 
                } 
            }

        } catch (InterruptedException ex) {} catch (ExecutionException ex) {}
    }

   

    @FXML
    private void SendData(MouseEvent event) {
        Integer age = tableVW.getSelectionModel().getSelectedItem().getAge();

        nameField.setText(tableVW.getSelectionModel().getSelectedItem().getName());
        majorField.setText(tableVW.getSelectionModel().getSelectedItem().getMajor());
        ageField.setText(age.toString());
    }

    @FXML
    private void updateData(ActionEvent event) {
        Person rowToUpdate = tableVW.getSelectionModel().getSelectedItem();
        ApiFuture<QuerySnapshot> future = App.fstore.collection("References").get();
        ApiFuture<WriteResult> writeResult;
        
         List<QueryDocumentSnapshot> documents;
         try {
            documents = future.get().getDocuments();
            
            for (QueryDocumentSnapshot document : documents) {
                if((newNameField != null && newMajorField!= null && newAgeField!=null) &&
                        (document.getData().get("Name").equals(rowToUpdate.getName()) && document.getData().get("Major").equals(rowToUpdate.getMajor()))){
                        DocumentReference docRef =  App.fstore.collection("References").document(document.getId());
                        writeResult = docRef.update("Name", newNameField.getText());
                        writeResult = docRef.update("Major", newMajorField.getText());
                        writeResult = docRef.update("Age", newAgeField.getText());
                        
                    //String id = ;
                    System.out.println(document.getId() );
                    
                }
            }
        
        
        
            
            
        
         } catch (InterruptedException ex) {} catch (ExecutionException ex) {}
    }
}
