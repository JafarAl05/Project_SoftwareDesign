package be.uantwerpen.sd.project;

/**
 * Starts the app from IntelliJ: run THIS class (green arrow next to main).
 * Why? If the class with main() itself "extends Application", Java checks whether
 * JavaFX is loaded as a module and otherwise says "JavaFX runtime components are missing".
 * A plain class that does not extend Application skips that check.
 */
public class Launcher {
    public static void main(String[] args) { ViewApp.main(args); }
}
