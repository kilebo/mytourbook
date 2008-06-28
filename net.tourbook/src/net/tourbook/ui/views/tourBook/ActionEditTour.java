/*******************************************************************************
 * Copyright (C) 2005, 2007  Wolfgang Schramm and Contributors
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
package net.tourbook.ui.views.tourBook;

import net.tourbook.Messages;
import net.tourbook.plugin.TourbookPlugin;
import net.tourbook.tour.TourManager;

import org.eclipse.jface.action.Action;

public class ActionEditTour extends Action {

	private TourBookView	fTourView;

	public ActionEditTour(TourBookView tourBookView) {

		fTourView = tourBookView;

		setText(Messages.App_Action_edit_tour);
		setImageDescriptor(TourbookPlugin.getImageDescriptor(Messages.Image__edit_tour));
		setDisabledImageDescriptor(TourbookPlugin.getImageDescriptor(Messages.Image__edit_tour_disabled));

		setEnabled(false);
	}

	@Override
	public void run() {

		Long tourId = fTourView.getActiveTourId();

		if (tourId != null) {
			TourManager.getInstance().openTourInEditor(tourId);
		}
	}

}
