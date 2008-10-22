/*******************************************************************************
 * Copyright (C) 2005, 2008  Wolfgang Schramm and Contributors
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
package net.tourbook.statistic;

import java.util.ArrayList;

import net.tourbook.Messages;
import net.tourbook.ui.views.tourCatalog.TourCatalogViewYearStatistic;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

public class ActionSelectYears extends Action implements IMenuCreator {

	private Menu							fMenu;

	private ArrayList<ActionYear>			fYearActions		= new ArrayList<ActionYear>();

	private TourCatalogViewYearStatistic	fYearStatistic;

	private int								fSelectedYear;

	public int								fIsSelectionCounter	= 0;

	private class ActionYear extends Action {

		private int	fActionYear;

		public ActionYear(final int year) {

			super(Integer.toString(year), AS_RADIO_BUTTON);

			fActionYear = year;
		}

		@Override
		public void run() {

			if (fIsSelectionCounter == 0) {

				setChecked(false);

			} else if (fIsSelectionCounter == 1) {

				/*
				 * there is somewhere a bug that the first selection does not work
				 */

				setChecked(true);

				fSelectedYear = fActionYear;
				fYearStatistic.onExecuteSelectNumberOfYears(fActionYear);

			} else {

				if (isChecked()) {

					// ignore uncheck event

					fSelectedYear = fActionYear;
					fYearStatistic.onExecuteSelectNumberOfYears(fActionYear);
				}
			}

			fIsSelectionCounter++;
		}
	}

	public ActionSelectYears(final TourCatalogViewYearStatistic yearStatistic) {

		super(Messages.tourCatalog_view_action_number_of_years, AS_DROP_DOWN_MENU);
		setMenuCreator(this);

		fYearStatistic = yearStatistic;

		// create action for each year
		for (int yearIndex = 0; yearIndex < 20; yearIndex++) {
			fYearActions.add(new ActionYear(yearIndex + 1));
		}

		// set as default
		fYearActions.get(0).setChecked(true);
	}

	private void addActionToMenu(final Action action) {
		final ActionContributionItem item = new ActionContributionItem(action);
		item.fill(fMenu, -1);
	}

	public void dispose() {
		if (fMenu != null) {
			fMenu.dispose();
			fMenu = null;
		}
	}

	public Menu getMenu(final Control parent) {
		return null;
	}

	public Menu getMenu(final Menu parent) {

		fMenu = new Menu(parent);

		for (final ActionYear yearAction : fYearActions) {
			addActionToMenu(yearAction);
		}

		return fMenu;
	}

	public int getSelectedYear() {
		return fSelectedYear;
	}

	public void setNumberOfYears(final int selectedYear) {

		fSelectedYear = selectedYear;

		// update check status for the menu items
		for (final ActionYear yearAction : fYearActions) {
			if (yearAction.fActionYear == selectedYear) {
				yearAction.setChecked(true);
			} else {
				yearAction.setChecked(false);
			}
		}
	}
}
