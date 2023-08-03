package me2;

/** ME2 mixin base that uses in ME2 mixins */
public interface Mixin {
    /** Mixin init method that used to something but this method calls than ME2 mixin init (ClientLoadEvent) */
    void init();

    /** Name of mixin that will be displayed */
    default String name() {
        return getClass().getSimpleName();
    }
}