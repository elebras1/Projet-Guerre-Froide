package com.populaire.projetguerrefroide.protocol.catalog;

import com.github.tommyettinger.ds.IntIntMap;
import com.github.tommyettinger.ds.IntLongMap;

public final class ProvinceTable {
    public static final long NO_POSITION = Long.MIN_VALUE;
    private final NamedTable names;
    private final int[] regionIndexes;
    private final int[] terrainIndexes;
    private final int[] resourceGoodIndexes;
    private final int[] positionsX;
    private final int[] positionsY;
    private final int[] borderStarts;
    private final int[] borderEnds;
    private final int[] adjacencyStarts;
    private final int[] adjacencies;
    private final IntIntMap indexByColor;
    private final IntLongMap buildingPositions;
    private final int buildingTypeCount;

    public ProvinceTable(NamedTable names, int[] regionIndexes, int[] terrainIndexes, int[] resourceGoodIndexes, int[] positionsX, int[] positionsY, int[] borderStarts, int[] borderEnds, int[] adjacencyStarts, int[] adjacencies, IntLongMap buildingPositions, int buildingTypeCount) {
        this.names = names;
        this.regionIndexes = regionIndexes;
        this.terrainIndexes = terrainIndexes;
        this.resourceGoodIndexes = resourceGoodIndexes;
        this.positionsX = positionsX;
        this.positionsY = positionsY;
        this.borderStarts = borderStarts;
        this.borderEnds = borderEnds;
        this.adjacencyStarts = adjacencyStarts;
        this.adjacencies = adjacencies;
        this.buildingPositions = buildingPositions;
        this.buildingTypeCount = buildingTypeCount;
        this.indexByColor = new IntIntMap(Math.max(names.size(), 1));
        this.indexByColor.setDefaultValue(-1);
        for (int index = 0; index < names.size(); index++) {
            this.indexByColor.put(names.color(index), index);
        }
    }

    public int size() {
        return this.names.size();
    }

    public long entityId(int index) {
        return this.names.entityId(index);
    }

    public String nameId(int index) {
        return this.names.nameId(index);
    }

    public int color(int index) {
        return this.names.color(index);
    }

    public int indexOf(long entityId) {
        return this.names.indexOf(entityId);
    }

    public int indexOfColor(int color) {
        return this.indexByColor.get(color);
    }

    public int regionIndex(int index) {
        return this.regionIndexes[index];
    }

    public int terrainIndex(int index) {
        return this.terrainIndexes[index];
    }

    public int resourceGoodIndex(int index) {
        return this.resourceGoodIndexes[index];
    }

    public int positionX(int index) {
        return this.positionsX[index];
    }

    public int positionY(int index) {
        return this.positionsY[index];
    }

    public int borderStart(int index) {
        return this.borderStarts[index];
    }

    public int borderEnd(int index) {
        return this.borderEnds[index];
    }

    public int adjacencyStart(int index) {
        return this.adjacencyStarts[index];
    }

    public int adjacencyEnd(int index) {
        return this.adjacencyStarts[index + 1];
    }

    public int adjacency(int position) {
        return this.adjacencies[position];
    }

    public long buildingPosition(int provinceIndex, int buildingTypeIndex) {
        return this.buildingPositions.getOrDefault(buildingPositionKey(provinceIndex, buildingTypeIndex, this.buildingTypeCount), NO_POSITION);
    }

    public static int buildingPositionKey(int provinceIndex, int buildingTypeIndex, int buildingTypeCount) {
        return provinceIndex * buildingTypeCount + buildingTypeIndex;
    }

    public static long packPosition(int x, int y) {
        return ((long) x << 32) | (y & 0xFFFFFFFFL);
    }

    public static int unpackX(long position) {
        return (int) (position >> 32);
    }

    public static int unpackY(long position) {
        return (int) position;
    }
}
