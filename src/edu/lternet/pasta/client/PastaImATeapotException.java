package edu.lternet.pasta.client;

public class PastaImATeapotException extends Exception {

    private static final long serialVersionUID = 1L;

    /*
     * Constructors
     */

    /**
     * Pasta I'm A Teapot Exception.
     */
    public PastaImATeapotException() {

    }

    /**
     * Pasta I'm A Teapot Exception.
     *
     * @param gripe
     *          The cause of the exception in natural language text as a String
     *          object.
     */
    public PastaImATeapotException(String gripe) {
        super(gripe);
    }

}