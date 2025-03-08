/* 
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details. 
 */
package movement;

import core.Coord;
import core.Settings;
import static movement.MovementModel.rng;

/**
 * Random waypoint movement model. Creates zig-zag paths within the simulation
 * area.
 */
public class RandomWaypointCoba extends MovementModel {

    public static final String MOVE_AREA = "moveArea";
    /**
     * how many waypoints should there be per path
     */
    private static final int PATH_LENGTH = 1;
    private Coord lastWaypoint;

    public RandomWaypointCoba(Settings settings) {
        super(settings);
    }

    protected RandomWaypointCoba(RandomWaypointCoba rwp) {
        super(rwp);
    }

    /**
     * Returns a possible (random) placement for a host
     *
     * @return Random position on the map
     */
    @Override
    public Coord getInitialLocation() {
        assert rng != null : "MovementModel not initialized!";
        Coord c = randomCoord(moveArea);

        this.lastWaypoint = c;
        return c;
    }

    @Override
    public Path getPath() {
        Path p;
        p = new Path(generateSpeed());
        p.addWaypoint(lastWaypoint.clone());
        Coord c = lastWaypoint;

        for (int i = 0; i < PATH_LENGTH; i++) {
            c = randomCoord(moveArea);
            p.addWaypoint(c);
        }

        this.lastWaypoint = c;
        return p;
    }

    @Override
    public RandomWaypointCoba replicate() {
        return new RandomWaypointCoba(this);
    }

    protected Coord randomCoord() {
        return new Coord(rng.nextDouble() * getMaxX(),
                rng.nextDouble() * getMaxY());
    }

    protected Coord randomCoord(int area) {
        double minX, maxX, minY, maxY;

        switch (area) {
            case 1:
                minX = 0;
                maxX = 333;
                minY = 0;
                maxY = 333;
                break;
            case 2:
                minX = 333;
                maxX = 666;
                minY = 0;
                maxY = 333;
                break;
            case 3:
                minX = 666;
                maxX = 1000;
                minY = 0;
                maxY = 333;
                break;
            case 4:
                minX = 0;
                maxX = 333;
                minY = 333;
                maxY = 666;
                break;
            case 5:
                minX = 666;
                maxX = 1000;
                minY = 333;
                maxY = 666;
                break;
            case 6:
                minX = 0;
                maxX = 333;
                minY = 666;
                maxY = 1000;
                break;
            case 7:
                minX = 333;
                maxX = 666;
                minY = 666;
                maxY = 1000;
                break;
            case 8:
                minX = 666;
                maxX = 1000;
                minY = 666;
                maxY = 1000;
                break;
            case 9:
                minX = 333;
                maxX = 666;  // X tetap di tengah
                minY = 333;
                maxY = 1000;  // Y hanya dalam batas vertikal
                break;
            default:
                throw new IllegalArgumentException("Area harus antara 1-4 atau 6-9 (area 5 tidak bisa dipilih)");
        }

        double x = minX + rng.nextDouble() * (maxX - minX);
        double y = minY + rng.nextDouble() * (maxY - minY);

        return new Coord(x, y);
    }

}
