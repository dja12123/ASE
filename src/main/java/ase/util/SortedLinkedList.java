package ase.util;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

public class SortedLinkedList<T> extends LinkedList<T>
{
	private static final long serialVersionUID = 1L;
	private final Comparator<T> comparator;

	public SortedLinkedList(Comparator<T> comparator)
	{
		super();
		this.comparator = comparator;
	}

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

	public boolean addAll(Collection<? extends T> clctn)
	{
		boolean flag = super.addAll(clctn);
		this.sort();
		return flag;
	}

	public boolean add(T e)
	{
		int index = 0;
		while (index < this.size())
		{
			int compare = this.comparator.compare(this.get(index), e);
			if (compare > 0)
			{
				this.add(index, e);
				break;
			}
			++index;
		}
		return true;
	}

}