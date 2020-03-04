/*******************************************************************************
 * Copyright (C) 2020 Frédéric Bard and Contributors
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
package net.tourbook.trainingstress;

import java.util.ArrayList;

import net.tourbook.data.TourData;
import net.tourbook.data.TourPerson;
import net.tourbook.tour.TourManager;

/**
 * Class that implements several of Dr Skiba's formulas that apply to running
 * More information can be found at http://topofusion.com/govss.php
 * Possible features:
 * - Use this equation to display an estimated power graph in the tour chart
 * - Add the GOVSS column in the tour book view
 */
public class Govss {
   //TODO; When a tour has its tour type changed, if this new tour type is not in the govss list, we remove the tourid from the performancemodeling table

   private TourPerson _tourPerson;
   private TourData   _tourData;

   public Govss(final TourPerson tourPerson) {
      this._tourPerson = tourPerson;
   }

   public Govss(final TourPerson tourPerson, final TourData tourData) {
      this._tourPerson = tourPerson;
      this._tourData = tourData;
   }

   /**
    * Function that calculates the GOVSS (Gravity Ordered Velocity Stress Score) for a given run and
    * athlete.
    * References
    * http://runscribe.com/wp-content/uploads/power/GOVSS.pdf
    * https://3record.de/about/power_estimation#run
    * https://runscribe.com/power/
    * Note : This function will assume that the tour is a run activity. If not, be aware that the
    * GOVSS value will be worthless.
    *
    * @return The GOVSS value
    */
   public int Compute() {
      if (_tourPerson == null || _tourData == null) {
         return 0;
      }

      // 1. Find the athlete’s velocity at LT by a 10 km to one hour maximal run.
      // 2. Convert this LT limited velocity to a LT limited power value using Equation 7. "Lactate limited power" may also be called "lactate adjusted power".
      final float athleteThresholdPower = _tourPerson.getGovssThresholdPower();

      // 3. Analyze the data from a particular workout from an athlete’s log, computing 120 second rolling averages from velocity and slope data.
      final ArrayList<Double> powerValues = computePowerValues();

      if (powerValues == null || powerValues.size() == 0) {
         return 0;
      }

      // 4. Raise the values in step 3 to the 4th power.
      for (int index = 0; index < powerValues.size(); index++) {
         powerValues.set(index, Math.pow(powerValues.get(index), 4));
      }

      // 5. Average values from step 4.
      final double averagePower = powerValues.stream().mapToDouble(Double::doubleValue).average().getAsDouble();

      // 6. Take the 4th root of step 5. This is the Lactate-Normalized Power.
      final float lactateNormalizedPower = (float) Math.pow(averagePower, 0.25);

      // 7. Divide Lactate Normalized Power by Threshold Power from step 2 to get the Intensity Weighting Fraction.
      final float intensityWeighingFactor = lactateNormalizedPower / athleteThresholdPower;

      // 8. Multiply the Lactate Normalized Power by the duration of the workout in seconds to obtain the normalized work performed in joules.
      final int normalizedWork = Math.round(lactateNormalizedPower * _tourData.getTourRecordingTime());

      // 9. Multiply value obtained in step 8 by the Intensity Weighting Fraction to get a raw training stress value.
      float trainingStressValue = normalizedWork * intensityWeighingFactor;

      // 10. Divide the values from step 9 by the amount of work performed during the 10k to 1 hr test (threshold power in watts x number of seconds).
      trainingStressValue /= (athleteThresholdPower * _tourPerson.getGovssTimeTrialDuration()); // 2400 = 40*60 = 40min

      // 11. Multiply the number from step 10 by 100 to obtain the final training stress in GOVSS.
      trainingStressValue *= 100;

      //Should that trigger a recompute of the Performance chart data ?
      return (int) trainingStressValue;
   }

   /**
    * Computes Caero (This is the energy cost of overcoming aerodynamic drag)
    *
    * @param speed
    * @return
    */
   private double computeCostAerodynamicDrag(final float speed) {

      final double Af = 0.2025 * 0.266 * Math.pow(_tourPerson.getHeight(), 0.725) * Math.pow(_tourPerson.getWeight(), 0.425);
      final double CAero = 0.5 * 1.2 * 0.9 * Af * Math.pow(speed, 2) / _tourPerson.getWeight();

      return CAero;
   }

   /**
    * Computes Ci (The energy cost to cover any given distance) with i being the slope of the
    * running surface (in ??? unit)
    *
    * @return
    */
   private double computeCostDistanceWithSlope(final double slope) {

      final double Cslope = (155.4 * Math.pow(slope, 5)) - (30.4 * Math.pow(slope, 4)) - (43.3 * Math.pow(slope, 3)) + (46.3 * Math.pow(slope, 2))
            + (19.5 * slope) + 3.6;

      return Cslope;
   }

   /**
    * Computes Ckin (The energy cost of changes in velocity)
    *
    * @return
    */
   private double computeCostKineticEnergy(final double distance, final double initialSpeed, final double speed) {

      final double Ckin = distance > 0 ? 0.5 * (Math.pow(speed, 2) - Math.pow(initialSpeed, 2)) / distance : 0;

      return Ckin;
   }

   /**
    * Function that calculates the running power for a given distance, slope, time and athlete.
    *
    * @return
    */
   public double ComputePower(final float distance, final double slope, final float initialSpeed, final float speed) {

      final double CAero = computeCostAerodynamicDrag(speed);
      final double Ckin = computeCostKineticEnergy(distance, initialSpeed, speed);
      final double Cslope = computeCostDistanceWithSlope(slope);
      final double efficiency = (0.25 + (0.054 * speed)) * (1 - ((0.5 * speed) / 8.33));

      final double power = (CAero + Ckin + Cslope * efficiency) * speed * _tourPerson.getWeight();

      return power;
   }

   private ArrayList<Double> computePowerValues() {
      if (_tourData == null) {
         return null;
      }

      final int[] timeSerie = _tourData.timeSerie;
      final int timeSeriesLength = timeSerie.length;

      final ArrayList<Double> powerValues = new ArrayList<>();

      final int rollingAverageInterval = 120; // The formula calls for 120 second rolling averages
      double powerValue = 0;
      int serieStartIndex = 0;
      int serieEndIndex = 0;
      float currentDistance = 0;
      float currentSlope = 0;
      float initialSpeed = 0;
      float currentSpeed = 0;

      for (; serieEndIndex < timeSeriesLength - 1;) {

         double currentRecordingTime = 0;
         serieStartIndex = serieEndIndex;

         for (; currentRecordingTime < rollingAverageInterval && serieEndIndex < timeSeriesLength - 1;) {

            ++serieEndIndex;
            currentRecordingTime = Math.max(0,
                  timeSerie[serieEndIndex] - timeSerie[serieStartIndex] - _tourData.getBreakTime(serieStartIndex, serieEndIndex));
         }

         currentSpeed = TourManager.computeTourSpeed(_tourData, serieStartIndex, serieEndIndex);
         //Convert the speed (km/h) to velocity (m/s)
         currentSpeed /= 3.6;
         currentDistance = TourManager.computeTourDistance(_tourData, serieStartIndex, serieEndIndex);
         currentSlope = TourManager.computeTourAverageGradient(_tourData, serieStartIndex, serieEndIndex);
         powerValue = ComputePower(currentDistance, currentSlope, initialSpeed, currentSpeed);

         if (currentSlope > -1 && currentSlope < 1 && currentDistance > 0) {
            powerValues.add(powerValue);
         }

         initialSpeed = currentSpeed;
      }

      return powerValues;
   }
}
