package org.oleg_w570.marksman_game;

public record Action(Type type, String info) {
    public static enum Type {
        New,
        State,
        WantToStart,
        Update,
        Shoot,
        WantToPause,
        Winner,
        Reset,
        Remove,
        Stop,
    }
}
