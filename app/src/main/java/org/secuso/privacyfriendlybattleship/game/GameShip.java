/*
    Copyright 2017 Alexander Müller, Ali Kalsen

    This file is part of Privacy Friendly Battleships.

    Privacy Friendly Battleships is free software: you can redistribute
    it and/or modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    Privacy Friendly Battleships is distributed in the hope that it will
    be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
    of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Foobar.  If not, see http://www.gnu.org/licenses/.
 */

package org.secuso.privacyfriendlybattleship.game;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * This class represents a ship of the battleships game. Each ship
 * consists of two or more cells and can be moved using the methods
 * provided in this class
 *
 * @author Alexander Müller, Ali Kalsen
 */

public class GameShip implements Parcelable{
    private int size;
    private GameCell[] shipsCells;
    private Direction orientation;
    private GameGrid grid;
    private GameShipSet shipSet;

    private int startCellCol;
    private int startCellRow;

    public GameShip(GameGrid grid,
                    GameShipSet shipSet,
                    GameCell shipStart,
                    int shipSize,
                    Direction shipOrientation) {
        if ( !argumentsValid(shipStart, shipSize, shipOrientation, grid.getSize())) {
            throw new IllegalArgumentException("The ship exceeds the limits of the game field");
        }

        this.size = shipSize;
        this.orientation = shipOrientation;
        this.grid = grid;
        this.shipSet = shipSet;
        this.startCellCol = shipStart.getCol();
        this.startCellRow = shipStart.getRow();

        //initialize shipsCells with cells of the ship
       initializeShipsCells();
    }

    static Boolean argumentsValid(GameCell shipStart, int shipSize, Direction orientation, int gridSize) {
        if (    (orientation == Direction.NORTH) &&
                ( (shipStart.getRow() + (shipSize - 1) ) >= gridSize ) ||
                (orientation == Direction.SOUTH) && ( (shipStart.getRow() - (shipSize - 1) ) < 0 ) ||
                (orientation == Direction.EAST) && ( (shipStart.getCol() - (shipSize - 1) ) < 0 ) ||
                (orientation == Direction.WEST) && ( (shipStart.getCol() + (shipSize - 1) ) >= gridSize)) {
            return false;
        }
        return true;
    }

    private void initializeShipsCells() {
        this.shipsCells = new GameCell[this.size];

        if (this.orientation == Direction.NORTH) {
            for (int i = 0; i < this.size; i++) {
                this.shipsCells[i] = this.grid.getCell(this.startCellCol, this.startCellRow + i);
            }
        } else if (this.orientation == Direction.SOUTH) {
            for (int i = 0; i < this.size; i++) {
                this.shipsCells[i] = this.grid.getCell(this.startCellCol, this.startCellRow - i);
            }
        } else if (this.orientation == Direction.EAST) {
            for (int i = 0; i < this.size; i++) {
                this.shipsCells[i] = this.grid.getCell(this.startCellCol - i, this.startCellRow);
            }
        } else if (this.orientation == Direction.WEST) {
            for (int i = 0; i < this.size; i++) {
                this.shipsCells[i] = this.grid.getCell(this.startCellCol + i, this.startCellRow);
            }
        }

        for (int i = 0; i < this.shipsCells.length; i++) {
            this.shipsCells[i].setShip(true);
        }
    }

    public int getSize() {
        return size;
    }

    public Direction getOrientation() {
        return orientation;
    }

    public GameCell getFirstCell() {
        return this.shipsCells[0];
    }

    public GameCell getLastCell() {
        return this.shipsCells[this.size-1];
    }

    public GameCell[] getShipsCells() {
        return shipsCells;
    }

    public boolean isDestroyed() {
        for (int i = 0; i < shipsCells.length; i++) {
            if ( !shipsCells[i].isHit() ) {
                return false;
            }
        }
        return true;
    }

    public boolean containsCell(GameCell cell) {
        for (int i = 0; i < this.shipsCells.length; i++) {
            if ( cell.equals(this.shipsCells[i]) ) return true;
        }
        return false;
    }

    /**
     * Returns true if the ships have at least one cell in between, false if they are adjacent or
     * overlapping. Diagonal cells are considered adjacent.
     * @param other ship to compare to
     * @return true if the given ship is not in contact
     */
    public boolean keepsDistanceTo(GameShip other) {
        for (int i = 0; i < this.size; i++) {
            GameCell[] otherCells = other.getShipsCells();
            for (int j = 0; j < other.getSize(); j++) {
                if ( this.shipsCells[i].isNextTo(otherCells[j]) )
                    return false;
            }
        }
        return true;
    }

    /**
     * Marks all cells of the ship as water except the ones with a ship-collision. Call this method
     * before deleting the Object.
     */
    public void close() {
        for(GameCell cell : this.shipsCells){
            if ( this.shipSet.shipsOnCell(cell) == 1) cell.setShip(false);
        }
    }

    public void moveShip(Direction direction) {
        int col = -1;
        int row = -1;
        switch (direction) {
            case NORTH:
                col = startCellCol;
                row = startCellRow - 1;
                break;
            case EAST:
                col = startCellCol + 1;
                row = startCellRow;
                break;
            case SOUTH:
                col = startCellCol;
                row = startCellRow + 1;
                break;
            case WEST:
                col = startCellCol - 1;
                row = startCellRow;
                break;
        }

        if ( col < 0 || col >= grid.getSize() || row < 0 || row >= grid.getSize() ){
            return;
        }

        if ( !argumentsValid(
                this.grid.getCell(col, row),
                this.size,
                this.orientation,
                this.grid.getSize()) ) {
            return;
        }

        this.close();
        this.startCellCol = col;
        this.startCellRow = row;
        this.initializeShipsCells();
    }

    public void turnShipRight() {
        int middleCellIndex = this.size / 2;
        Direction newOrientation = Direction.NORTH;
        int newStartCol = startCellCol;
        int newStartRow = startCellRow;
        switch (this.orientation) {
            case NORTH:
                newOrientation = Direction.EAST;
                newStartCol = this.startCellCol + middleCellIndex;
                newStartRow = this.startCellRow + middleCellIndex;
                if ( newStartCol < this.size - 1 )
                    newStartCol = this.size - 1;
                if ( newStartCol > this.grid.getSize() - 1 )
                    newStartCol = this.grid.getSize() - 1;
                break;
            case EAST:
                newOrientation = Direction.SOUTH;
                newStartCol = this.startCellCol - middleCellIndex;
                newStartRow = this.startCellRow + middleCellIndex;
                if ( newStartRow < this.size - 1 )
                    newStartRow = this.size - 1;
                if ( newStartRow > this.grid.getSize() - 1 )
                    newStartRow = this.grid.getSize() - 1;
                break;
            case SOUTH:
                newOrientation = Direction.WEST;
                newStartCol = this.startCellCol - middleCellIndex;
                newStartRow = this.startCellRow - middleCellIndex;
                if ( newStartCol < 0 )
                    newStartCol = 0;
                if ( newStartCol + this.size - 1 > this.grid.getSize() - 1)
                    newStartCol = this.grid.getSize() - this.size;
                break;
            case WEST:
                newOrientation = Direction.NORTH;
                newStartCol = this.startCellCol + middleCellIndex;
                newStartRow = this.startCellRow - middleCellIndex;
                if ( newStartRow < 0)
                    newStartRow = 0;
                if ( newStartRow + this.size - 1 > this.grid.getSize() - 1 )
                    newStartRow = this.grid.getSize() - this.size;
                break;
        }

        this.close();
        this.orientation = newOrientation;
        this.startCellCol = newStartCol;
        this.startCellRow = newStartRow;
        this.initializeShipsCells();
    }

    public void turnShipLeft() {
        int middleCellIndex = this.size / 2;
        Direction newOrientation = Direction.NORTH;
        int newStartCol = startCellCol;
        int newStartRow = startCellRow;
        switch (this.orientation) {
            case NORTH:
                newOrientation = Direction.WEST;
                newStartCol = this.startCellCol - middleCellIndex;
                newStartRow = this.startCellRow + middleCellIndex;
                if ( newStartCol < 0 )
                    newStartCol = 0;
                if ( newStartCol + this.size - 1 > this.grid.getSize() - 1)
                    newStartCol = this.grid.getSize() - this.size;
                break;
            case EAST:
                newOrientation = Direction.NORTH;
                newStartCol = this.startCellCol - middleCellIndex;
                newStartRow = this.startCellRow - middleCellIndex;
                if ( newStartRow < 0)
                    newStartRow = 0;
                if ( newStartRow + this.size - 1 > this.grid.getSize() - 1 )
                    newStartRow = this.grid.getSize() - this.size;
                break;
            case SOUTH:
                newOrientation = Direction.EAST;
                newStartCol = this.startCellCol + middleCellIndex;
                newStartRow = this.startCellRow - middleCellIndex;
                if ( newStartCol < this.size - 1 )
                    newStartCol = this.size - 1;
                if ( newStartCol > this.grid.getSize() - 1 )
                    newStartCol = this.grid.getSize() - 1;
                break;
            case WEST:
                newOrientation = Direction.SOUTH;
                newStartCol = this.startCellCol + middleCellIndex;
                newStartRow = this.startCellRow + middleCellIndex;
                if ( newStartRow < this.size - 1 )
                    newStartRow = this.size - 1;
                if ( newStartRow > this.grid.getSize() - 1 )
                    newStartRow = this.grid.getSize() - 1;
                break;
        }

        this.close();
        this.orientation = newOrientation;
        this.startCellCol = newStartCol;
        this.startCellRow = newStartRow;
        this.initializeShipsCells();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.size);
        out.writeString(this.orientation.name());
        out.writeInt(this.shipsCells[0].getCol());
        out.writeInt(this.shipsCells[0].getRow());
    }

    public static final Parcelable.Creator<GameShip> CREATOR = new Parcelable.Creator<GameShip>() {
        public GameShip createFromParcel(Parcel in) {
            return new GameShip(in);
        }

        public GameShip[] newArray(int size) {
            return new GameShip[size];
        }
    };

    private GameShip(Parcel in) {
        this.size = in.readInt();
        this.orientation = Direction.valueOf(in.readString());
        this.startCellCol = in.readInt();
        this.startCellRow = in.readInt();
        //recreateShip has to be called for the ship to be fully recovered.
    }

    void recreateShip(GameGrid grid, GameShipSet set) {
        this.grid = grid;
        this.shipSet = set;

        initializeShipsCells();
    }
}
