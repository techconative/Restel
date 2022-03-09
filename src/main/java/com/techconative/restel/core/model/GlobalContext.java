package com.techconative.restel.core.model;


/**
 * Meant to store the values that are applicable across the test execution.
 * <p>
 * There cannot be more than one {@link GlobalContext}
 *
 * @author kannanr
 */
public class GlobalContext extends AbstractContext {
    private static GlobalContext own;

    private GlobalContext() {
        // getInstance is the only way to initialize
        super(null);
    }

    /**
     * Gets the {@link GlobalContext} instance. If not available creates one
     * and returns
     *
     * @return Returns the {@link GlobalContext} instance.
     */
    public static GlobalContext getInstance() {
        if (own == null) {
            own = new GlobalContext();
        }
        return own;
    }

}
