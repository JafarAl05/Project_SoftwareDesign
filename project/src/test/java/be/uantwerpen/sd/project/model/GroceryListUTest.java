package be.uantwerpen.sd.project.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Full unit test of GroceryList (every method and every if branch). */
class GroceryListUTest {
    private WeekPlan plan;
    private GroceryList list;
    private Recipe pasta;

    @BeforeEach
    void setUp() {
        plan = new WeekPlan();
        list = new GroceryList();
        plan.addObserver(list);
        pasta = new RecipeBuilder().title("Pasta").ingredient("pasta", 100, "g").ingredient("onion", 1, "piece").build();
    }

    @Test
    void emptyPlanGivesEmptyList() {
        assertTrue(list.items().isEmpty());
        assertEquals(0, list.amountOf("pasta", "g"));
    }

    @Test
    void sameRecipeTwiceIsSummed() {
        plan.set("Monday", "Dinner", pasta);
        plan.set("Thursday", "Dinner", pasta);
        assertEquals(200, list.amountOf("pasta", "g"));
        assertEquals(2, list.amountOf("onion", "piece"));
    }

    @Test
    void kilogramsAreConvertedToGramsAndNamesNormalised() {
        Recipe big = new RecipeBuilder().title("Lots of pasta").ingredient(" Pasta ", 0.1, "kg").build();
        plan.set("Monday", "Lunch", pasta);
        plan.set("Monday", "Dinner", big);
        assertEquals(200, list.amountOf("pasta", "g"), 1e-9);
    }

    @Test
    void differentBaseUnitsStaySeparateLines() {
        Recipe r = new RecipeBuilder().title("Mix").ingredient("milk", 1, "l").ingredient("milk", 50, "g").build();
        plan.set("Friday", "Breakfast", r);
        assertEquals(1000, list.amountOf("milk", "ml"));
        assertEquals(50, list.amountOf("milk", "g"));
        assertEquals(2, list.items().size());
    }

    @Test
    void removingAMealShrinksTheList() {
        plan.set("Monday", "Dinner", pasta);
        plan.set("Tuesday", "Dinner", pasta);
        plan.set("Tuesday", "Dinner", null);
        assertEquals(100, list.amountOf("pasta", "g"));
        plan.set("Monday", "Dinner", null);
        assertTrue(list.items().isEmpty());
    }

    @Test
    void boughtTickSurvivesRecalculation() {
        plan.set("Monday", "Dinner", pasta);
        list.items().get(1).toggleBought();              // items are alphabetical: [onion, pasta] -> tick "pasta"
        plan.set("Tuesday", "Dinner", pasta);            // list is recalculated
        assertEquals("pasta", list.items().get(1).getName());
        assertTrue(list.items().get(1).isBought());      // "pasta" is still ticked
        assertFalse(list.items().get(0).isBought());     // "onion" was not ticked
    }

    @Test
    void extrasAreKeptAndShownLast() {
        list.addExtra(" Dish soap ", 1, "piece");
        plan.set("Monday", "Dinner", pasta);
        GroceryItem last = list.items().get(list.items().size() - 1);
        assertEquals("dish soap", last.getName());
        assertTrue(last.isManual());
        assertEquals("1 piece dish soap  (extra)", last.toString());
    }

    @Test
    void invalidExtraIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> list.addExtra(" ", 1, "piece"));
        assertThrows(IllegalArgumentException.class, () -> list.addExtra("bread", 0, "piece"));
    }
}
