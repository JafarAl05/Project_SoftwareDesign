package be.uantwerpen.sd.project.model;

/** One line on the grocery list. */
public class GroceryItem {
    private final String name;
    private final int amount;
    private final String unit;      // "g", "ml" or "piece"
    private final boolean manual;   // true = added by hand (extra)
    private boolean bought;

    public GroceryItem(String name, int amount, String unit, boolean manual) {
        this.name = name;
        this.amount = amount;
        this.unit = unit;
        this.manual = manual;
    }

    /** "pasta|g": same key = same line on the list. */
    public String key() { return name + "|" + unit; }

    /** 100 g pasta + 100 g pasta = 200 g pasta */
    public GroceryItem plus(GroceryItem other) { return new GroceryItem(name, amount + other.amount, unit, manual); }

    public void toggleBought() { bought = !bought; }

    public String getName() { return name; }
    public int getAmount() { return amount; }
    public String getUnit() { return unit; }
    public boolean isManual() { return manual; }
    public boolean isBought() { return bought; }

    @Override
    public String toString() {
        return amount + " " + unit + " " + name + (manual ? "  (extra)" : "");
    }
}
