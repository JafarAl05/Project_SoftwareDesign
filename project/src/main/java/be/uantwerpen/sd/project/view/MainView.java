package be.uantwerpen.sd.project.view;

import be.uantwerpen.sd.project.controller.PlannerController;
import be.uantwerpen.sd.project.model.*;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.stream.Collectors;

/**
 * The VIEW of MVC: draws everything and passes clicks on to the controller.
 * Listens (PropertyChangeListener) to the model and redraws on every change.
 */
public class MainView extends TabPane implements PropertyChangeListener {
    private final MealPlannerDB model;
    private final PlannerController controller;

    // tab 1: recipes
    private final ListView<Recipe> recipeList = new ListView<>();
    private final TextField title = new TextField();
    private final TextArea description = new TextArea();
    private final TextArea ingredients = new TextArea();
    private final TextField tags = new TextField();
    // tab 2: week plan (7 days x 4 meals)
    private final ComboBox<Recipe>[][] cells = new ComboBox[7][4];
    private boolean refreshing;   // true while WE fill the combo boxes -> do not send commands
    // tab 3: groceries
    private final VBox groceryBox = new VBox(6);

    public MainView(MealPlannerDB model, PlannerController controller) {
        this.model = model;
        this.controller = controller;
        getTabs().addAll(recipesTab(), planTab(), groceryTab());
        setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
        model.addPropertyChangeListener(this);   // MVC observer: notify me on every change
        refresh();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) { refresh(); }

    // ======================= Tab 1: recipes =======================
    private Tab recipesTab() {
        title.setPromptText("Title");
        description.setPromptText("Description");
        ingredients.setPromptText("One per line, e.g.   200 g pasta   /   2 piece egg   /   0.5 l milk");
        tags.setPromptText("Tags, separated by commas (vegetarian, quick)");
        description.setPrefRowCount(2);

        recipeList.getSelectionModel().selectedItemProperty().addListener((obs, old, r) -> fillForm(r));

        Button add = new Button("Add");
        Button save = new Button("Save changes");
        Button remove = new Button("Remove");
        Button clear = new Button("Clear form");
        add.setOnAction(e -> safe(() -> controller.addRecipe(title.getText(), description.getText(), ingredients.getText(), tags.getText())));
        save.setOnAction(e -> withSelected(r -> controller.editRecipe(r, title.getText(), description.getText(), ingredients.getText(), tags.getText())));
        remove.setOnAction(e -> withSelected(controller::removeRecipe));
        clear.setOnAction(e -> recipeList.getSelectionModel().clearSelection());

        VBox form = new VBox(8, new Label("Recipe"), title, description, new Label("Ingredients"), ingredients,
                tags, new HBox(8, add, save, remove, clear));
        HBox root = new HBox(12, recipeList, form);
        HBox.setHgrow(form, Priority.ALWAYS);
        root.setPadding(new Insets(12));
        return new Tab("Recipes", root);
    }

    private void fillForm(Recipe r) {
        title.setText(r == null ? "" : r.getTitle());
        description.setText(r == null ? "" : r.getDescription());
        ingredients.setText(r == null ? "" : r.getIngredients().stream().map(Ingredient::toString).collect(Collectors.joining("\n")));
        tags.setText(r == null ? "" : String.join(", ", r.getTags()));
    }

    private void withSelected(java.util.function.Consumer<Recipe> action) {
        Recipe r = recipeList.getSelectionModel().getSelectedItem();
        if (r == null) showError("Select a recipe first.");
        else safe(() -> action.accept(r));
    }

    // ======================= Tab 2: week plan =======================
    private Tab planTab() {
        GridPane grid = new GridPane();
        grid.setHgap(6);
        grid.setVgap(6);
        for (int d = 0; d < 7; d++) {
            String day = WeekPlan.DAYS[d];
            grid.add(new Label(day), d + 1, 0);
            for (int s = 0; s < 4; s++) {
                String slot = WeekPlan.SLOTS[s];
                if (d == 0) grid.add(new Label(slot), 0, s + 1);
                ComboBox<Recipe> combo = new ComboBox<>();
                combo.setPrefWidth(120);
                combo.setOnAction(e -> {
                    if (!refreshing && combo.getValue() != model.getMeal(day, slot))
                        controller.assignMeal(day, slot, combo.getValue());
                });
                Button x = new Button("✕");
                x.setOnAction(e -> controller.clearMeal(day, slot));
                cells[d][s] = combo;
                grid.add(new HBox(2, combo, x), d + 1, s + 1);
            }
        }
        Button undo = new Button("↶ Undo");
        Button redo = new Button("↷ Redo");
        undo.setOnAction(e -> controller.undo());
        redo.setOnAction(e -> controller.redo());
        ScrollPane scroll = new ScrollPane(grid);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        VBox root = new VBox(12, scroll, new HBox(8, undo, redo));
        root.setPadding(new Insets(12));
        return new Tab("Week plan", root);
    }

    // ======================= Tab 3: groceries =======================
    private Tab groceryTab() {
        TextField amount = new TextField();
        TextField unit = new TextField();
        TextField name = new TextField();
        amount.setPromptText("amount");
        unit.setPromptText("unit (g, ml, piece)");
        name.setPromptText("name, e.g. dish soap");
        Button add = new Button("Add extra");
        add.setOnAction(e -> safe(() -> {
            controller.addExtra(name.getText(), amount.getText(), unit.getText());
            name.clear();
            amount.clear();
            unit.clear();
        }));
        ScrollPane scroll = new ScrollPane(groceryBox);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        VBox root = new VBox(12, new Label("Calculated automatically from your week plan:"), scroll,
                new HBox(8, amount, unit, name, add));
        root.setPadding(new Insets(12));
        return new Tab("Groceries", root);
    }

    // ======================= redraw everything =======================
    private void refresh() {
        refreshing = true;
        recipeList.getItems().setAll(model.getRecipes());
        for (int d = 0; d < 7; d++) {
            for (int s = 0; s < 4; s++) {
                cells[d][s].getItems().setAll(model.getRecipes());
                cells[d][s].setValue(model.getMeal(WeekPlan.DAYS[d], WeekPlan.SLOTS[s]));
            }
        }
        groceryBox.getChildren().clear();
        for (GroceryItem item : model.getGroceries()) {
            CheckBox box = new CheckBox(item.toString());
            box.setSelected(item.isBought());
            box.setOnAction(e -> controller.toggleBought(item));
            groceryBox.getChildren().add(box);
        }
        refreshing = false;
    }

    /** Runs an action and shows invalid input as a pop-up instead of crashing. */
    private void safe(Runnable action) {
        try {
            action.run();
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void showError(String message) { new Alert(Alert.AlertType.ERROR, message).showAndWait(); }
}
