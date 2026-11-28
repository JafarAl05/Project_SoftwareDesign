package be.uantwerpen.sd.project.model;

/** OBSERVER: whoever implements this is told every time the week plan changes. */
public interface PlanObserver {
    void onPlanChanged(WeekPlan plan);
}
