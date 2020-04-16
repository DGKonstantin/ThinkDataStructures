package com.allendowney.thinkdast;

import java.util.HashMap;
import java.util.Map;

import org.jfree.data.xy.XYSeries;

import com.allendowney.thinkdast.Profiler.Timeable;

public class ProfileMapPut {
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int startN = 16000;
		int endMillis = 8000;
		//profileHashMapPut(startN, endMillis);
		//profileMyHashMapPut(startN, endMillis);
		profileMyFixedHashMapPut(startN, endMillis);
	}

	/**
	 * Characterize the run time of putting a key in java.util.HashMap
	 */
	public static void profileHashMapPut(int startN, int endMillis) {
		Timeable timeable = new Timeable() {
			Map<String, Integer> map;

			public void setup(int n) {
				map = new HashMap<String, Integer>();
			}

			public void timeMe(int n) {
				for (int i=0; i<n; i++) {
					map.put(String.format("%10d", i), i);
				}
			}
		};
		runProfiler("HashMap put", timeable, startN, endMillis);
	}
	
	/**
	 * Characterize the run time of putting a key in MyHashMap
	 */
	public static void profileMyHashMapPut(int startN, int endMillis) {
		Timeable timeable = new Timeable() {
			Map<String, Integer> map;

			public void setup(int n) {
				map = new MyHashMap<String, Integer>();
			}

			public void timeMe(int n) {
				for (int i=0; i<n; i++) {
					map.put(String.format("%10d", i), i);
				}
			}
		};
		runProfiler("MyHashMap put", timeable, startN, endMillis);
	}
	

	/**
	 * Characterize the run time of putting a key in MyFixedHashMap
	 */
	public static void profileMyFixedHashMapPut(int startN, int endMillis) {
		Timeable timeable = new Timeable() {
			Map<String, Integer> map;

			public void setup(int n) {
				map = new MyFixedHashMap<String, Integer>();
			}

			public void timeMe(int n) {
				for (int i=0; i<n; i++) {
					map.put(String.format("%10d", i), i);
				}
			}
		};
		runProfiler("MyFixedHashMap put", timeable, startN, endMillis);
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