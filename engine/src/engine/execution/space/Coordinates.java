package engine.execution.space;

import engine.execution.instance.entity.EntityInstance;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class Coordinates {
    private final EnumSet<Direction> checkedDirections;
    private final int x;
    private final int y;
    private EntityInstance entityInstance;
    private final List<Coordinates> nearCoordinates;

    public Coordinates(int x, int y) {
        this.x = x;
        this.y = y;
        checkedDirections = EnumSet.noneOf(Direction.class);
        nearCoordinates = new ArrayList<>();
    }

    public Integer getX() {
        return x;
    }

    public Integer getY() {
        return y;
    }

    public Direction drawDirection() {
        Direction direction = Direction.randomDirection();

        if (checkIfValid(direction)) {
            return direction;
        }

        return null;
    }

    private boolean checkIfValid(Direction direction) {
        if (!checkedDirections.contains(direction)) {
            checkedDirections.add(direction);
            return true;
        }
        return false;
    }

    public boolean didCheckAllDirections() {
        return checkedDirections.size() == Direction.values().length;
    }

    public void resetCheckedDirections() {
        checkedDirections.clear();
    }

    public void addNearCoordinate(Coordinates coordinates) {
        nearCoordinates.add(coordinates);
    }

    public List<Coordinates> getNearCoordinates() {
        return nearCoordinates;
    }

    public EntityInstance getEntityInstance() {
        return entityInstance;
    }

    public void setEntityInstance(EntityInstance entityInstance) {
        this.entityInstance = entityInstance;
    }
}
