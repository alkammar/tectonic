package com.morkim.tectonic;

import java.util.ArrayList;
import java.util.List;

public class RequiredInputs {

    private UseCase useCase;
    private List<Integer> inputs = new ArrayList<>();

    RequiredInputs(UseCase useCase) {
        this.useCase = useCase;
    }

    /**
     * Adds the {@code code} to a list of required inputs if the condition is true
     *
     * @param condition The condition to validate that the input is required
     * @param code The code of the required input
     * @return The required input object for chaining
     */
    public RequiredInputs check(boolean condition, int code) {

        if (condition) inputs.add(code);

        return this;
    }

    /**
     * Checks that no inputs are required. If inputs are required then a request for inputs to
     * listeners is fired
     *
     * @return True if no inputs are required
     */
    public boolean validate() {
        boolean valid = inputs.size() == 0;
        if (!valid) useCase.requestAction(UseCase.NO_ACTOR, inputs.toArray(new Integer[0]));
        return valid;
    }
}
