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
package net.tourbook.common.formatter;

import java.text.NumberFormat;

import net.tourbook.common.Messages;

public class ValueFormatter_Number_1_3 implements IValueFormatter {

	private final NumberFormat _nf3 = NumberFormat.getNumberInstance();

	public ValueFormatter_Number_1_3() {

		_nf3.setMinimumFractionDigits(3);
		_nf3.setMaximumFractionDigits(3);
	}

	public ValueFormatter_Number_1_3(final boolean isGroupingUsed) {

		this();

		_nf3.setGroupingUsed(isGroupingUsed);
	}

	@Override
	public String printDouble(final double value) {
		return _nf3.format(value);
	}

	@Override
	public String printLong(final long value) {
		return Messages.App_Error_NotSupportedValueFormatter;
	}

	@Override
	public String toString() {
		return "ValueFormatter_Number_1_3 [" // //$NON-NLS-1$
				+ "printDouble()" //$NON-NLS-1$
				+ "]"; //$NON-NLS-1$
	}

}
