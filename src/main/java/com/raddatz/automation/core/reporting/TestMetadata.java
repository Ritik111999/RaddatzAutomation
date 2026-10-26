package com.raddatz.automation.core.reporting;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares executive QA report metadata on a TestNG test method.
 * Read by reporting listeners to populate ExtentReports and consolidated QA PDFs.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface TestMetadata {

    String module();

    String scenarioId();

    String title();

    TestType testType() default TestType.POSITIVE;

    String expectedResult();

    String preconditions() default "";

    String[] testSteps() default {};

    boolean reportDefectOnFailure() default true;

    DefectSeverity defectSeverity() default DefectSeverity.MEDIUM;

    enum DefectSeverity {
        CRITICAL("Critical"),
        HIGH("High"),
        MEDIUM("Medium"),
        LOW("Low");

        private final String label;

        DefectSeverity(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }
    }

    enum TestType {
        POSITIVE("Positive"),
        NEGATIVE("Negative");

        private final String label;

        TestType(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }
    }
}
