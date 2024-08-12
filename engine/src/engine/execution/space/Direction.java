package engine.execution.space;

import java.util.Random;

public enum Direction {
    LEFT, RIGHT, UP, DOWN;

    private static final Random random = new Random();

    public static Direction randomDirection()  {
        Direction[] directions = values();
        return directions[random.nextInt(directions.length)];
    }
}
