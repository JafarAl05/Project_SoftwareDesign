package be.uantwerpen.sd.project.model;

import java.util.List;

/** A recipe. Create it with the RecipeBuilder. It does not change after it is built. */
public class Recipe {
    private final String id;
    private final String title;
    private final String description;
    private final List<Ingredient> ingredients;
    private final List<String> tags;

    // not "public": only the RecipeBuilder (same package) creates recipes
    Recipe(String id, String title, String description, List<Ingredient> ingredients, List<String> tags) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.ingredients = ingredients;
        this.tags = tags;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public List<Ingredient> getIngredients() { return ingredients; }
    public List<String> getTags() { return tags; }

    public boolean hasTag(String tag) { return tags.contains(tag.toLowerCase()); }

    @Override
    public String toString() { return title; }   // this is how ListView and ComboBox show the title
}
