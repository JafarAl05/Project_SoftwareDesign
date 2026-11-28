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
    public void addRecipe(String title, String description, String ingredients, String tags) {
        db.addRecipe(build(new RecipeBuilder(), title, description, ingredients, tags));
    }

    public void editRecipe(Recipe old, String title, String description, String ingredients, String tags) {
        db.updateRecipe(build(new RecipeBuilder().id(old.getId()), title, description, ingredients, tags));
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
        db.addExtra(name, Double.parseDouble(amount.trim().replace(',', '.')), unit);
    }

    /** Form text -> Recipe. Ingredients: one per line, e.g. "200 g pasta". */
    private Recipe build(RecipeBuilder b, String title, String description, String ingredients, String tags) {
        b.title(title).description(description);
        for (String line : ingredients.split("\\R")) {
            if (line.isBlank()) continue;
            String[] p = line.trim().split("\\s+", 3);   // [amount, unit, name]
            if (p.length < 3) throw new IllegalArgumentException("Write ingredients like: 200 g pasta");
            b.ingredient(p[2], Double.parseDouble(p[0].replace(',', '.')), p[1]);
        }
        for (String tag : tags.split(",")) b.tag(tag);
        return b.build();
    }
}
