package be.uantwerpen.sd.project.command;

import be.uantwerpen.sd.project.model.MealPlannerDB;
import be.uantwerpen.sd.project.model.Recipe;

/** Puts a recipe on a day + meal (recipe == null = remove the meal). */
public class SetMealCommand implements Command {
    private final MealPlannerDB db;   // the "receiver": it does the real work
    private final String day;    // e.g. "Monday"
    private final String slot;   // e.g. "Dinner"
    private final Recipe recipe;
    private Recipe previous;          // what was there before, needed for undo

    public SetMealCommand(MealPlannerDB db, String day, String slot, Recipe recipe) {
        this.db = db;
        this.day = day;
        this.slot = slot;
        this.recipe = recipe;
    }

    @Override
    public void execute() {
        previous = db.getMeal(day, slot);
        db.setMeal(day, slot, recipe);
    }

    @Override
    public void undo() { db.setMeal(day, slot, previous); }
}
