/*******************************************************************************
 * Copyright (C) 2005, 2012  Wolfgang Schramm and Contributors
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
package net.tourbook.photo;

import java.util.ArrayList;
import java.util.Collection;

import net.tourbook.photo.internal.gallery.MT20.GalleryMT20Item;

import org.eclipse.jface.viewers.ISelection;

/**
 * This class is used to identify photos in a structured selection and is needed to identify an
 * empty selection !!!.
 */
public class PhotoSelection implements ISelection {

	public ArrayList<Photo>				galleryPhotos;
	public Collection<GalleryMT20Item>	galleryItems;
	public int[]						selectionIndices;
	public boolean						isLinkPhotoDisplayed;

	public PhotoSelection(	final ArrayList<Photo> allGalleryPhotos,
							final Collection<GalleryMT20Item> allGalleryItems,
							final int[] allSelectionIndices,
							final boolean allIsLinkPhotoDisplayed) {

		galleryPhotos = allGalleryPhotos;
		galleryItems = allGalleryItems;
		selectionIndices = allSelectionIndices;
		isLinkPhotoDisplayed = allIsLinkPhotoDisplayed;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public String toString() {

		final int maxLen = 5;

		return "PhotoSelection [galleryPhotos=" //$NON-NLS-1$
				+ (galleryPhotos != null ? galleryPhotos.subList(0, Math.min(galleryPhotos.size(), maxLen)) : null)
				+ "]"; //$NON-NLS-1$
	}

}
