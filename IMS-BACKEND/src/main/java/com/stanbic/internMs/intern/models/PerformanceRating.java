package com.stanbic.internMs.intern.models;

    import lombok.Getter;

@Getter
public enum PerformanceRating {
    SETTING_THE_EXAMPLE(3,"Setting the Example"),
    RIGHT_ON_TRACK(2,"Right on Track"),
    MAKING_PROGRESS(1,"Making Progress"),
    TIME_TO_STEP_UP(0,"Time to Step up");

    private final int score;
    private final String label;

    PerformanceRating(int score, String label){
        this.score=score;
        this.label=label;
    }

    public static PerformanceRating fromScore(int score){
        for(PerformanceRating rating:values()){
            if(rating.getScore()==score){
                return rating;
            }
        }
        throw new IllegalArgumentException("Invalid Performance score: " + score);
    }

}
