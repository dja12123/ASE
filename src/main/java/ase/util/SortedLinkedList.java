package ase.util;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

/**
 * This is a doubly linked list that is used to maintain a sorted list. Sorting
 * is O(nlog(n)), look up for max and min are O(1), insertion and deletion is
 * O(1), search in sorted list is O(log(n)).
 *
 * @author SEAK2
 */
public class SortedLinkedList<T> extends LinkedList<T> implements Cloneable
{

	private static final long serialVersionUID = -6089720372001116175L;

	private final Comparator<T> comparator;

	/**
	 * Constructor takes in unsorted list and sorts it. Takes O(nlog(n)) where n is
	 * number of items in collection
	 *
	 * @param unSortedList
	 *            collection of items to sort
	 * @param comparator
	 *            comparator to use to sort
	 */
	public SortedLinkedList(Collection<T> unSortedList, Comparator<T> comparator)
	{
		super(unSortedList);
		this.sort();
		this.comparator = comparator;
	}

	/**
	 * Constructor to create an empty list
	 * 
	 * @param comparator
	 */
	public SortedLinkedList(Comparator<T> comparator)
	{
		super();
		this.comparator = comparator;
	}

	/**
	 * Sorts the collection
	 */
	public final void sort()
	{
		Collections.sort(this, comparator);
	}

	public boolean addAll(int i, Collection<? extends T> clctn)
	{
		boolean flag = super.addAll(i, clctn);
		this.sort();
		return flag;
	}

	@Override
	/**
	 * Can add a collection of items to list, but will sort after addition
	 */
	public boolean addAll(Collection<? extends T> clctn)
	{
		boolean flag = super.addAll(clctn);
		this.sort();
		return flag;
	}

	@Override
    /**
     * Will add item in index to maintain sorted list
     */
    public boolean add(T e) {
    	int index = 0;
    	while(index < this.size())
    	{
    		int compare = this.comparator.compare(this.get(index), e);
    		if(compare > 0)
    		{
    			this.add(index, e);
    			break;
    		}
    		++index;
    	}
       
        return true;
    }

}