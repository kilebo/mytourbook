/*******************************************************************************
 * Copyright (C) 2005, 2016 Wolfgang Schramm and Contributors
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
package net.tourbook.common.time;

public class TimeZoneData {

	public String	label;

	public int		zoneOffsetSeconds;

	public String	zoneId;

	@Override
	public String toString() {
		return "TimeZone [" //$NON-NLS-1$

		+ ("zoneOffset=" + zoneOffsetSeconds + ", ") // //$NON-NLS-1$ //$NON-NLS-2$
				+ ("zoneId=" + zoneId + ", ") //$NON-NLS-1$ //$NON-NLS-2$
				+ ("label=" + label + ", ") //$NON-NLS-1$ //$NON-NLS-2$

				+ "]\n"; //$NON-NLS-1$
	}
}
