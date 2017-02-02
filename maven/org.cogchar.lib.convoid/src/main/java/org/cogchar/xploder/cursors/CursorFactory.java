package org.cogchar.xploder.cursors;

import org.cogchar.api.convoid.act.Act;
import org.cogchar.api.convoid.act.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author matt
 */
public class CursorFactory {
	private static final Logger theLogger = LoggerFactory.getLogger(CursorFactory.class);

	public static MeaningCursorMap buildTransitionMap(Category transRoot) {
		if (transRoot == null) {
			throw new IllegalArgumentException("Root category cannot be null");
		}
		List<IConvoidCursor> cursors = buildAllCursorsForCategory(transRoot, "TRANSITION");
		MeaningCursorMap map = new MeaningCursorMap();
		for (IConvoidCursor c : cursors) {
			for (String m : c.getMeanings()) {
				map.put(m, c);
			}
		}
		return map;
	}

	public static List<Category> getSubCategoriesWithContent(Category cat) {
		if (cat == null) {
			theLogger.warn("Cannot get sub categories for a null category");
			return null;
		}
		List<Category> categories = new ArrayList<>();
		for (Category c : cat.getSubCategories()) {
			categories.addAll(getSubCategoriesWithContent(c));
		}
		for (Act a : cat.getActs()) {
			if (!a.getSteps().isEmpty()) {
				categories.add(cat);
				return categories;
			}
		}
		return categories;
	}

	public static List<CategoryCursor> buildCategoryCursors(List<Category> categories, String type) {
		List<CategoryCursor> cursors = new ArrayList<>();
		for (Category c : categories) {
			cursors.add(new CategoryCursor(c, 360000000L, type));
		}
		return cursors;
	}

	public static List<ActSequenceCursor> getActSequences(CategoryCursor cursor) {
		List<ActSequenceCursor> sequences = new ArrayList<>();
		List<Act> acts = cursor.getCategory().getActs();
		List<Integer> startActs = new ArrayList<>();
		List<Integer> endActs = new ArrayList<>();
		for (int i = 0; i < acts.size(); i++) {
			Act a = acts.get(i);
			if (a.getMeanings() != null && !a.getMeanings().isEmpty()) {
				startActs.add(i);
			}
			if (a.isNextRandom()) {
				endActs.add(i);
			}
		}
		Iterator<Integer> startIt = startActs.iterator();
		Iterator<Integer> endIt = endActs.iterator();
		int endIndex = endIt.hasNext() ? endIt.next() : acts.size() - 1;
		while (startIt.hasNext()) {
			int startIndex = startIt.next();
			while (startIndex > endIndex) {
				endIndex = endIt.hasNext() ? endIt.next() : acts.size() - 1;
			}
			if (endIndex >= startIndex) {
				ActSequenceCursor asc = new ActSequenceCursor(cursor, startIndex, endIndex, 1500000L);
				sequences.add(asc);
			}
		}
		return sequences;
	}

	public static List<IConvoidCursor> buildAllCursorsForCategory(Category cat, String type) {
		List<Category> categories = getSubCategoriesWithContent(cat);
		//System.out.println(cat.getName());
		//System.out.println(categories.get(0).getName());
		List<CategoryCursor> catCursors = buildCategoryCursors(categories, type);
		List<IConvoidCursor> cursors = new ArrayList<>();
		for (CategoryCursor cc : catCursors) {
			cursors.add(cc);
			cursors.addAll(getActSequences(cc));
		}
		return cursors;
	}
}
