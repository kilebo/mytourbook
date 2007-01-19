/*******************************************************************************
 * Copyright (C) 2006, 2007  Wolfgang Schramm
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
 * Administrator 10.08.2005
 */

package net.tourbook.chart;


public class ChartMarker {

	public String	graphLabel	= ""; //$NON-NLS-1$
	public String	markerLabel	= ""; //$NON-NLS-1$

	public int		graphX;

	/**
	 * index in the data serie
	 */
	public int		serieIndex;

	/**
	 * visual position in the chart
	 */
	public int		visualPosition;

	/**
	 * marker type, this can be <code>TourMarker.MARKER_TYPE_DEVICE</code> or
	 * <code>TourMarker.MARKER_TYPE_CUSTOM</code>
	 */
	public int		type;

	public ChartMarker() {}
}
