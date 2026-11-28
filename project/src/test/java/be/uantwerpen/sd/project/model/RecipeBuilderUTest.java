package be.uantwerpen.sd.project.model;

import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

class RecipeBuilderUTest {

    @Test
    void buildsCompleteRecipe() {
        String id = "recipe-1";
        Recipe r = new RecipeBuilder().id(id).title(" Pasta ").description("tasty").calories(550)
                .ingredient("pasta", 100, "g").tag(" Vegetarian ").tag(" ").build();
        assertEquals(id, r.getId());
        assertEquals("Pasta", r.getTitle());
        assertEquals("tasty", r.getDescription());
        assertEquals(550, r.getCalories());
        assertEquals(1, r.getIngredients().size());
        assertTrue(r.hasTag("vegetarian"));
        assertEquals(1, r.getTags().size());               // empty tag is ignored
        assertEquals("Pasta", r.toString());
    }

    @Test
    void blankTitleOrNoIngredientsIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new RecipeBuilder().ingredient("x", 1, "g").build());
        assertThrows(IllegalArgumentException.class, () -> new RecipeBuilder().title("Empty").build());
        assertThrows(IllegalArgumentException.class, () -> new RecipeBuilder().ingredient("x", 0, "g"));
        assertThrows(IllegalArgumentException.class, () -> new RecipeBuilder().ingredient("x", 1, "kilo"));
        assertThrows(IllegalArgumentException.class, () -> new RecipeBuilder().calories(-1));
    }

    @Test
    void resultIsUnmodifiable() {
        Recipe r = new RecipeBuilder().title("A").ingredient("x", 1, "g").build();
        assertThrows(UnsupportedOperationException.class, () -> r.getIngredients().clear());
        assertThrows(UnsupportedOperationException.class, () -> r.getTags().add("new"));
    }
}
