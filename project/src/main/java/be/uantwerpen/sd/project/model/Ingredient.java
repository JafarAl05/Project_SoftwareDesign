package be.uantwerpen.sd.project.model;

/** One line of a recipe, e.g. 100 g pasta. */
public class Ingredient {
    public static final String[] UNITS = {"g", "ml", "piece"};   // the choices in the unit dropdown

    private final String name;
    private final int amount;
    private final String unit;

    public Ingredient(String name, int amount, String unit) {
        name = name.trim().toLowerCase();               // "Pasta " and "pasta" are the same
        if (name.isEmpty() || amount <= 0) throw new IllegalArgumentException("Invalid ingredient: " + name);
        if (!java.util.List.of(UNITS).contains(unit)) throw new IllegalArgumentException("Unknown unit: " + unit);
        this.name = name;
        this.amount = amount;
        this.unit = unit;
    }

    public String getName() { return name; }
    public int getAmount() { return amount; }
    public String getUnit() { return unit; }

    @Override
    public String toString() { return amount + " " + unit + " " + name; }
}
