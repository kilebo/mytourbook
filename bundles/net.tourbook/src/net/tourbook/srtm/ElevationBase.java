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
/**
 * @author Alfred Barten
 */
package net.tourbook.srtm;

import java.io.File;

import net.tourbook.application.TourbookPlugin;
 
public abstract class ElevationBase {

	final public GeoLat	gridLat;
	final public GeoLon	gridLon;
	final public GeoLat	firstLat;
	final public GeoLat	lastLat;
	final public GeoLon	firstLon;
	final public GeoLon	lastLon;

	public ElevationBase() {
		gridLat = new GeoLat();
		gridLon = new GeoLon();
		firstLat = new GeoLat();
		lastLat = new GeoLat();
		firstLon = new GeoLon();
		lastLon = new GeoLon();
	}

	public synchronized static String getElevationDataPath(final String layerSubdir) {

		// Create directory for local placement of the elevation files and return its path

		String elevationDataPath;

		final String prefDataPath = TourbookPlugin.getDefault()
				.getPreferenceStore()
				.getString(IPreferences.SRTM_DATA_FILEPATH); // TODO rename

		if (prefDataPath.length() == 0 || new File(prefDataPath).exists() == false) {
			elevationDataPath = (String) System.getProperties().get("user.home"); //$NON-NLS-1$
		} else {
			elevationDataPath = prefDataPath;
		}

		elevationDataPath = elevationDataPath.replace('/', File.separatorChar).replace('\\', File.separatorChar);
		if (!elevationDataPath.endsWith(File.separator)) {
			elevationDataPath = elevationDataPath + File.separator;
		}
		elevationDataPath = elevationDataPath + layerSubdir;
		final File elevationDataDir = new File(elevationDataPath);
		if (elevationDataDir.exists() == false) {
			if (elevationDataDir.mkdirs() == false) {
				return null;
			}
		}
		return elevationDataPath;
	}

	public static void main(final String[] args) {}

	/**
	 * @param lat
	 * @param lon
	 * @return
	 */
	public synchronized float getElevation(final GeoLat lat, final GeoLon lon) {
		return 0;
	}

	/**
	 * @param lat
	 * @param lon
	 * @return
	 */
	public synchronized double getElevationDouble(final GeoLat lat, final GeoLon lon) {
		return 0;
	}

	public synchronized float getElevationGrid(final GeoLat lat, final GeoLon lon) {
		return (float) getElevationGridDouble(lat, lon);
	}

	/**
	 * @param colorButtons
	 * @param l
	 * @return Returns {@link Float#MIN_VALUE} when the altitude is invalid, this can happen when
	 *         the altitude cannot be read from a file or the file cannot be retrieved from the SRTM
	 *         host.
	 */
	public synchronized double getElevationGridDouble(final GeoLat lat, final GeoLon lon) {

		float elev1, elev2, elev3, elev4;
		double p, q;
		short ok = 0;
		double elevMid;

		firstLat.toLeft(lat, gridLat);
		lastLat.toRight(lat, gridLat);
		firstLon.toLeft(lon, gridLon);
		lastLon.toRight(lon, gridLon);

		elev1 = getElevation(lastLat, firstLon);
		elev2 = getElevation(lastLat, lastLon);
		elev3 = getElevation(firstLat, firstLon);
		elev4 = getElevation(firstLat, lastLon);

		// adjust invalid values
		final boolean isValidElev1 = isValid(elev1);
		final boolean isValidElev2 = isValid(elev2);
		final boolean isValidElev3 = isValid(elev3);
		final boolean isValidElev4 = isValid(elev4);

		ok = 0;
		if (isValidElev1) {
			ok++;
		}
		if (isValidElev2) {
			ok++;
		}
		if (isValidElev3) {
			ok++;
		}
		if (isValidElev4) {
			ok++;
		}
		
		if (ok != 4) {

			if (ok == 0) {
				elevMid = (elev1 + elev2 + elev3 + elev4) / 4;
				return Float.MIN_VALUE;
			}
			
			elevMid = 0;
			
			if (isValidElev1) {
				elevMid += elev1;
			}
			if (isValidElev2) {
				elevMid += elev2;
			}
			if (isValidElev3) {
				elevMid += elev3;
			}
			if (isValidElev4) {
				elevMid += elev4;
			}
			
			elevMid /= ok;
			
			if (!isValidElev1) {
				elev1 = (float) elevMid;
			}
			if (!isValidElev2) {
				elev2 = (float) elevMid;
			}
			if (!isValidElev3) {
				elev3 = (float) elevMid;
			}
			if (!isValidElev4) {
				elev4 = (float) elevMid;
			}
		}

		p = lat.decimal - firstLat.decimal;
		p /= lastLat.decimal - firstLat.decimal;
		q = lon.decimal - firstLon.decimal;
		q /= lastLon.decimal - firstLon.decimal;

		return ((1 - q) * p * elev1 + q * p * elev2 + (1 - q) * (1 - p) * elev3 + q * (1 - p) * elev4 + 0.5);
	}

	// dummy
	public String getName() {
		return "FILETYPE-DUMMY"; //$NON-NLS-1$
	}

	// dummy
	public short getSecDiff() {
		// number of degrees seconds between two data points
		return 42;
	}

	public boolean isValid(final double elev) {
		return isValid((short) elev);
	}

	public boolean isValid(final short elev) {
		if (elev >= -11000 && elev < 8850) {
			return true;
		}
		return false;
	}

}
