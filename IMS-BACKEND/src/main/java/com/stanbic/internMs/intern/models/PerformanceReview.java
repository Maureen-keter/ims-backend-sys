package com.stanbic.internMs.intern.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@Entity
@Data
@Table(name="performance_reviews")
@AllArgsConstructor
@NoArgsConstructor


public class PerformanceReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="intern_id", nullable = false)
    private User intern;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="line_manager_id",nullable = false)
    private User lineManager;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rotation_id")
    private Rotation rotation;
    private LocalDate startDate;
    private LocalDate reviewDate;

    private ReviewPeriod period;
    private LocalDate createdAt=LocalDate.now();

    @Enumerated(EnumType.STRING)
    private PerformanceRating qualityOfWork;
    @Enumerated(EnumType.STRING)
    private PerformanceRating jobKnowledge;
    @Enumerated(EnumType.STRING)
    private PerformanceRating communication;
    @Enumerated(EnumType.STRING)
    private PerformanceRating quantityOfWork;
    @Enumerated(EnumType.STRING)
    private PerformanceRating dependability;
    @Enumerated(EnumType.STRING)
    private PerformanceRating interpersonalSkills;
    @Enumerated(EnumType.STRING)
    private PerformanceRating initiative;
    @Enumerated(EnumType.STRING)
    private PerformanceRating adaptability;
    @Enumerated(EnumType.STRING)
    private PerformanceRating decisionMaking;
    @Enumerated(EnumType.STRING)
    private PerformanceRating selfLeadership;

    @Column(length=1000)
    private String improvementAreas;
    @Column(length=1000)
    private String strengths;
    @Column(length=1000)
    private String summary;

    private Boolean objectivesMet;
    private Boolean trainingNeedsAddressed;
    private Boolean eligibleForNextRotation;
    private Boolean extensionRequired;
    private Integer extensionMonths;
    private LocalDate newCompletionDate;
    private String learnerComments;



//    Helper method to compute avg performance score
    public double calculateAverageScore(){
        PerformanceRating[] ratings= {
                qualityOfWork, jobKnowledge, communication, quantityOfWork,
                dependability, interpersonalSkills, initiative, adaptability,
                decisionMaking, selfLeadership
        };

        int total=0;
        int count=0;

        for(PerformanceRating rating:ratings){
            if (rating !=null){
                total+=rating.getScore();
                count++;
            }
        }
        return count > 0? (double) total/count: 0.0;
    }

    public enum ReviewPeriod{
        MONTH_1,MONTH_2,MONTH_3,MONTH_4,MONTH_5,MONTH_6
    }
}
