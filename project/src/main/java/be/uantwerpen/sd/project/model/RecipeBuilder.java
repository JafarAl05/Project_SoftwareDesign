package be.uantwerpen.sd.project.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * BUILDER: builds a Recipe step by step.
 *   new RecipeBuilder().title("Pasta").calories(550).ingredient("pasta", 100, "g").tag("quick").build();
 */
public class RecipeBuilder {
    private String id = UUID.randomUUID().toString();
    private String title = "";
    private String description = "";
    private int calories;
    private final List<Ingredient> ingredients = new ArrayList<>();
    private final List<String> tags = new ArrayList<>();

    /** Only needed when editing: the recipe keeps its old id. */
    public RecipeBuilder id(String id) { this.id = id; return this; }

    public RecipeBuilder title(String title) { this.title = title.trim(); return this; }

    public RecipeBuilder description(String description) { this.description = description.trim(); return this; }

    public RecipeBuilder calories(int calories) {
        if (calories < 0) throw new IllegalArgumentException("Calories cannot be negative.");
        this.calories = calories;
        return this;
    }

    public RecipeBuilder ingredient(String name, int amount, String unit) {
        ingredients.add(new Ingredient(name, amount, unit));
        return this;
    }

    public RecipeBuilder tag(String tag) {
        String t = tag.trim().toLowerCase();
        if (!t.isEmpty() && !tags.contains(t)) tags.add(t);
        return this;
    }

    /** Checks everything and only then returns the recipe. */
    public Recipe build() {
        if (title.isEmpty()) throw new IllegalArgumentException("A recipe needs a title.");
        if (ingredients.isEmpty()) throw new IllegalArgumentException("A recipe needs at least one ingredient.");
        return new Recipe(id, title, description, calories, List.copyOf(ingredients), List.copyOf(tags));
    }
}
