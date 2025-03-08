import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.*;
import java.util.List;

public class BookCatalogueApp extends Application {

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "password";
    private static final String VIEWER_USERNAME = "viewer";
    private static final String VIEWER_PASSWORD = "viewonly";
    private static final String FILE_NAME = "books.txt";

    private Stage primaryStage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        showLoginScreen();
    }

    private void showLoginScreen() {
        Label usernameLabel = new Label("Username:");
        TextField usernameField = new TextField();
        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();
        Button loginButton = new Button("Login");

        loginButton.setOnAction(e -> checkLogin(usernameField.getText(), passwordField.getText()));

        VBox loginLayout = new VBox(10);
        loginLayout.setPadding(new Insets(10));
        loginLayout.getChildren().addAll(usernameLabel, usernameField, passwordLabel, passwordField, loginButton);

        Scene loginScene = new Scene(loginLayout, 300, 200);
        primaryStage.setScene(loginScene);
        primaryStage.setTitle("Login");
        primaryStage.show();

        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Login Information");
        alert.setHeaderText("Admin and Viewer Accounts");
        alert.setContentText("Admin: Use 'admin' / 'password'\nViewer: Use 'viewer' / 'viewonly'");
        alert.showAndWait();
    }

    private void checkLogin(String username, String password) {
        if (ADMIN_USERNAME.equals(username) && ADMIN_PASSWORD.equals(password)) {
            showCatalogueScreen("Admin");
        } else if (VIEWER_USERNAME.equals(username) && VIEWER_PASSWORD.equals(password)) {
            showCatalogueScreen("Viewer");
        } else {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Login Failed");
            alert.setHeaderText(null);
            alert.setContentText("Invalid username or password.");
            alert.showAndWait();
        }
    }

    private void showCatalogueScreen(String role) {
        ListView<Book> bookList = new ListView<>();
        loadBooksFromFile(bookList);

        Button addButton = new Button("Add");
        Button editButton = new Button("Edit");
        Button deleteButton = new Button("Delete");
        Button logoutButton = new Button("Logout");

        if ("Viewer".equals(role)) {
            addButton.setDisable(true);
            editButton.setDisable(true);
            deleteButton.setDisable(true);
        }

        setupAddButton(addButton, bookList);
        setupEditButton(editButton, bookList);
        setupDeleteButton(deleteButton, bookList);
        setupLogoutButton(logoutButton);

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));
        layout.getChildren().addAll(bookList, addButton, editButton, deleteButton, logoutButton);

        Scene scene = new Scene(layout, 400, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Book Catalogue");
        primaryStage.show();
    }

    private void setupAddButton(Button addButton, ListView<Book> bookList) {
        addButton.setOnAction(e -> {
            TextInputDialog titleDialog = new TextInputDialog();
            titleDialog.setHeaderText("Enter the book title");
            titleDialog.setTitle("Add Book");
            titleDialog.setGraphic(null);
            titleDialog.showAndWait().ifPresent(title -> {
                TextInputDialog authorDialog = new TextInputDialog();
                authorDialog.setHeaderText("Enter the book author");
                authorDialog.setTitle("Add Book Author");
                authorDialog.setGraphic(null);
                authorDialog.showAndWait().ifPresent(author -> {
                    Book newBook = new Book(title, author);
                    bookList.getItems().add(newBook);
                    saveBooksToFile(bookList);
                });
            });
        });
    }

    private void setupEditButton(Button editButton, ListView<Book> bookList) {
        editButton.setOnAction(e -> {
            Book selectedBook = bookList.getSelectionModel().getSelectedItem();
            if (selectedBook != null) {
                TextInputDialog titleDialog = new TextInputDialog(selectedBook.getTitle());
                titleDialog.setHeaderText("Edit the book title");
                titleDialog.setTitle("Edit Book");
                titleDialog.setGraphic(null);
                titleDialog.showAndWait().ifPresent(newTitle -> {
                    TextInputDialog authorDialog = new TextInputDialog(selectedBook.getAuthor());
                    authorDialog.setHeaderText("Edit the book author");
                    authorDialog.setTitle("Edit Book Author");
                    authorDialog.setGraphic(null);
                    authorDialog.showAndWait().ifPresent(newAuthor -> {
                        selectedBook.setTitle(newTitle);
                        selectedBook.setAuthor(newAuthor);
                        bookList.refresh();
                        saveBooksToFile(bookList);
                    });
                });
            } else {
                showAlert("No book selected", "Please select a book to edit.", AlertType.WARNING);
            }
        });
    }

    private void setupDeleteButton(Button deleteButton, ListView<Book> bookList) {
        deleteButton.setOnAction(e -> {
            Book selectedBook = bookList.getSelectionModel().getSelectedItem();
            if (selectedBook != null) {
                bookList.getItems().remove(selectedBook);
                saveBooksToFile(bookList);
            } else {
                showAlert("No book selected", "Please select a book to delete.", AlertType.WARNING);
            }
        });
    }

    private void setupLogoutButton(Button logoutButton) {
        logoutButton.setOnAction(e -> showLoginScreen());
    }

    private void saveBooksToFile(ListView<Book> bookList) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (Book book : bookList.getItems()) {
                writer.write(book.getTitle() + ";" + book.getAuthor());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Unable to save books to file.", AlertType.ERROR);
        }
    }

    private void loadBooksFromFile(ListView<Book> bookList) {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length == 2) {
                    bookList.getItems().add(new Book(parts[0], parts[1]));
                }
            }
        } catch (IOException e) {
            // If no file exists, it's fine (first launch).
        }
    }

    private void showAlert(String title, String message, AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class Book {
        private String title;
        private String author;

        public Book(String title, String author) {
            this.title = title;
            this.author = author;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        @Override
        public String toString() {
            return title + " by " + author;
        }
    }
}