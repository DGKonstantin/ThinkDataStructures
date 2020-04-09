package com.allendowney.thinkdast;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.jfree.data.xy.XYSeries;

import com.allendowney.thinkdast.Profiler.Timeable;

public class ProfileListAdd {
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int startN = 512000;
		int endMillis = 10000;
		//profileArrayListAddEnd(startN, endMillis);
		//profileArrayListAddBeginning(startN, endMillis);
		profileLinkedListAddBeginning(startN, endMillis);
		profileLinkedListAddEnd(startN, endMillis);
	}

	/**
	 * Characterize the run time of adding to the end of an ArrayList
	 */
	public static void profileArrayListAddEnd(int startN, int endMillis) {
		Timeable timeable = new Timeable() {
			List<String> list;

			public void setup(int n) {
				list = new ArrayList<String>();
			}

			public void timeMe(int n) {
				for (int i=0; i<n; i++) {
					list.add("a string");
				}
			}
		};
		runProfiler("ArrayList add end", timeable, startN, endMillis);
	}
	
	/**
	 * Characterize the run time of adding to the beginning of an ArrayList
	 */
	public static void profileArrayListAddBeginning(int startN, int endMillis) {
		Timeable timeable = new Timeable() {
			List<String> list;

			@Override
			public void setup(int n) {
				list = new ArrayList<>();
			}

			@Override
			public void timeMe(int n) {
				for (int i = 0; i < n; i++) {
					list.add(0,"a string");
				}
			}
		};
		runProfiler("ArrayList add begin", timeable, startN, endMillis);
	}

	/**
	 * Characterize the run time of adding to the beginning of a LinkedList
	 */
	public static void profileLinkedListAddBeginning(int startN, int endMillis) {
		Timeable timeable = new Timeable() {
			List<String> list;

			@Override
			public void setup(int n) {
				list = new LinkedList<>();
			}

			@Override
			public void timeMe(int n) {
				for (int i = 0; i < n; i++) {
					list.add(0, "a String");
				}
			}
		};
		runProfiler("LinkedList add begin", timeable, startN, endMillis);
	}

	/**
	 * Characterize the run time of adding to the end of a LinkedList
	 */
	public static void profileLinkedListAddEnd(int startN, int endMillis) {
		Timeable timeable = new Timeable() {
			List<String> list;

			@Override
			public void setup(int n) {
				list = new LinkedList<>();
			}

			@Override
			public void timeMe(int n) {
				for (int i = 0; i < n; i++) {
					list.add("a String");
				}
			}
		};
		runProfiler("LinkedList add end", timeable, startN, endMillis);
	}

	/**
	 * Runs the profiles and displays results.
	 * 
	 * @param timeable
	 * @param startN
	 * @param endMillis
	 */
	private static void runProfiler(String title, Timeable timeable, int startN, int endMillis) {
		Profiler profiler = new Profiler(title, timeable);
		XYSeries series = profiler.timingLoop(startN, endMillis);
		profiler.plotResults(series);
	}
}