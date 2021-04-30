package ca.uwaterloo.cs.jgrok.env;


public interface Loader {
    public void load(Env env, String fileName) throws LoadingException;
}
