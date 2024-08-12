package engine.execution.space;

import dto.definition.WorldInfoDTO;
import engine.execution.instance.entity.EntityInstance;

import java.util.Random;

public class Grid {
    private final Coordinates[][] gridSpace;
    private final int xSize;
    private final int ySize;
    private final Random rand;

    public Grid(int x, int y) {
        gridSpace = new Coordinates[x][y];
        xSize = x;
        ySize = y;
        initiateCoordinates();
        rand = new Random();
    }

    private void initiateCoordinates() {
        for (int x = 0; x < xSize; x++) {
            for (int y = 0; y < ySize; y++) {
                gridSpace[x][y] = new Coordinates(x, y);
            }
        }
        for (int x = 0; x < xSize; x++) {
            for (int y = 0; y < ySize; y++) {
                setNearCoordinates(gridSpace[x][y]);
            }
        }

    }

    private void setNearCoordinates(Coordinates coordinates) {
        int x = coordinates.getX();
        int y = coordinates.getY();

        int[][] relativePositions = {
                {-1, -1}, {-1, 0}, {-1, 1},
                {0, -1}, {0, 1},
                {1, -1}, {1, 0}, {1, 1}
        };

        for (int[] relativePos : relativePositions) {
            int newX = (x + relativePos[0] + xSize) % xSize;
            int newY = (y + relativePos[1] + ySize) % ySize;

            coordinates.addNearCoordinate(gridSpace[newX][newY]);
        }
    }

    public boolean move(Coordinates coordinates, Direction direction) {
        boolean didMove = false;
        int x = coordinates.getX();
        int y = coordinates.getY();

        switch (direction) {
            case LEFT:
                if (y == 0) {
                    y = ySize - 1;
                } else{
                    y -= 1;
                }
                break;
            case RIGHT:
                if (y == ySize - 1) {
                    y = 0;
                } else {
                    y += 1;
                }
                break;
            case UP:
                if (x == 0) {
                    x = xSize - 1;
                } else {
                    x -= 1;
                }
                break;
            case DOWN:
                if (x == xSize - 1) {
                    x = 0;
                } else {
                    x += 1;
                }
                break;
        }

        if (gridSpace[x][y].getEntityInstance() == null) {
            gridSpace[x][y].setEntityInstance(coordinates.getEntityInstance());
            coordinates.getEntityInstance().setCoordinates(gridSpace[x][y]);
            coordinates.setEntityInstance(null);
            didMove = true;
        }

        return didMove;
    }

    public void placeEntityInstance(EntityInstance entityInstance) {
        int x, y;

        while (true) {
            x = rand.nextInt(xSize);
            y = rand.nextInt(ySize);
            if (gridSpace[x][y].getEntityInstance() == null) {
                gridSpace[x][y].setEntityInstance(entityInstance);
                entityInstance.setCoordinates(gridSpace[x][y]);
                break;
            }
        }
    }

    public Integer getSize() {
        return xSize * ySize;
    }

    public void setGridInfo(WorldInfoDTO worldInfoDTO){
        worldInfoDTO.setGridSize(xSize, ySize);
    }
}
