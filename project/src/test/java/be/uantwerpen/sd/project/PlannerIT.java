package be.uantwerpen.sd.project;

import be.uantwerpen.sd.project.command.CommandHistory;
import be.uantwerpen.sd.project.controller.PlannerController;
import be.uantwerpen.sd.project.model.MealPlannerDB;
import be.uantwerpen.sd.project.model.Recipe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Integration test: controller + singleton model + commands + observer together. */
class PlannerIT {
    private MealPlannerDB db;
    private PlannerController controller;
    private final List<PropertyChangeEvent> events = new ArrayList<>();

    @BeforeEach
    void setUp() {
        db = MealPlannerDB.getInstance();
        db.reset();
        controller = new PlannerController(db, new CommandHistory());
        db.addPropertyChangeListener(events::add);   // plays the "View"
    }

    @Test
    void singletonIsAlwaysTheSameObject() {
        assertSame(db, MealPlannerDB.getInstance());
    }

    @Test
    void planTwiceSumsAndUndoRedoUpdatesTheList() {
        controller.addRecipe("Pasta", "", "100 g pasta\n1 piece onion", "quick");
        Recipe pasta = db.getRecipes().get(0);

        controller.assignMeal("Monday", "Dinner", pasta);
        controller.assignMeal("Thursday", "Dinner", pasta);
        assertEquals(200, db.amountOf("pasta", "g"));

        controller.undo();
        assertEquals(100, db.amountOf("pasta", "g"));
        controller.redo();
        assertEquals(200, db.amountOf("pasta", "g"));

        controller.clearMeal("Monday", "Dinner");
        assertNull(db.getMeal("Monday", "Dinner"));
        assertEquals(100, db.amountOf("pasta", "g"));
        assertTrue(events.stream().anyMatch(e -> e.getPropertyName().equals("plan")));
    }

    @Test
    void editingAndRemovingARecipeUpdatesPlanAndList() {
        controller.addRecipe("Pasta", "", "100 g pasta", "");
        Recipe pasta = db.getRecipes().get(0);
        controller.assignMeal("Monday", "Dinner", pasta);

        controller.editRecipe(pasta, "Pasta XL", "", "0.3 kg pasta", "");
        assertEquals("Pasta XL", db.getMeal("Monday", "Dinner").getTitle());
        assertEquals(300, db.amountOf("pasta", "g"), 1e-9);

        controller.removeRecipe(db.getRecipes().get(0));
        assertTrue(db.getRecipes().isEmpty());
        assertNull(db.getMeal("Monday", "Dinner"));
        assertTrue(db.getGroceries().isEmpty());
    }

    @Test
    void groceryTicksAndExtrasThroughController() {
        controller.addExtra("bread", "1", "piece");
        controller.toggleBought(db.getGroceries().get(0));
        assertTrue(db.getGroceries().get(0).isBought());
        assertThrows(IllegalArgumentException.class, () -> controller.addExtra("milk", "lots", "l"));
        assertThrows(IllegalArgumentException.class, () -> controller.addRecipe("Wrong", "", "pasta", ""));
        assertThrows(IllegalArgumentException.class, () -> controller.addRecipe("Wrong", "", "100 kilo pasta", ""));
    }
}
