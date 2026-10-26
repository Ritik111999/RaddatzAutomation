package com.raddatz.automation.pages;

/**
 * Read-only DOM contract observed from a visible form control.  Values are
 * intentionally captured as attributes rather than inferred from a failed
 * submission, so observation cannot create an incomplete business record.
 */
public record ObservedFieldConstraint(
        String control,
        String type,
        boolean required,
        String min,
        String max,
        String maxLength,
        String pattern,
        String ariaRequired) {
}
