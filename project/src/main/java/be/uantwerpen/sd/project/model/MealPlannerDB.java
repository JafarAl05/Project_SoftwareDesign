package be.uantwerpen.sd.project.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

/**
 * The MODEL of MVC: the one place that holds all data.
 * SINGLETON (thread-safe): the same instance everywhere via getInstance().
 */
public final class MealPlannerDB {
    private static volatile MealPlannerDB instance;   // volatile: every thread sees the new value immediately

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private final List<Recipe> recipes = new ArrayList<>();
    private WeekPlan plan;
    private GroceryList groceries;

    private MealPlannerDB() { reset(); }               // private: nobody else can call "new"

    public static MealPlannerDB getInstance() {
        if (instance == null) {                         // 1st check: fast, without a lock
            synchronized (MealPlannerDB.class) {        // lock: only one thread at a time
                if (instance == null) {                 // 2nd check: maybe another thread was faster
                    instance = new MealPlannerDB();
                }
            }
        }
        return instance;
    }

    /** Clears everything. Useful for tests, because the singleton stays alive. */
    public void reset() {
        recipes.clear();
        plan = new WeekPlan();
        groceries = new GroceryList();
        plan.addObserver(groceries);                    // OBSERVER: the list follows the plan
        pcs.firePropertyChange("reset", null, null);
    }

    // ---------- recipes ----------
    public List<Recipe> getRecipes() { return List.copyOf(recipes); }

    public void addRecipe(Recipe r) {
        recipes.add(r);
        pcs.firePropertyChange("recipes", null, r);
    }

    public void updateRecipe(Recipe updated) {
        recipes.replaceAll(r -> r.getId().equals(updated.getId()) ? updated : r);
        plan.replaceEverywhere(updated.getId(), updated);  // use the new version in the plan too
        pcs.firePropertyChange("recipes", null, updated);
    }

    public void removeRecipe(Recipe r) {
        recipes.removeIf(x -> x.getId().equals(r.getId()));
        plan.replaceEverywhere(r.getId(), null);           // remove from the plan -> list shrinks too
        pcs.firePropertyChange("recipes", r, null);
    }

    // ---------- week plan ----------
    public Recipe getMeal(String day, String slot) { return plan.get(day, slot); }

    public void setMeal(String day, String slot, Recipe recipe) {
        plan.set(day, slot, recipe);                    // -> WeekPlan notifies GroceryList
        pcs.firePropertyChange("plan", null, recipe);   // -> MVC: the View redraws
    }

    public int caloriesOn(String day) { return plan.caloriesOn(day); }

    // ---------- groceries ----------
    public List<GroceryItem> getGroceries() { return groceries.items(); }

    public int amountOf(String name, String unit) { return groceries.amountOf(name, unit); }

    public void toggleBought(GroceryItem item) {
        item.toggleBought();
        pcs.firePropertyChange("groceries", null, item);
    }

    public void addExtra(String name, int amount, String unit) {
        groceries.addExtra(name, amount, unit);
        pcs.firePropertyChange("groceries", null, name);
    }

    // ---------- MVC: the View subscribes here ----------
    public void addPropertyChangeListener(PropertyChangeListener l) { pcs.addPropertyChangeListener(l); }

    public void removePropertyChangeListener(PropertyChangeListener l) { pcs.removePropertyChangeListener(l); }
}
