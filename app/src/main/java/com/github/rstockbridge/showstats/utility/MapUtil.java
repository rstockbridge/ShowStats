package com.github.rstockbridge.showstats.utility;

import androidx.annotation.NonNull;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class MapUtil {

    private MapUtil() {
    }

    // returns a map sorted in descending order by value first and
    // in ascending order by key if values are equal
    @NonNull
    public static <T extends Comparable<T>, U extends Comparable<U>> Map<T, U> sortMapByValues(Map<T, U> map) {
        final Set<Map.Entry<T, U>> mapEntries = map.entrySet();

        final List<Map.Entry<T, U>> sortedList = new LinkedList<>(mapEntries);

        Collections.sort(sortedList, (entry1, entry2) -> {
            // if values are equal, sort by key in descending order
            // (so that keys are in ascending order after reversing below)
            if (entry1.getValue().compareTo(entry2.getValue()) == 0) {
                return -entry1.getKey().compareTo(entry2.getKey());
            } else {

                return entry1.getValue().compareTo(entry2.getValue());
            }
        });

        // sort values in descending order
        Collections.reverse(sortedList);

        final Map<T, U> result = new LinkedHashMap<>();
        for (final Map.Entry<T, U> entry : sortedList) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }
}
