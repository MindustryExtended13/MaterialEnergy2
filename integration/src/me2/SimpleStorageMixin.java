package me2;

import mindustry.ctype.UnlockableContent;
import mindustry.gen.Building;

/** Class that created to M.E. system, system will be use this to edit content of building */
public interface SimpleStorageMixin extends Mixin {
    /** returns maximum value that can be in the building for id */
    float maximumAccepted(Building building, int id);
    /** returns value that now stored in the building for id */
    float amount(Building building, int id);
    /** removes value from building for id, returns value, that means how many value not removed */
    float extract(Building building, int id, float amount);
    /** receives value from building for id, returns value, that means how many value not received */
    float receive(Building building, int id, float amount);
    /** returns bool that means can build extract any value for id */
    boolean canExtract(Building building, int id);
    /** returns bool that means can build receive any value for id */
    boolean canReceive(Building building, int id);

    /** other version of SimpleStorageMixin that uses UnlockableContent instead of id int (but can use id) */
    interface ContentStorageMixin<T extends UnlockableContent> extends SimpleStorageMixin {
        float maximumAccepted(Building building, T content);
        float amount(Building building, T content);

        float extract(Building building, T content, float amount);
        float receive(Building building, T content, float amount);
        boolean canExtract(Building building, T content);
        boolean canReceive(Building building, T content);
        T toContent(int id);

        @Override
        default float receive(Building building, int id, float amount) {
            return receive(building, toContent(id), amount);
        }

        @Override
        default boolean canExtract(Building building, int id) {
            return canExtract(building, toContent(id));
        }

        @Override
        default float maximumAccepted(Building building, int id) {
            return maximumAccepted(building, toContent(id));
        }

        @Override
        default float amount(Building building, int id) {
            return amount(building, toContent(id));
        }

        @Override
        default float extract(Building building, int id, float amount) {
            return extract(building, toContent(id), amount);
        }

        @Override
        default boolean canReceive(Building building, int id) {
            return canReceive(building, toContent(id));
        }
    }
}