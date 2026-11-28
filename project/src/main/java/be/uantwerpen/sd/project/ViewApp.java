package be.uantwerpen.sd.project;

import be.uantwerpen.sd.project.command.CommandHistory;
import be.uantwerpen.sd.project.controller.PlannerController;
import be.uantwerpen.sd.project.model.MealPlannerDB;
import be.uantwerpen.sd.project.model.RecipeBuilder;
import be.uantwerpen.sd.project.view.MainView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/** Starts the app and connects Model, View and Controller. */
public class ViewApp extends Application {

    public static void main(String[] args) { launch(args); }

    @Override
    public void start(Stage stage) {
        MealPlannerDB model = MealPlannerDB.getInstance();                               // M
        PlannerController controller = new PlannerController(model, new CommandHistory()); // C
        addDemoRecipes(model);
        MainView view = new MainView(model, controller);                                  // V

        stage.setTitle("Smart Meal Planner");
        Scene scene = new Scene(view, 1300, 600);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());   // the looks
        stage.setScene(scene);
        stage.show();
    }

    /** A few recipes so you see something right away (built with the BUILDER). */
    private static void addDemoRecipes(MealPlannerDB db) {
        db.addRecipe(new RecipeBuilder().title("Pasta pomodoro").description("Weeknight classic").calories(550)
                .ingredient("pasta", 100, "g").ingredient("passata", 200, "ml").ingredient("onion", 1, "piece")
                .tag("vegetarian").tag("quick").build());
        db.addRecipe(new RecipeBuilder().title("Porridge").description("Warm breakfast").calories(300)
                .ingredient("oats", 60, "g").ingredient("milk", 250, "ml").tag("vegetarian").tag("budget").build());
        db.addRecipe(new RecipeBuilder().title("Omelette").description("Quick and simple").calories(350)
                .ingredient("egg", 3, "piece").ingredient("cheese", 20, "g").tag("quick").build());
    }
}
