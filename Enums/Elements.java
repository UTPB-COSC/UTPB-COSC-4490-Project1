/*
 * Enumeration Class for the level editor each enumeration corresponds to a number that is used to fill in an array that contains
 * data about how the level should be created.
 */

package Enums;

public enum Elements {
    None(0),
    Platform(1),
    Player(2),
    BasicEnemy(3);

    public final int id;

    Elements(int id) {
        this.id = id;
    }

    public static Elements getElements(int id) {
        for(Elements element : values()) {
            if(element.id == id)
                return element;
        }

        return None;
    }
}
