package be.uantwerpen.sd.project.model;

import java.util.*;

/**
 * The grocery list.
 * OBSERVER (observer): listens to the WeekPlan and recalculates itself.
 */
public class GroceryList implements PlanObserver {
    private final Map<String, GroceryItem> generated = new TreeMap<>(); // from the plan, alphabetical
    private final List<GroceryItem> extras = new ArrayList<>();         // added by hand

    @Override
    public void onPlanChanged(WeekPlan plan) {
        Map<String, GroceryItem> fresh = new TreeMap<>();
        for (Recipe recipe : plan.plannedRecipes()) {
            for (Ingredient i : recipe.getIngredients()) {
                GroceryItem item = new GroceryItem(i.getName(), i.getAmount(), i.getUnit(), false);
                fresh.merge(item.key(), item, GroceryItem::plus);   // same key? -> add up
            }
        }
        // keep the "bought" ticks for items that are still on the list
        for (GroceryItem item : fresh.values()) {
            GroceryItem old = generated.get(item.key());
            if (old != null && old.isBought()) item.toggleBought();
        }
        generated.clear();
        generated.putAll(fresh);
    }

    public void addExtra(String name, int amount, String unit) {
        if (name.isBlank() || amount <= 0) throw new IllegalArgumentException("Enter a name and an amount > 0.");
        extras.add(new GroceryItem(name.trim().toLowerCase(), amount, unit, true));
    }

    /** First the calculated items, then the extras. */
    public List<GroceryItem> items() {
        List<GroceryItem> all = new ArrayList<>(generated.values());
        all.addAll(extras);
        return all;
    }

    /** Total of an ingredient in its base unit (0 if it is not on the list). */
    public int amountOf(String name, String unit) {
        GroceryItem item = generated.get(name + "|" + unit);
        return item == null ? 0 : item.getAmount();
    }
}
