package be.uantwerpen.sd.project.view;

import be.uantwerpen.sd.project.controller.PlannerController;
import be.uantwerpen.sd.project.model.*;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

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
    private final TextField calories = new TextField();
    private final VBox ingredientRows = new VBox(6);     // one IngredientRow per ingredient
    private final TextField tags = new TextField();
    // tab 2: week plan (7 days x 4 meals) + kcal per day
    private final ComboBox<Recipe>[][] cells = new ComboBox[7][4];
    private final Label[] dayTotals = new Label[7];
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
        description.setPrefRowCount(2);
        calories.setPromptText("kcal per serving, e.g. 550");
        tags.setPromptText("Tags, separated by commas (vegetarian, quick)");
        fillForm(null);

        recipeList.getSelectionModel().selectedItemProperty().addListener((obs, old, r) -> fillForm(r));

        Button addRow = new Button("+ Ingredient");
        addRow.setOnAction(e -> addIngredientRow(null));
        Button add = new Button("Add");
        Button save = new Button("Save changes");
        Button remove = new Button("Remove");
        Button clear = new Button("Clear form");
        add.getStyleClass().add("primary");
        add.setOnAction(e -> safe(() -> controller.addRecipe(title.getText(), description.getText(), calories.getText(), rows(), tags.getText())));
        save.setOnAction(e -> withSelected(r -> controller.editRecipe(r, title.getText(), description.getText(), calories.getText(), rows(), tags.getText())));
        remove.setOnAction(e -> withSelected(controller::removeRecipe));
        clear.setOnAction(e -> { recipeList.getSelectionModel().clearSelection(); fillForm(null); });

        VBox form = new VBox(8, header("Recipe"), title, description, header("Calories (kcal per serving)"), calories,
                header("Ingredients"), ingredientRows, addRow,
                header("Tags"), tags, new HBox(8, add, save, remove, clear));
        form.getStyleClass().add("card");
        HBox root = new HBox(12, recipeList, form);
        HBox.setHgrow(form, Priority.ALWAYS);
        root.setPadding(new Insets(12));
        return new Tab("Recipes", root);
    }

    private void fillForm(Recipe r) {
        title.setText(r == null ? "" : r.getTitle());
        description.setText(r == null ? "" : r.getDescription());
        calories.setText(r == null ? "" : String.valueOf(r.getCalories()));
        tags.setText(r == null ? "" : String.join(", ", r.getTags()));
        ingredientRows.getChildren().clear();
        if (r == null) addIngredientRow(null);                      // new recipe: one empty row
        else for (Ingredient i : r.getIngredients()) addIngredientRow(i);
    }

    private void addIngredientRow(Ingredient ingredient) {
        IngredientRow[] row = new IngredientRow[1];   // array trick: the lambda may refer to its own row
        row[0] = new IngredientRow(ingredient, () -> ingredientRows.getChildren().remove(row[0]));
        ingredientRows.getChildren().add(row[0]);
    }

    /** All ingredient rows as {amount, unit, name}. */
    private String[][] rows() {
        return ingredientRows.getChildren().stream().map(n -> ((IngredientRow) n).values()).toArray(String[][]::new);
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
            grid.add(header(day), d + 1, 0);
            for (int s = 0; s < 4; s++) {
                String slot = WeekPlan.SLOTS[s];
                if (d == 0) grid.add(header(slot), 0, s + 1);
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
            dayTotals[d] = new Label();
            dayTotals[d].getStyleClass().add("kcal");
            grid.add(dayTotals[d], d + 1, 5);
        }
        grid.add(header("Total"), 0, 5);
        Button undo = new Button("↶ Undo");
        Button redo = new Button("↷ Redo");
        undo.setOnAction(e -> controller.undo());
        redo.setOnAction(e -> controller.redo());
        grid.getStyleClass().add("card");
        ScrollPane scroll = new ScrollPane(grid);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        VBox root = new VBox(12, scroll, new HBox(8, undo, redo));
        root.setPadding(new Insets(12));
        return new Tab("Week plan", root);
    }

    // ======================= Tab 3: groceries =======================
    private Tab groceryTab() {
        TextField amount = new TextField();
        ComboBox<String> unit = new ComboBox<>();
        TextField name = new TextField();
        amount.setPromptText("amount");
        unit.getItems().setAll(Ingredient.UNITS);
        unit.setValue("piece");
        name.setPromptText("name, e.g. dish soap");
        Button add = new Button("Add extra");
        add.getStyleClass().add("primary");
        add.setOnAction(e -> safe(() -> {
            controller.addExtra(name.getText(), amount.getText(), unit.getValue());
            name.clear();
            amount.clear();
        }));
        groceryBox.getStyleClass().add("card");
        ScrollPane scroll = new ScrollPane(groceryBox);
        scroll.setFitToWidth(true);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        VBox root = new VBox(12, header("Calculated automatically from your week plan"), scroll,
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
            dayTotals[d].setText(model.caloriesOn(WeekPlan.DAYS[d]) + " kcal");
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

    private static Label header(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("header");
        return label;
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
