package org.oleg_w570.marksman_game;

import java.util.ArrayList;
import java.util.List;

public class GameInfo {
    public final CircleInfo bigCircle;
    public final CircleInfo smallCircle;
    public final List<PlayerInfo> allPlayers = new ArrayList<>();

    public GameInfo(final double height) {
        bigCircle = new CircleInfo(493.0, 0.5 * height, 50.0, 0.75);
        smallCircle = new CircleInfo(599.0, 0.5 * height, 25.0, 1.5);
    }
}
