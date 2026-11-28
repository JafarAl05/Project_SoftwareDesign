package be.uantwerpen.sd.project.model;

/** One line of a recipe, e.g. 100 g pasta. kg and l are converted to g and ml right away. */
public class Ingredient {
    private final String name;
    private final double amount;
    private final String unit;   // always "g", "ml" or "piece"

    public Ingredient(String name, double amount, String unit) {
        name = name.trim().toLowerCase();               // "Pasta " and "pasta" are the same
        unit = unit.trim().toLowerCase();
        if (name.isEmpty() || amount <= 0) throw new IllegalArgumentException("Invalid ingredient: " + name);
        if (unit.equals("kg")) { amount *= 1000; unit = "g"; }    // 0.2 kg -> 200 g
        if (unit.equals("l"))  { amount *= 1000; unit = "ml"; }   // 0.5 l  -> 500 ml
        if (!unit.equals("g") && !unit.equals("ml") && !unit.equals("piece"))
            throw new IllegalArgumentException("Unknown unit: " + unit + " (use g, kg, ml, l or piece)");
        this.name = name;
        this.amount = amount;
        this.unit = unit;
    }

    public String getName() { return name; }
    public double getAmount() { return amount; }
    public String getUnit() { return unit; }

    @Override
    public String toString() { return format(amount) + " " + unit + " " + name; }

    /** 200.0 -> "200", 1.5 -> "1.5" */
    public static String format(double d) { return d == (long) d ? String.valueOf((long) d) : String.valueOf(d); }
}
