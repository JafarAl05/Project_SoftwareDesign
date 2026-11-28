package be.uantwerpen.sd.project.controller;

import be.uantwerpen.sd.project.command.CommandHistory;
import be.uantwerpen.sd.project.command.SetMealCommand;
import be.uantwerpen.sd.project.model.*;

/** The CONTROLLER of MVC: turns clicks from the View into requests to the model. */
public class PlannerController {
    private final MealPlannerDB db;
    private final CommandHistory history;

    public PlannerController(MealPlannerDB db, CommandHistory history) {
        this.db = db;
        this.history = history;
    }

    // ---------- recipes ----------
    // ingredients = the rows of the form: each row is {amount, unit, name}
    public void addRecipe(String title, String description, String calories, String[][] ingredients, String tags) {
        db.addRecipe(build(new RecipeBuilder(), title, description, calories, ingredients, tags));
    }

    public void editRecipe(Recipe old, String title, String description, String calories, String[][] ingredients, String tags) {
        db.updateRecipe(build(new RecipeBuilder().id(old.getId()), title, description, calories, ingredients, tags));
        history.clear();   // old undo steps know the old recipe, so clear them
    }

    public void removeRecipe(Recipe recipe) {
        db.removeRecipe(recipe);
        history.clear();
    }

    // ---------- week plan (via COMMAND, so it can be undone) ----------
    public void assignMeal(String day, String slot, Recipe recipe) {
        history.run(new SetMealCommand(db, day, slot, recipe));
    }

    public void clearMeal(String day, String slot) { assignMeal(day, slot, null); }

    public void undo() { history.undo(); }

    public void redo() { history.redo(); }

    // ---------- groceries ----------
    public void toggleBought(GroceryItem item) { db.toggleBought(item); }

    public void addExtra(String name, String amount, String unit) {
        db.addExtra(name, toInt(amount, "The amount"), unit);
    }

    /** Form fields -> Recipe. Empty rows are skipped. */
    private Recipe build(RecipeBuilder b, String title, String description, String calories, String[][] ingredients, String tags) {
        b.title(title).description(description).calories(calories.isBlank() ? 0 : toInt(calories, "Calories"));
        for (String[] row : ingredients) {
            if (row[2].isBlank()) continue;                       // row without ingredient name
            b.ingredient(row[2], toInt(row[0], "The amount of " + row[2]), row[1]);
        }
        for (String tag : tags.split(",")) b.tag(tag);
        return b.build();
    }

    /** "200" -> 200, with a clear message for "abc" or "1.5". */
    private int toInt(String text, String what) {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(what + " must be a whole number.");
        }
    }
}
