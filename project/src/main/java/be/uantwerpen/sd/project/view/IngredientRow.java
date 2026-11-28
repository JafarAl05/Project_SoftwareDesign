package be.uantwerpen.sd.project.view;

import be.uantwerpen.sd.project.model.Ingredient;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/** One row of the ingredient form:  [amount] [unit ▼] [ingredient] [✕] */
public class IngredientRow extends HBox {
    private final TextField amount = new TextField();
    private final ComboBox<String> unit = new ComboBox<>();
    private final TextField name = new TextField();

    public IngredientRow(Ingredient ingredient, Runnable onRemove) {
        super(6);
        amount.setPromptText("amount");
        amount.setPrefWidth(80);
        unit.getItems().setAll(Ingredient.UNITS);
        unit.setValue("g");
        unit.setPrefWidth(90);
        name.setPromptText("ingredient, e.g. pasta");
        HBox.setHgrow(name, Priority.ALWAYS);
        if (ingredient != null) {                      // editing: show the existing values
            amount.setText(String.valueOf(ingredient.getAmount()));
            unit.setValue(ingredient.getUnit());
            name.setText(ingredient.getName());
        }
        Button remove = new Button("✕");
        remove.setOnAction(e -> onRemove.run());
        getChildren().addAll(amount, unit, name, remove);
    }

    /** {amount, unit, name}: what the controller needs. */
    public String[] values() { return new String[]{amount.getText(), unit.getValue(), name.getText()}; }
}
