package org.cogchar.convoid.cursors;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import org.cogchar.convoid.output.config.Act;
import org.cogchar.convoid.output.config.Category;

/**
 *
 * @author matt
 */
public class CursorFactory {
    private static Logger theLogger = Logger.getLogger(CursorFactory.class.getName());

    public static MeaningCursorMap buildTransitionMap(Category transRoot){
        if(transRoot == null){
            throw new IllegalArgumentException("Root category cannot be null");
        }
        List<IConvoidCursor> cursors = buildAllCursorsForCategory(transRoot, "TRANSITION");
        MeaningCursorMap map = new MeaningCursorMap();
        for(IConvoidCursor c : cursors){
            for(String m : c.getMeanings()){
                map.put(m, c);
            }
        }
        return map;
    }

    public static List<Category> getSubCategoriesWithContent(Category cat){
        if(cat == null){
            theLogger.warning("Cannot get sub categories for a null category");
            return null;
        }
        List<Category> categories = new ArrayList<Category>();
        for(Category c : cat.getSubCategories()){
            categories.addAll(getSubCategoriesWithContent(c));
        }
        for(Act a : cat.getActs()){
            if(!a.getSteps().isEmpty()){
                categories.add(cat);
                return categories;
            }
        }
        return categories;
    }

    public static List<CategoryCursor> buildCategoryCursors(List<Category> categories, String type){
        List<CategoryCursor> cursors = new ArrayList<CategoryCursor>();
        for(Category c : categories){
            cursors.add(new CategoryCursor(c, 360000000L, type));
        }
        return cursors;
    }

    public static List<ActSequenceCursor> getActSequences(CategoryCursor cursor){
		List<ActSequenceCursor> sequences = new ArrayList<ActSequenceCursor>();
        List<Act> acts = cursor.getCategory().getActs();
        List<Integer> startActs = new ArrayList<Integer>();
        List<Integer> endActs = new ArrayList<Integer>();
        for(int i=0; i<acts.size(); i++){
            Act a = acts.get(i);
            if(a.getMeanings() != null && !a.getMeanings().isEmpty()){
                startActs.add(i);
            }
            if(a.isNextRandom()){
                endActs.add(i);
            }
        }
        Iterator<Integer> startIt = startActs.iterator();
        Iterator<Integer> endIt = endActs.iterator();
        int endIndex = endIt.hasNext() ? endIt.next() : acts.size() - 1;
        while(startIt.hasNext()){
            int startIndex = startIt.next();
            while(startIndex > endIndex){
                endIndex = endIt.hasNext() ? endIt.next() : acts.size() - 1;
            }
            if(endIndex >= startIndex){
                ActSequenceCursor asc = new ActSequenceCursor(cursor, startIndex, endIndex, 1500000L);
                sequences.add(asc);
            }
        }
		return sequences;
    }

    public static List<IConvoidCursor> buildAllCursorsForCategory(Category cat, String type){
        List<Category> categories = getSubCategoriesWithContent(cat);
        //System.out.println(cat.getName());
        //System.out.println(categories.get(0).getName());
        List<CategoryCursor> catCursors = buildCategoryCursors(categories, type);
        List<IConvoidCursor> cursors = new ArrayList<IConvoidCursor>();
        for(CategoryCursor cc : catCursors){
            cursors.add(cc);
            cursors.addAll(getActSequences(cc));
        }
        return cursors;
    }
}
