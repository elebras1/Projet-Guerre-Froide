package com.populaire.projetguerrefroide.protocol.catalog;

import com.github.tommyettinger.ds.LongIntMap;
import com.github.tommyettinger.ds.LongList;
import com.github.tommyettinger.ds.IntList;
import com.github.tommyettinger.ds.ObjectList;

public final class NamedTable {
    private final long[] entityIds;
    private final String[] nameIds;
    private final int[] colors;
    private final LongIntMap indexByEntity;

    private NamedTable(long[] entityIds, String[] nameIds, int[] colors) {
        this.entityIds = entityIds;
        this.nameIds = nameIds;
        this.colors = colors;
        this.indexByEntity = new LongIntMap(Math.max(entityIds.length, 1));
        this.indexByEntity.setDefaultValue(-1);
        for (int index = 0; index < entityIds.length; index++) {
            this.indexByEntity.put(entityIds[index], index);
        }
    }

    public int size() {
        return this.entityIds.length;
    }

    public long entityId(int index) {
        return this.entityIds[index];
    }

    public String nameId(int index) {
        return this.nameIds[index];
    }

    public int color(int index) {
        return this.colors[index];
    }

    public int indexOf(long entityId) {
        return this.indexByEntity.get(entityId);
    }

    public static final class Builder {
        private final LongList entityIds = new LongList();
        private final ObjectList<String> nameIds = new ObjectList<>();
        private final IntList colors = new IntList();

        public Builder add(long entityId, String nameId, int color) {
            this.entityIds.add(entityId);
            this.nameIds.add(nameId);
            this.colors.add(color);
            return this;
        }

        public int size() {
            return this.entityIds.size();
        }

        public NamedTable build() {
            return new NamedTable(this.entityIds.toArray(), this.nameIds.toArray(new String[0]), this.colors.toArray());
        }
    }
}
