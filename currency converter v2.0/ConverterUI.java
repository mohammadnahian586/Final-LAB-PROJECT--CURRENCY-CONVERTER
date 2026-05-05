import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConverterUI {
    private String username;
    private ApiRateFetcher fetcher = new ApiRateFetcher();

    public ConverterUI(String username) {
        this.username = username;
    }

    public void show(Stage stage) {
        Label lblWelcome = new Label("Welcome " + username + "!");
        Label lblBase = new Label("Base Currency");
        ComboBox<String> cbBase = new ComboBox<>();
        Label lblTarget = new Label("Target Currency");
        ComboBox<String> cbTarget = new ComboBox<>();
        Label lblAmount = new Label("Amount");
        TextField txtAmount = new TextField();
        Button btnConvert = new Button("Convert");
        TextArea txtResult = new TextArea();
        txtResult.setEditable(false);
        txtResult.setWrapText(true);

        ProgressIndicator progress = new ProgressIndicator();
        progress.setVisible(false);

        // Disable controls until currencies loaded
        cbBase.setDisable(true);
        cbTarget.setDisable(true);
        btnConvert.setDisable(true);
        txtAmount.setDisable(true);

        // Background task to load all currencies (probe with USD)
        Task<ObservableList<String>> loadCurrenciesTask = new Task<>() {
            @Override
            protected ObservableList<String> call() {
                // Try to fetch using USD as probe; if fails, fall back to a small built-in list
                Map<String, Double> rates = fetcher.getAllRates("USD");
                if (rates == null || rates.isEmpty()) {
                    List<String> fallback = new ArrayList<>();
                    fallback.add("USD"); fallback.add("EUR"); fallback.add("GBP"); fallback.add("BDT");
                    return FXCollections.observableArrayList(fallback);
                } else {
                    return FXCollections.observableArrayList(rates.keySet());
                }
            }
        };

        loadCurrenciesTask.setOnRunning(e -> {
            progress.setVisible(true);
        });

        loadCurrenciesTask.setOnSucceeded(e -> {
            ObservableList<String> all = loadCurrenciesTask.getValue();
            cbBase.setItems(all);
            cbTarget.setItems(all);
            if (all.contains("USD")) cbBase.setValue("USD");
            else if (!all.isEmpty()) cbBase.setValue(all.get(0));
            // set default target
            if (all.contains("BDT")) cbTarget.setValue("BDT");
            else if (!all.isEmpty()) cbTarget.setValue(all.get(0));

            cbBase.setDisable(false);
            cbTarget.setDisable(false);
            btnConvert.setDisable(false);
            txtAmount.setDisable(false);
            progress.setVisible(false);

            // Preload rates for the selected base so target list is accurate (optional)
            populateTargetsForBase(cbBase.getValue(), cbTarget);
        });

        loadCurrenciesTask.setOnFailed(e -> {
            progress.setVisible(false);
            cbBase.setDisable(false);
            cbTarget.setDisable(false);
            btnConvert.setDisable(false);
            txtAmount.setDisable(false);
            // fallback small list
            ObservableList<String> fallback = FXCollections.observableArrayList("USD","EUR","GBP","BDT","INR","JPY");
            cbBase.setItems(fallback);
            cbTarget.setItems(fallback);
            cbBase.setValue("USD");
            cbTarget.setValue("BDT");
        });

        new Thread(loadCurrenciesTask).start();

        // When base changes, repopulate target list using getAllRates(base)
        cbBase.setOnAction(e -> {
            String base = cbBase.getValue();
            if (base != null && !base.isEmpty()) {
                // run in background to avoid blocking UI
                Task<ObservableList<String>> t = new Task<>() {
                    @Override
                    protected ObservableList<String> call() {
                        Map<String, Double> rates = fetcher.getAllRates(base);
                        if (rates == null || rates.isEmpty()) {
                            return FXCollections.observableArrayList();
                        } else {
                            return FXCollections.observableArrayList(rates.keySet());
                        }
                    }
                };
                t.setOnSucceeded(ev -> {
                    ObservableList<String> targets = t.getValue();
                    if (!targets.isEmpty()) {
                        cbTarget.setItems(targets);
                        if (targets.contains("BDT")) cbTarget.setValue("BDT");
                        else cbTarget.setValue(targets.get(0));
                    }
                });
                t.setOnFailed(ev -> {
                    // ignore; keep previous items
                });
                new Thread(t).start();
            }
        });

        btnConvert.setOnAction(e -> {
            try {
                String base = cbBase.getValue();
                String target = cbTarget.getValue();
                if (base == null || target == null) {
                    txtResult.setText("Please select both base and target currencies.");
                    return;
                }
                double amount = Double.parseDouble(txtAmount.getText().trim());
                // fetch rate in background
                Task<Double> rateTask = new Task<>() {
                    @Override
                    protected Double call() {
                        return fetcher.getRate(base, target);
                    }
                };
                rateTask.setOnSucceeded(ev -> {
                    double rate = rateTask.getValue();
                    if (rate == -1) {
                        txtResult.setText("Conversion failed. Check currency codes or internet connection.");
                        return;
                    }
                    double result = amount * rate;
                    StringBuilder sb = new StringBuilder();
                    sb.append("=== Conversion Result ===\n");
                    sb.append(String.format("%.4f %s = %.4f %s\n", amount, base, result, target));
                    sb.append(String.format("Rate 1 %s = %.6f %s\n", base, rate, target));
                    sb.append("=========================\n");
                    txtResult.setText(sb.toString());
                });
                rateTask.setOnFailed(ev -> {
                    txtResult.setText("Error fetching rate: " + rateTask.getException().getMessage());
                });
                new Thread(rateTask).start();
            } catch (NumberFormatException nfe) {
                txtResult.setText("Enter a valid numeric amount.");
            } catch (Exception ex) {
                txtResult.setText("Error: " + ex.getMessage());
            }
        });

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(12));
        grid.setHgap(10);
        grid.setVgap(10);

        grid.add(lblWelcome, 0, 0, 2, 1);
        grid.add(lblBase, 0, 1);
        grid.add(cbBase, 1, 1);
        grid.add(lblTarget, 0, 2);
        grid.add(cbTarget, 1, 2);
        grid.add(lblAmount, 0, 3);
        grid.add(txtAmount, 1, 3);
        grid.add(btnConvert, 1, 4);
        grid.add(txtResult, 0, 5, 2, 1);
        grid.add(progress, 0, 6);

        Scene scene = new Scene(grid, 520, 420);
        stage.setTitle("Currency Converter");
        stage.setScene(scene);
        stage.show();
    }

    // helper to populate target list for a given base (synchronous call used after initial load)
    private void populateTargetsForBase(String base, ComboBox<String> cbTarget) {
        Task<ObservableList<String>> t = new Task<>() {
            @Override
            protected ObservableList<String> call() {
                Map<String, Double> rates = fetcher.getAllRates(base);
                if (rates == null || rates.isEmpty()) return FXCollections.observableArrayList();
                return FXCollections.observableArrayList(rates.keySet());
            }
        };
        t.setOnSucceeded(ev -> {
            ObservableList<String> targets = t.getValue();
            if (!targets.isEmpty()) {
                cbTarget.setItems(targets);
                if (targets.contains("BDT")) cbTarget.setValue("BDT");
                else cbTarget.setValue(targets.get(0));
            }
        });
        new Thread(t).start();
    }
}
