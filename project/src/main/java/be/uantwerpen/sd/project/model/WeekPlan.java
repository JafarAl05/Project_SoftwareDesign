package be.uantwerpen.sd.project.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The week plan: 7 days x 4 meals.
 * OBSERVER (subject): on every change it notifies its PlanObservers.
 */
public class WeekPlan {
    public static final String[] DAYS = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
    public static final String[] SLOTS = {"Breakfast", "Lunch", "Dinner", "Snack"};

    private final Map<String, Recipe> meals = new HashMap<>();   // key e.g. "Monday Dinner"
    private final List<PlanObserver> observers = new ArrayList<>();

    public Recipe get(String day, String slot) { return meals.get(day + " " + slot); }

    /** Puts a recipe on a meal. recipe == null means: remove the meal. */
    public void set(String day, String slot, Recipe recipe) {
        if (recipe == null) meals.remove(day + " " + slot);
        else meals.put(day + " " + slot, recipe);
        notifyObservers();
    }

    /** All planned recipes. A list, so pasta planned twice counts twice. */
    public List<Recipe> plannedRecipes() { return new ArrayList<>(meals.values()); }

    /** Total kcal of all meals planned on one day. */
    public int caloriesOn(String day) {
        int total = 0;
        for (String slot : SLOTS) {
            Recipe r = get(day, slot);
            if (r != null) total += r.getCalories();
        }
        return total;
    }

    /** After editing a recipe: put the new version everywhere (null = remove everywhere). */
    public void replaceEverywhere(String id, Recipe replacement) {
        for (String key : new ArrayList<>(meals.keySet())) {
            if (!meals.get(key).getId().equals(id)) continue;
            if (replacement == null) meals.remove(key);
            else meals.put(key, replacement);
        }
        notifyObservers();
    }

    public void addObserver(PlanObserver o) { observers.add(o); }

    public void removeObserver(PlanObserver o) { observers.remove(o); }

    private void notifyObservers() {
        for (PlanObserver o : observers) o.onPlanChanged(this);
    }
}
