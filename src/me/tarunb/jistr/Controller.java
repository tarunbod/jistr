package me.tarunb.jistr;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;
import java.util.Scanner;

public class Controller {

    @FXML
    private TextField filenameField;

    @FXML
    private TextField descriptionField;

    @FXML
    private TextArea codeArea;

    @FXML
    public void onPostButtonPressed(ActionEvent e) {

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to post this?", ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            JSONObject data = new JSONObject();
            data.put("public", true);
            data.put("description", descriptionField.getText());

            JSONObject contentObj = new JSONObject();
            contentObj.put("content", codeArea.getText());

            JSONObject filesObj = new JSONObject();
            filesObj.put(filenameField.getText(), contentObj);

            data.put("files", filesObj);

            try {
                URL postGistUrl = new URL("https://api.github.com/gists");
                HttpURLConnection con = (HttpURLConnection) postGistUrl.openConnection();
                con.setRequestProperty("Accept-Charset", "UTF-8");
                con.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                con.setDoOutput(true);

                OutputStream out = con.getOutputStream();
                out.write(data.toJSONString().getBytes("UTF-8"));
                out.close();

                StringBuilder response = new StringBuilder();
                Scanner in = new Scanner(con.getInputStream());
                while (in.hasNext()) {
                    response.append(in.next());
                }
                in.close();

                JSONObject responseObj = (JSONObject) new JSONParser().parse(response.toString());
                String createdURL = (String) responseObj.get("html_url");

                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(createdURL), null);
                new Alert(
                        Alert.AlertType.INFORMATION,
                        "Your new gist is posted at: " + createdURL + ". It has been copied to your clipboard",
                        ButtonType.OK
                ).showAndWait();

                con.disconnect();
            } catch (Exception err) {
                System.out.println("ERROR:\n");
                err.printStackTrace();

                Alert alert = new Alert(Alert.AlertType.INFORMATION, "An error occurred when posting your gist:\n" + err.toString(), ButtonType.OK);
                alert.setHeaderText("Error when posting");
                alert.showAndWait();
            }
        }
    }
}
