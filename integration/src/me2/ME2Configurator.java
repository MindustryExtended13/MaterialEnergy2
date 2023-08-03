package me2;

import arc.struct.Seq;

/** Class that used to storage, get and register Mixins */
@SuppressWarnings("unchecked")
public class ME2Configurator {
    private static final Seq<Mixin> mixins = new Seq<>();

    /** Gets mixin that have the same class as selected and returns this mixin */
    public static<T extends Mixin> T getMixinByClass(Class<T> cl) {
        return (T) mixins.find(mixin -> mixin.getClass().equals(cl));
    }

    /** Register mixin to the list but if mixin is not null */
    public static void register(Mixin mixin) {
        if(mixin != null) mixins.add(mixin);
    }

    /**
     * Returns list of mixins that extends class that selected in type-class.
     * Output array will be type type-class.
     * @param type type-class
     */
    public static<T extends Mixin> Seq<T> select(Class<T> type) {
        return mixins
                .select(mixin -> {
                    Class<?>[] intf = mixin.getClass().getInterfaces();
                    for(Class<?> cl : intf) {
                        if(cl == type) {
                            return true;
                        }
                    }

                    return mixin.getClass() == type;
                })
                .map(mixin -> (T) mixin);
    }

    /**
     * WARNING: This method created to read values. Writing to array can cause bugs and crashes
     * @return list of mixins that will be registered.
     */
    public static Seq<Mixin> mixins() {
        return mixins;
    }
}