package com.pethoalpar.homesensor2.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author alpar.petho
 *
 */
public class CollectionUtil {

	private CollectionUtil() {

	}

	public static <T> List<T> asList(T entity) {
		List<T> retList = new ArrayList<>();
		if (entity != null) {
			retList.add(entity);
		}
		return retList;
	}

	public static <T> boolean isEmpty(Collection<T> entities) {
		return entities == null || entities.isEmpty();
	}

	public static <T> boolean isNotEmpty(Collection<T> entities) {
		return entities != null && !entities.isEmpty();
	}

	public static <T> List<T> emptyList() {
		return new ArrayList<>();
	}

	public static <T> Set<T> emptySet() {
		return new HashSet<>();
	}

	public static <T> List<T> clone(List<T> originalList) {
		List<T> retList = new ArrayList<>();
		if (isNotEmpty(originalList)) {
			originalList.forEach(retList::add);
		}
		return retList;
	}

	public static <T> Boolean equals(List<T> list1, List<T> list2) {
		if (isEmpty(list1) || isEmpty(list2)) {
			return true;
		}
		if ((isEmpty(list1) && isNotEmpty(list2)) || (isNotEmpty(list1) && isEmpty(list2))) {
			return false;
		}
		for (T e1 : list1) {
			boolean contains = false;
			for (T e2 : list2) {
				if (e1.equals(e2)) {
					contains = true;
					break;
				}
			}
			if (!contains) {
				return false;
			}
		}
		for (T e1 : list2) {
			boolean contains = false;
			for (T e2 : list1) {
				if (e1.equals(e2)) {
					contains = true;
					break;
				}
			}
			if (!contains) {
				return false;
			}
		}
		return true;
	}

	public static <T> Boolean notEquals(List<T> list1, List<T> list2) {
		return !equals(list1, list2);
	}

	public static final <T> List<T> emptyIfNull(List<T> list) {
		return list == null ? emptyList() : list;
	}

	public static final <T> Set<T> emptyIfNull(Set<T> list) {
		return list == null ? emptySet() : list;
	}

	public static final <T> Collection<T> emptyIfNull(Collection<T> list) {
		return list == null ? emptyList() : list;
	}

}
