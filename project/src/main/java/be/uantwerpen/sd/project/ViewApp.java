package be.uantwerpen.sd.project;

import be.uantwerpen.sd.project.command.CommandHistory;
import be.uantwerpen.sd.project.controller.PlannerController;
import be.uantwerpen.sd.project.model.MealPlannerDB;
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
        addDemoRecipes(controller);
        MainView view = new MainView(model, controller);                                  // V

        stage.setTitle("Smart Meal Planner");
        stage.setScene(new Scene(view, 1300, 560));
        stage.show();
    }

    /** A few recipes so you see something right away. */
    private static void addDemoRecipes(PlannerController c) {
        c.addRecipe("Pasta pomodoro", "Weeknight classic", "100 g pasta\n200 ml passata\n1 piece onion", "vegetarian, quick");
        c.addRecipe("Porridge", "Warm breakfast", "60 g oats\n0.25 l milk", "vegetarian, budget");
        c.addRecipe("Omelette", "Quick and simple", "3 piece egg\n20 g cheese", "quick");
    }
}
