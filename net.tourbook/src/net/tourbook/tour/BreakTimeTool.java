/*******************************************************************************
 * Copyright (C) 2005, 2011  Wolfgang Schramm and Contributors
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation version 2 of the License.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110, USA
 *******************************************************************************/
package net.tourbook.tour;

import net.tourbook.Messages;
import net.tourbook.application.TourbookPlugin;
import net.tourbook.data.TourData;
import net.tourbook.preferences.ITourbookPreferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * This class contains all data which are needed to compute tour break time.
 */
public class BreakTimeTool {

	private static int				_prefBreakTimeMethod;
	private static int				_prefShortestTime;
	private static float			_prefMaxDistance;
	private static float			_prefMinSliceSpeed;
	private static float			_prefMinAvgSpeed;

	/**
	 * break time method which is an index for a combo box
	 */
	public static final int			BREAK_TIME_METHOD_BY_TIME_DISTANCE	= 0;
	public static final int			BREAK_TIME_METHOD_BY_SLICE_SPEED	= 1;
	public static final int			BREAK_TIME_METHOD_BY_AVG_SPEED		= 2;

	public static final String[]	BREAK_TIME_METHODS					= { //
																		Messages.Compute_BreakTime_Method_TimeDistance, // 0
			Messages.Compute_BreakTime_Method_SpeedBySlice, // 1
			Messages.Compute_BreakTime_Method_SpeedByAverage			// 2
																		};

	private static IPreferenceStore	_prefStore							= TourbookPlugin
																				.getDefault()
																				.getPreferenceStore();
	private static boolean			_isPrefSet;

	/**
	 * method how break time is computed
	 */
	public int						breakTimeMethod;

	/**
	 * shortes tims in seconds
	 */
	public int						breakShortestTime;

	/**
	 * max distance in meter
	 */
	public float					breakMaxDistance;

	/**
	 * slice speed in km/h
	 */
	public float					breakMinSliceSpeed;

	/**
	 * average speed in km/h
	 */
	public float					breakMinAvgSpeed;

	@SuppressWarnings("unused")
	private BreakTimeTool() {}

	public BreakTimeTool(	final int breakTimeMethod,
							final int breakShortestTime,
							final float breakMaxDistance,
							final float breakMinSliceSpeed,
							final float breakMinAvgSpeed) {

		this.breakTimeMethod = breakTimeMethod;

		// 0
		this.breakShortestTime = breakShortestTime;
		this.breakMaxDistance = breakMaxDistance;

		// 1
		this.breakMinSliceSpeed = breakMinSliceSpeed;

		// 2
		this.breakMinAvgSpeed = breakMinAvgSpeed;
	}

	private static void checkPrefValues() {

		if (_isPrefSet) {
			return;
		}

		updatePrefValues();
		_isPrefSet = true;

		// observe modifications

		_prefStore.addPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(final PropertyChangeEvent event) {

				final String property = event.getProperty();

				if (property.equals(ITourbookPreferences.BREAK_TIME_IS_MODIFIED)
						|| property.equals(ITourbookPreferences.MEASUREMENT_SYSTEM)) {

					// break time or measurement has been modified

					updatePrefValues();
				}
			}
		});
	}

	public static BreakTimeResult computeBreakTimeBySpeed(	final TourData tourData,
															final int breakMethod,
															final float minSpeed) {

		final int[] timeSerie = tourData.timeSerie;
		final int[] distanceSerie = tourData.getMetricDistanceSerie();

		final boolean[] breakTimeSerie = new boolean[timeSerie.length];

		boolean isSliceSpeed;
		int[] speedSerie = null;

		if (breakMethod == BreakTimeTool.BREAK_TIME_METHOD_BY_SLICE_SPEED) {

			// slice speed

			isSliceSpeed = true;

		} else {

			// average speed

			isSliceSpeed = false;

			speedSerie = tourData.getSpeedSerieMetric();
		}

		int lastTime = 0;
		int lastDistance = 0;
		int tourBreakTime = 0;

		for (int serieIndex = 0; serieIndex < timeSerie.length; serieIndex++) {

			final int currentTime = timeSerie[serieIndex];
			final int currentDistance = distanceSerie[serieIndex];

			final int timeDiffSlice = currentTime - lastTime;
			final int distDiffSlice = currentDistance - lastDistance;

			float speedToCheck;
			if (isSliceSpeed) {
				speedToCheck = timeDiffSlice == 0 ? //
						0
						: (float) (distDiffSlice * 3.6 / timeDiffSlice);
			} else {
				speedToCheck = (float) (speedSerie[serieIndex] / 10.0);
			}

			if (speedToCheck <= minSpeed) {

				// current time slice is also a break

				breakTimeSerie[serieIndex] = true;
				tourBreakTime += timeDiffSlice;
			}

			lastTime = currentTime;
			lastDistance = currentDistance;
		}

		return new BreakTimeResult(breakTimeSerie, tourBreakTime);
	}

	public static BreakTimeResult computeBreakTimeByTimeDistance(	final TourData tourData,
																	final int shortestBreakTime,
																	final float maxDistance) {

		final int[] timeSerie = tourData.timeSerie;
		final int[] distanceSerie = tourData.getMetricDistanceSerie();

		final boolean[] breakTimeSerie = new boolean[timeSerie.length];

		int lastTime = 0;
		int lastDistance = 0;
		int tourBreakTime = 0;

		for (int serieIndex = 0; serieIndex < timeSerie.length; serieIndex++) {

			final int currentTime = timeSerie[serieIndex];
			final int currentDistance = distanceSerie[serieIndex];

			final int timeDiffSlice = currentTime - lastTime;
			final int distDiffSlice = currentDistance - lastDistance;

			if (timeDiffSlice > shortestBreakTime && distDiffSlice < maxDistance) {

				/*
				 * this is the simplest case for a break, a slice is larger than the shortest break
				 * time
				 */

				breakTimeSerie[serieIndex] = true;
				tourBreakTime += timeDiffSlice;

			} else {

				/*
				 * go back in the data serie to find all time slices which are within the shortest
				 * break time
				 */
				int prevIndex = serieIndex;
				int timeDiffPrevSlices = 0;
				int distDiffPrevSlices = 0;

				while (timeDiffPrevSlices <= shortestBreakTime) {

					prevIndex--;

					// check bounds
					if ((prevIndex < 0)) {
						break;
					}

					/*
					 * ignore previous time slices when the first previous time slice was a longer
					 * break, because the start after the break is always slowly
					 */
//					if (prevIndex == serieIndex - 1
//							&& (timeDiffSlice > _breakUIShortestBreakTime && distDiffSlice < _breakUIMaxDistance)) {
//						break;
//					}

					final int prevTime = timeSerie[prevIndex];
					final int prevDist = distanceSerie[prevIndex];

					timeDiffPrevSlices = currentTime - prevTime;
					distDiffPrevSlices = currentDistance - prevDist;
				}

				if (timeDiffPrevSlices > shortestBreakTime && distDiffPrevSlices < maxDistance) {

					// current time slice is also a break

					breakTimeSerie[serieIndex] = true;
					tourBreakTime += timeDiffSlice;
				}
			}

			lastTime = currentTime;
			lastDistance = currentDistance;
		}

		return new BreakTimeResult(breakTimeSerie, tourBreakTime);
	}

	/**
	 * @return Returns values from the prer store for computing the break time.
	 */
	public static BreakTimeTool getPrefValues() {

		checkPrefValues();

		return new BreakTimeTool(
				_prefBreakTimeMethod,
				_prefShortestTime,
				_prefMaxDistance,
				_prefMinSliceSpeed,
				_prefMinAvgSpeed);
	}

	private static void updatePrefValues() {

		_prefBreakTimeMethod = _prefStore.getInt(ITourbookPreferences.BREAK_TIME_METHOD);

		_prefShortestTime = _prefStore.getInt(ITourbookPreferences.BREAK_TIME_SHORTEST_TIME);
		_prefMaxDistance = _prefStore.getFloat(ITourbookPreferences.BREAK_TIME_MAX_DISTANCE);

		_prefMinSliceSpeed = _prefStore.getFloat(ITourbookPreferences.BREAK_TIME_MIN_SLICE_SPEED);
		_prefMinAvgSpeed = _prefStore.getFloat(ITourbookPreferences.BREAK_TIME_MIN_AVG_SPEED);
	}

}