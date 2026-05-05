import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LoginUI extends Application {
    private UserAuth auth = new UserAuth();

    @Override
    public void start(Stage stage) {
        Label lblUser = new Label("Username:");
        TextField txtUser = new TextField();
        Label lblPass = new Label("Password:");
        PasswordField txtPass = new PasswordField();

        Button btnLogin = new Button("Login");
        Button btnRegister = new Button("Register");
        Label lblMsg = new Label();

        btnLogin.setOnAction(e -> {
            if (auth.login(txtUser.getText(), txtPass.getText())) {
                lblMsg.setText("Login successful!");
                ConverterUI converterUI = new ConverterUI(txtUser.getText());
                Stage converterStage = new Stage();
                converterUI.show(converterStage);
                stage.close();
            } else {
                lblMsg.setText("Login failed. Try again.");
            }
        });

        btnRegister.setOnAction(e -> {
            if (auth.register(txtUser.getText(), txtPass.getText())) {
                lblMsg.setText("Registration successful! Please login.");
            } else {
                lblMsg.setText("Username already exists.");
            }
        });

        VBox root = new VBox(10, lblUser, txtUser, lblPass, txtPass, btnLogin, btnRegister, lblMsg);
        Scene scene = new Scene(root, 300, 250);

        stage.setTitle("Currency Converter Login");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
