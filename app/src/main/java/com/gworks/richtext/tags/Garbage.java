package com.gworks.richtext.tags;

/**
 * Created by durgadass on 6/1/18.
 */

public class Garbage {

    /**
     * Returns all the markups applied strictly inside the given range [from, to).
     *
     * @param from from inclusive
     * @param to to exclusive
     */
//    public List<Markup> getAppliedMarkups(int from, int to) {
//        ArrayList<Markup> result = new ArrayList<>();
//        Set<Markup> startedMarkups = new HashSet<>(); // To keep track of the started markups.
//
//        for (int i = from; i < to; i++) {
//
//            List<Markup> startingMarkups = spansStartingAt(i);
//            if (startingMarkups != null)
//                startedMarkups.addAll(startingMarkups);
//
//            List<Markup> endingMarkups = spansEndingAt(i);
//            if (endingMarkups != null) {
//                for (Markup endingMarkup : endingMarkups) {
//                    // Only markups started in the given range are added to the result.
//                    if (startedMarkups.contains(endingMarkup)) {
//                        result.add(endingMarkup);
//                        startedMarkups.remove(endingMarkup);
//                    }
//                }
//            }
//        }
//        return result;
//    }

}
