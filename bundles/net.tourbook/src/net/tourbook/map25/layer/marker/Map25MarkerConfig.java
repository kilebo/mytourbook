/*******************************************************************************
 * Copyright (C) 2005, 2017 Wolfgang Schramm and Contributors
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
package net.tourbook.map25.layer.marker;

import net.tourbook.common.util.StatusUtil;
import net.tourbook.map25.Map25ConfigManager;

import org.eclipse.swt.graphics.RGB;

public class Map25MarkerConfig implements Cloneable {

	/*
	 * Set default values also here to ensure that a valid value is set. A default value would not
	 * be set when an xml tag is not available.
	 */

	public String	id						= Long.toString(System.nanoTime());
	public String	defaultId				= Map25ConfigManager.CONFIG_DEFAULT_ID_1;
	public String	name					= Map25ConfigManager.CONFIG_DEFAULT_ID_1;

	/*
	 * Marker
	 */
	public RGB		markerColorForeground	= Map25ConfigManager.DEFAULT_MARKER_FOREGROUND;
	public RGB		markerColorBackground	= Map25ConfigManager.DEFAULT_MARKER_BACKGROUND;
	public int		markerOrientation		= Map25ConfigManager.SYMBOL_ORIENTATION_BILLBOARD;
	public int		markerSymbolSizeDP		= Map25ConfigManager.DEFAULT_MARKER_SYMBOL_SIZE;

	/*
	 * Cluster
	 */
	public boolean	isMarkerClustered		= true;
	public RGB		clusterColorForeground	= Map25ConfigManager.DEFAULT_CLUSTER_FOREGROUND;
	public RGB		clusterColorBackground	= Map25ConfigManager.DEFAULT_CLUSTER_BACKGROUND;
	public int		clusterGridSize			= Map25ConfigManager.DEFAULT_CLUSTER_GRID_SIZE;
	public int		clusterOrientation		= Map25ConfigManager.SYMBOL_ORIENTATION_BILLBOARD;
	public int		clusterSymbolSize		= Map25ConfigManager.DEFAULT_CLUSTER_SYMBOL_SIZE;

	/**
	 * Create a copy of this object.
	 * 
	 * @return a copy of this <code>Insets</code> object.
	 */
	@Override
	public Object clone() {

		try {

			// create clones for shallow copied fields so that they can be modified

			final Map25MarkerConfig newConfig = (Map25MarkerConfig) super.clone();

//			newConfig.clusterColorBackground = new RGB(
//					clusterColorBackground.red,
//					clusterColorBackground.green,
//					clusterColorBackground.blue);
//
//			newConfig.clusterColorForeground = new RGB(
//					clusterColorForeground.red,
//					clusterColorForeground.green,
//					clusterColorForeground.blue);

			return newConfig;

		} catch (final CloneNotSupportedException e) {

			// this shouldn't happen, since we are Cloneable
			StatusUtil.log(e);
			throw new InternalError();
		}
	}

	@Override
	public boolean equals(final Object obj) {

		if (this == obj) {
			return true;
		}

		if (obj == null) {
			return false;
		}

		if (getClass() != obj.getClass()) {
			return false;
		}

		final Map25MarkerConfig other = (Map25MarkerConfig) obj;

		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {

		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());

		return result;
	}

	@Override
	public String toString() {
		return "Map25MarkerConfig ["

				+ "id=" + id + ", "
				+ "name=" + name + ", "

				+ "iconClusterSizeDP=" + clusterSymbolSize + ", "
				+ "iconMarkerSizeDP=" + markerSymbolSizeDP + ", "

				+ "clusterColorForeground=" + clusterColorForeground + ", "
				+ "clusterColorBackground=" + clusterColorBackground +

				"]\n";
	}
}