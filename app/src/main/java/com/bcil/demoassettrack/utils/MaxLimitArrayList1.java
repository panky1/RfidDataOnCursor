package com.bcil.demoassettrack.utils;

import com.bcil.demoassettrack.model.AssetInfo;
import com.zebra.rfid.api3.Constants;

import java.util.ArrayList;
import java.util.Collection;


public class MaxLimitArrayList1 extends ArrayList<AssetInfo> {
    private static final int MAX_ITEMS = Constants.UNIQUE_TAG_LIMIT;

    @Override
    public synchronized boolean add(AssetInfo inventoryListItem) {
        if (size() < MAX_ITEMS)
            return super.add(inventoryListItem);
        else {
            return false;
        }
    }

    @Override
    public synchronized void add(int index, AssetInfo inventoryListItem) {
        if (size() < MAX_ITEMS)
            super.add(index, inventoryListItem);
    }

    @Override
    public synchronized boolean addAll(Collection<? extends AssetInfo> collection) {
        if (size() + collection.size() < MAX_ITEMS)
            return super.addAll(collection);
        else {
            return false;
        }
    }

    @Override
    public synchronized boolean addAll(int index, Collection<? extends AssetInfo> collection) {
        if (size() + collection.size() < MAX_ITEMS)
            return super.addAll(index, collection);
        else {
            return false;
        }
    }
}

