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
package net.tourbook.ui.views;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Set;

import net.tourbook.Messages;
import net.tourbook.chart.SelectionChartXSliderPosition;
import net.tourbook.data.TourData;
import net.tourbook.data.TourTag;
import net.tourbook.data.TourType;
import net.tourbook.plugin.TourbookPlugin;
import net.tourbook.preferences.ITourbookPreferences;
import net.tourbook.tour.ITourPropertyListener;
import net.tourbook.tour.SelectionActiveEditor;
import net.tourbook.tour.SelectionTourData;
import net.tourbook.tour.SelectionTourId;
import net.tourbook.tour.TourChart;
import net.tourbook.tour.TourEditor;
import net.tourbook.tour.TourManager;
import net.tourbook.ui.ActionModifyColumns;
import net.tourbook.ui.ColumnManager;
import net.tourbook.ui.ITourViewer;
import net.tourbook.ui.TableColumnDefinition;
import net.tourbook.ui.TableColumnFactory;
import net.tourbook.ui.UI;
import net.tourbook.ui.views.tourCatalog.SelectionTourCatalogView;
import net.tourbook.ui.views.tourCatalog.TVICatalogComparedTour;
import net.tourbook.util.PixelConverter;
import net.tourbook.util.PostSelectionProvider;

import org.aspencloud.cdatetime.CDT;
import org.aspencloud.cdatetime.CDateTime;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.TableViewerFocusCellManager;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.part.ViewPart;

// author: Wolfgang Schramm
// create: 24.08.2007

public class TourPropertiesView extends ViewPart implements ITourViewer {

	public static final String		ID						= "net.tourbook.views.TourPropertiesView";		//$NON-NLS-1$

	private static final String		MEMENTO_SELECTED_TAB	= "tourProperties.selectedTab";				//$NON-NLS-1$

	private static IMemento			fSessionMemento;
	private CTabFolder				fTabFolder;

	private Label					fLblDate;

	private Label					fLblDatapoints;
	private Label					fLblDeviceName;
	private Label					fLblDrivingTime;
	private Label					fLblRecordingTime;
	private Label					fLblStartTime;
	private Label					fLblTourTags;
	private Label					fLblTourType;
	private Text					fTextTitle;

	private Text					fTextStartLocation;
	private Text					fTextEndLocation;
	private Text					fTextDescription;
	private PostSelectionProvider	fPostSelectionProvider;

	private ISelectionListener		fPostSelectionListener;
	private IPartListener2			fPartListener;
	private IPropertyChangeListener	fPrefChangeListener;
	public Calendar					fCalendar				= GregorianCalendar.getInstance();

	private DateFormat				fTimeFormatter			= DateFormat.getTimeInstance(DateFormat.SHORT);

	private DateFormat				fDurationFormatter		= DateFormat.getTimeInstance(DateFormat.SHORT,
																	Locale.GERMAN);
	private NumberFormat			fNumberFormatter		= NumberFormat.getNumberInstance();

	/**
	 * contains the tour editor when the tour is opened in an editor or <code>null</code> when the
	 * tour is not from an editor
	 */
	private TourEditor				fTourEditor;

	private TourChart				fTourChart;
	private TourData				fTourData;

	private ITourPropertyListener	fTourPropertyListener;

	private ScrolledComposite		fScrolledContainer;

	private Composite				fContentContainer;
	private Composite				fDataViewerContainer;

	private TableViewer				fDataViewer;
	private ColumnManager			fColumnManager;
	private TableColumnDefinition	fColDefAltitude;

	/*
	 * data series which are displayed in the viewer
	 */
	private int[]					fSerieTime;

	private int[]					fSerieDistance;
	private int[]					fSerieAltitude;
	private int[]					fSerieTemperature;
	private int[]					fSerieCadence;
	private int[]					fSerieGradient;
	private int[]					fSerieSpeed;
	private int[]					fSeriePace;
	private int[]					fSeriePower;
	private int[]					fSeriePulse;
	private double[]				fSerieLatitude;
	private double[]				fSerieLongitude;

	private CDateTime				fTourDate;

	private class TimeSlice {
		int	serieIndex;

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof TimeSlice)) {
				return false;
			}
			final TimeSlice other = (TimeSlice) obj;
			if (!getOuterType().equals(other.getOuterType())) {
				return false;
			}
			if (serieIndex != other.serieIndex) {
				return false;
			}
			return true;
		}

		private TourPropertiesView getOuterType() {
			return TourPropertiesView.this;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + serieIndex;
			return result;
		}

	}

	private class TourDataContentProvider implements IStructuredContentProvider {

		public TourDataContentProvider() {}

		public void dispose() {}

		public Object[] getElements(final Object parent) {

			if (fTourData == null) {
				return new Object[0];
			}

			fSerieTime = fTourData.timeSerie;

			fSerieDistance = fTourData.getDistanceSerie();
			fSerieAltitude = fTourData.getAltitudeSerie();
			fSerieTemperature = fTourData.getTemperatureSerie();
			fSerieGradient = fTourData.getGradientSerie();
			fSerieSpeed = fTourData.getSpeedSerie();
			fSeriePace = fTourData.getPaceSerie();
			fSeriePower = fTourData.getPowerSerie();

			fSerieCadence = fTourData.cadenceSerie;
			fSeriePulse = fTourData.pulseSerie;

			fSerieLatitude = fTourData.latitudeSerie;
			fSerieLongitude = fTourData.longitudeSerie;

			final TimeSlice[] dataElements = new TimeSlice[fTourData.timeSerie.length];
			TimeSlice timeSlice;

			/*
			 * create viewer elements (time slices)
			 */
			for (int serieIndex = 0; serieIndex < dataElements.length; serieIndex++) {

				dataElements[serieIndex] = timeSlice = new TimeSlice();

				timeSlice.serieIndex = serieIndex;
			}

			return (dataElements);
		}

		public void inputChanged(final Viewer v, final Object oldInput, final Object newInput) {}
	}

	private void addPartListener() {

		// set the part listener
		fPartListener = new IPartListener2() {
			public void partActivated(final IWorkbenchPartReference partRef) {}

			public void partBroughtToTop(final IWorkbenchPartReference partRef) {}

			public void partClosed(final IWorkbenchPartReference partRef) {
				if (ID.equals(partRef.getId())) {
					// keep settings for this part
					saveSettings();
				}
			}

			public void partDeactivated(final IWorkbenchPartReference partRef) {}

			public void partHidden(final IWorkbenchPartReference partRef) {}

			public void partInputChanged(final IWorkbenchPartReference partRef) {}

			public void partOpened(final IWorkbenchPartReference partRef) {}

			public void partVisible(final IWorkbenchPartReference partRef) {}
		};
		// register the listener in the page
		getSite().getPage().addPartListener(fPartListener);
	}

	private void addPrefListener() {

		fPrefChangeListener = new Preferences.IPropertyChangeListener() {
			public void propertyChange(final Preferences.PropertyChangeEvent event) {

				if (fTourData == null) {
					return;
				}

				final String property = event.getProperty();

				if (property.equals(ITourbookPreferences.MEASUREMENT_SYSTEM)) {

					// measurement system has changed

					UI.updateUnits();

					fColumnManager.saveState(fSessionMemento);
					fColumnManager.clearColumns();
					defineViewerColumns(fDataViewerContainer);

					recreateViewer();

				} else if (property.equals(ITourbookPreferences.TOUR_TYPE_LIST_IS_MODIFIED)) {

					// reload tour data

					fTourData = TourManager.getInstance().getTourData(fTourData.getTourId());
					updateTourProperties(fTourData);
				}
			}
		};
		TourbookPlugin.getDefault().getPluginPreferences().addPropertyChangeListener(fPrefChangeListener);
	}

	/**
	 * listen for events when a tour is selected
	 */
	private void addSelectionListener() {

		fPostSelectionListener = new ISelectionListener() {
			public void selectionChanged(final IWorkbenchPart part, final ISelection selection) {

				if (part == TourPropertiesView.this) {
					return;
				}
				onChangeSelection(selection);
			}
		};
		getSite().getPage().addPostSelectionListener(fPostSelectionListener);
	}

	private void addTourPropertyListener() {

		fTourPropertyListener = new ITourPropertyListener() {
			@SuppressWarnings("unchecked")
			public void propertyChanged(final int propertyId, final Object propertyData) {

				if (fTourData == null) {
					return;
				}

				if (propertyId == TourManager.TOUR_PROPERTIES_CHANGED) {

					// get modified tours
					final ArrayList<TourData> modifiedTours = (ArrayList<TourData>) propertyData;
					final long displayedTourId = fTourData.getTourId();

					for (final TourData tourData : modifiedTours) {
						if (tourData.getTourId() == displayedTourId) {
							updateTourProperties(tourData);
							return;
						}
					}

				} else if (propertyId == TourManager.TAG_STRUCTURE_CHANGED) {

//					if (isTagListModified(fTourData)) {
//						updateTourProperties(fTourData);
//					}

					fTourData = TourManager.getInstance().getTourData(fTourData.getTourId());
					updateTourProperties(fTourData);
				}
			}
		};

		TourManager.getInstance().addPropertyListener(fTourPropertyListener);
	}

	private void createActions() {

		final ActionModifyColumns actionModifyColumns = new ActionModifyColumns(this);

		/*
		 * fill view menu
		 */
		final IMenuManager menuMgr = getViewSite().getActionBars().getMenuManager();
		menuMgr.add(actionModifyColumns);
	}

	/**
	 * @param parent
	 */
	private void createDataViewer(final Composite parent) {

		// parent.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_YELLOW));

		// table
		final Table table = new Table(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.MULTI);

		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		table.addTraverseListener(new TraverseListener() {
			public void keyTraversed(final TraverseEvent e) {
				e.doit = e.keyCode != SWT.CR; // vetoes all CR traversals
			}
		});

		fDataViewer = new TableViewer(table);

		final TableViewerFocusCellManager focusCellManager = new TableViewerFocusCellManager(fDataViewer,
				new FocusCellOwnerDrawHighlighter(fDataViewer));

		final ColumnViewerEditorActivationStrategy actSupport = new ColumnViewerEditorActivationStrategy(fDataViewer) {
			@Override
			protected boolean isEditorActivationEvent(final ColumnViewerEditorActivationEvent event) {
				return event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL
						|| event.eventType == ColumnViewerEditorActivationEvent.MOUSE_CLICK_SELECTION
						|| (event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED && event.keyCode == SWT.CR)
						|| event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC;
			}
		};

		TableViewerEditor.create(fDataViewer, //
				focusCellManager,
				actSupport,
				ColumnViewerEditor.TABBING_HORIZONTAL
						| ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR
						| ColumnViewerEditor.TABBING_VERTICAL
						| ColumnViewerEditor.KEYBOARD_ACTIVATION);

		/*
		 * create editing support after the viewer is created and before the columns are created
		 */
		createEditingSupport();

		fColumnManager.createColumns();

		fDataViewer.setContentProvider(new TourDataContentProvider());
		fDataViewer.setUseHashlookup(true);

		fDataViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(final SelectionChangedEvent event) {
				final StructuredSelection selection = (StructuredSelection) event.getSelection();
				if (selection != null) {
					fireSliderPosition(selection);
				}
			}
		});
	}

	private void createEditingSupport() {

		final TextCellEditor cellEditor = new TextCellEditor(fDataViewer.getTable());

		fColDefAltitude.setEditingSupport(new EditingSupport(fDataViewer) {

			@Override
			protected boolean canEdit(final Object element) {
				if (fTourEditor == null || fSerieAltitude == null) {
					return false;
				}
				return true;
			}

			@Override
			protected CellEditor getCellEditor(final Object element) {
				return cellEditor;
			}

			@Override
			protected Object getValue(final Object element) {
				final TimeSlice timeSlice = (TimeSlice) element;
				return new Integer(fSerieAltitude[timeSlice.serieIndex]).toString();
			}

			@Override
			protected void setValue(final Object element, final Object value) {

				if (value instanceof String) {

					try {
						final TimeSlice timeSlice = (TimeSlice) element;
						final int newAltitude = Integer.parseInt((String) value);

						// value has changed, mark editor dirty
						if (newAltitude != fSerieAltitude[timeSlice.serieIndex]) {

							fSerieAltitude[timeSlice.serieIndex] = newAltitude;

							final TourData tourData = fTourEditor.getTourChart().getTourData();
							tourData.clearComputedSeries();

							// set tour modified and update the chart, computed data will be recomputed
							fTourEditor.setTourDirtyAndUpdateChart();

							reloadViewer();

							fPostSelectionProvider.setSelection(new SelectionTourData(null, tourData, true));
						}

					} catch (final Exception e) {
						// ignore invalid characters
					}
				}
			}
		});
	}

	@Override
	public void createPartControl(final Composite parent) {

		// define all columns
		fColumnManager = new ColumnManager(this, fSessionMemento);
		defineViewerColumns(parent);

		createUI(parent);

		addSelectionListener();
		addPartListener();
		addTourPropertyListener();
		addPrefListener();

		createActions();

		// this part is a selection provider
		getSite().setSelectionProvider(fPostSelectionProvider = new PostSelectionProvider());

		restoreState(fSessionMemento);

		enableControls();

		// show data from last selection
		final ISelection workbenchSelection = getSite().getWorkbenchWindow().getSelectionService().getSelection();
		onChangeSelection(workbenchSelection);
	}

	private Composite createTabInfo(final Composite parent) {

		Label label;
		final PixelConverter pixelConverter = new PixelConverter(parent);

		final ScrolledComposite scrolledContainer = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL);

		scrolledContainer.setExpandVertical(true);
		scrolledContainer.setExpandHorizontal(true);
		final Composite locationContainer;

		locationContainer = new Composite(scrolledContainer, SWT.NONE);
		locationContainer.setLayout(new GridLayout(2, false));
		locationContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		scrolledContainer.setContent(locationContainer);
		scrolledContainer.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(final ControlEvent e) {
			// disabled because a long description text in one line is not wrapped
//				scrolledContainer.setMinSize(locationContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			}
		});

		// title
		label = new Label(locationContainer, SWT.NONE);
		label.setText(Messages.Tour_Properties_Label_tour_title);

		fTextTitle = new Text(locationContainer, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(fTextTitle);
		fTextTitle.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(final KeyEvent e) {
				onChangeContent();
			}
		});

		// description
		label = new Label(locationContainer, SWT.NONE);
		label.setText(Messages.Tour_Properties_Label_description);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.BEGINNING).applyTo(label);

		fTextDescription = new Text(locationContainer, SWT.BORDER | SWT.WRAP | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		GridDataFactory//
		.fillDefaults()
				.grab(true, true)
				.hint(SWT.DEFAULT, pixelConverter.convertHeightInCharsToPixels(2))
				.applyTo(fTextDescription);

		fTextDescription.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(final KeyEvent e) {
				onChangeContent();
			}
		});

		// start location
		label = new Label(locationContainer, SWT.NONE);
		label.setText(Messages.Tour_Properties_Label_start_location);

		fTextStartLocation = new Text(locationContainer, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(fTextStartLocation);
		fTextStartLocation.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(final KeyEvent e) {
				onChangeContent();
			}
		});

		// end location
		label = new Label(locationContainer, SWT.NONE);
		label.setText(Messages.Tour_Properties_Label_end_location);

		fTextEndLocation = new Text(locationContainer, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(fTextEndLocation);
		fTextEndLocation.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(final KeyEvent e) {
				onChangeContent();
			}
		});

		return scrolledContainer;
	}

	private Control createTabRawData(final Composite parent) {

		fDataViewerContainer = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().applyTo(fDataViewerContainer);

		createDataViewer(fDataViewerContainer);

		return fDataViewerContainer;
	}

	private Composite createTabTourData(final Composite parent) {

		Label label;

		fScrolledContainer = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL);
		fScrolledContainer.setExpandVertical(true);
		fScrolledContainer.setExpandHorizontal(true);

		fContentContainer = new Composite(fScrolledContainer, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(fContentContainer);
		GridLayoutFactory.swtDefaults().numColumns(2).spacing(10, 5).applyTo(fContentContainer);

		fScrolledContainer.setContent(fContentContainer);
		fScrolledContainer.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(final ControlEvent e) {
				onResizeContainer();
			}
		});

		{
			// tour date
			label = new Label(fContentContainer, SWT.NONE);
			label.setText(Messages.Tour_Properties_Label_tour_date);
			GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.BEGINNING).applyTo(label);
			fLblDate = new Label(fContentContainer, SWT.WRAP);
			GridDataFactory.fillDefaults().grab(true, false).applyTo(fLblDate);

			// tour date 2
			label = new Label(fContentContainer, SWT.NONE);
			label.setText(Messages.Tour_Properties_Label_tour_date);
			fTourDate = new CDateTime(fContentContainer, CDT.BORDER | CDT.DATE_LONG | CDT.DROP_DOWN);
			GridDataFactory.fillDefaults().grab(true, false).applyTo(fTourDate);
			// set full date pattern
			fTourDate.setPattern(((SimpleDateFormat) SimpleDateFormat.getDateInstance(DateFormat.FULL)).toLocalizedPattern());

			fTourDate.setNullText(UI.EMPTY_STRING);

			// start time
			label = new Label(fContentContainer, SWT.NONE);
			label.setText(Messages.Tour_Properties_Label_start_time);
			fLblStartTime = new Label(fContentContainer, SWT.WRAP);
			GridDataFactory.fillDefaults().grab(true, false).applyTo(fLblStartTime);

			// recording time
			label = new Label(fContentContainer, SWT.NONE);
			label.setText(Messages.Tour_Properties_Label_recording_time);
			fLblRecordingTime = new Label(fContentContainer, SWT.WRAP);
			GridDataFactory.fillDefaults().grab(true, false).applyTo(fLblRecordingTime);

			// driving time
			label = new Label(fContentContainer, SWT.NONE);
			label.setText(Messages.Tour_Properties_Label_driving_time);
			fLblDrivingTime = new Label(fContentContainer, SWT.WRAP);
			GridDataFactory.fillDefaults().grab(true, false).applyTo(fLblDrivingTime);

			// tour type
			label = new Label(fContentContainer, SWT.NONE);
			label.setText(Messages.Tour_Properties_Label_device_name);
			fLblDeviceName = new Label(fContentContainer, SWT.WRAP);
			GridDataFactory.fillDefaults().grab(true, false).applyTo(fLblDeviceName);

			// tour type
			label = new Label(fContentContainer, SWT.NONE);
			label.setText(Messages.Tour_Properties_Label_tour_type);
			fLblTourType = new Label(fContentContainer, SWT.WRAP);
			GridDataFactory.fillDefaults().grab(true, false).applyTo(fLblTourType);

			// tags
			label = new Label(fContentContainer, SWT.NONE);
			label.setText(Messages.Tour_Properties_Label_tour_tag);
			GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.BEGINNING).applyTo(label);

			fLblTourTags = new Label(fContentContainer, SWT.WRAP);
			GridDataFactory.fillDefaults()//
					.grab(true, false)
					.hint(10, SWT.DEFAULT)
					.applyTo(fLblTourTags);

			// data points
			label = new Label(fContentContainer, SWT.NONE);
			label.setText(Messages.Tour_Properties_Label_datapoints);
			fLblDatapoints = new Label(fContentContainer, SWT.WRAP);
			GridDataFactory.fillDefaults().grab(true, false).applyTo(fLblDatapoints);
		}

		return fScrolledContainer;
	}

	private void createUI(final Composite parent) {

		fTabFolder = new CTabFolder(parent, SWT.FLAT | SWT.BOTTOM);

		final CTabItem fTabItemLocation = new CTabItem(fTabFolder, SWT.FLAT);
		fTabItemLocation.setText(Messages.Tour_Properties_tabLabel_info);
		fTabItemLocation.setControl(createTabInfo(fTabFolder));

		final CTabItem fTabItemTime = new CTabItem(fTabFolder, SWT.FLAT);
		fTabItemTime.setText(Messages.Tour_Properties_tabLabel_time);
		fTabItemTime.setControl(createTabTourData(fTabFolder));

		final CTabItem fTabItemTourData = new CTabItem(fTabFolder, SWT.FLAT);
		fTabItemTourData.setText(Messages.Tour_Properties_tabLabel_tour_data);
		fTabItemTourData.setControl(createTabRawData(fTabFolder));

	}

	private void defineViewerColumns(final Composite parent) {

		final PixelConverter pixelConverter = new PixelConverter(parent);

		TableColumnDefinition colDef;

		/*
		 * 1. column will be hidden because the alignment for the first column is always to the left
		 */
		colDef = TableColumnFactory.FIRST_COLUMN.createColumn(fColumnManager, pixelConverter);
		colDef.setIsDefaultColumn();
		colDef.setCanModifyVisibility(false);
		colDef.setIsColumnMoveable(false);
		colDef.setHideColumn();
		colDef.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(final ViewerCell cell) {}
		});

		/*
		 * column: #
		 */
		colDef = TableColumnFactory.SEQUENCE.createColumn(fColumnManager, pixelConverter);
		colDef.setIsDefaultColumn();
		colDef.setCanModifyVisibility(false);
		colDef.setIsColumnMoveable(false);
		colDef.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(final ViewerCell cell) {

				cell.setText(Integer.toString(((TimeSlice) cell.getElement()).serieIndex));

				cell.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
			}
		});

		/*
		 * column: time
		 */
		colDef = TableColumnFactory.TOUR_TIME.createColumn(fColumnManager, pixelConverter);
		colDef.setIsDefaultColumn();
		colDef.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(final ViewerCell cell) {

				if (fSerieTime != null) {
					final TimeSlice timeSlice = (TimeSlice) cell.getElement();
					cell.setText(Integer.toString(fSerieTime[timeSlice.serieIndex]));
				}
			}
		});

		/*
		 * column: distance
		 */
		colDef = TableColumnFactory.DISTANCE.createColumn(fColumnManager, pixelConverter);
		colDef.setIsDefaultColumn();
		colDef.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(final ViewerCell cell) {

				if (fSerieDistance != null) {
					final TimeSlice timeSlice = (TimeSlice) cell.getElement();
					final int distance = fSerieDistance[timeSlice.serieIndex];
					fNumberFormatter.setMinimumFractionDigits(3);
					fNumberFormatter.setMaximumFractionDigits(3);
					cell.setText(fNumberFormatter.format((float) distance / 1000));
				}
			}
		});

		/*
		 * column: altitude
		 */
		fColDefAltitude = colDef = TableColumnFactory.ALTITUDE.createColumn(fColumnManager, pixelConverter);
		colDef.setIsDefaultColumn();
		colDef.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(final ViewerCell cell) {
				if (fSerieAltitude != null) {
					final TimeSlice timeSlice = (TimeSlice) cell.getElement();
					cell.setText(Integer.toString(fSerieAltitude[timeSlice.serieIndex]));
				}
			}
		});

		/*
		 * column: pulse
		 */
		colDef = TableColumnFactory.PULSE.createColumn(fColumnManager, pixelConverter);
		colDef.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(final ViewerCell cell) {
				if (fSeriePulse != null) {
					final TimeSlice timeSlice = (TimeSlice) cell.getElement();
					cell.setText(Integer.toString(fSeriePulse[timeSlice.serieIndex]));
				}
			}
		});

		/*
		 * column: temperature
		 */
		colDef = TableColumnFactory.TEMPERATURE.createColumn(fColumnManager, pixelConverter);
		colDef.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(final ViewerCell cell) {
				if (fSerieTemperature != null) {
					final TimeSlice timeSlice = (TimeSlice) cell.getElement();
					cell.setText(Integer.toString(fSerieTemperature[timeSlice.serieIndex]));
				}
			}
		});

		/*
		 * column: cadence
		 */
		colDef = TableColumnFactory.CADENCE.createColumn(fColumnManager, pixelConverter);
		colDef.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(final ViewerCell cell) {
				if (fSerieCadence != null) {
					final TimeSlice timeSlice = (TimeSlice) cell.getElement();
					cell.setText(Integer.toString(fSerieCadence[timeSlice.serieIndex]));
				}
			}
		});

		/*
		 * column: gradient
		 */
		colDef = TableColumnFactory.GRADIENT.createColumn(fColumnManager, pixelConverter);
		colDef.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(final ViewerCell cell) {
				if (fSerieGradient != null) {
					final TimeSlice timeSlice = (TimeSlice) cell.getElement();
					fNumberFormatter.setMinimumFractionDigits(1);
					fNumberFormatter.setMaximumFractionDigits(1);

					cell.setText(fNumberFormatter.format((float) fSerieGradient[timeSlice.serieIndex] / 10));
				}
			}
		});

		/*
		 * column: speed
		 */
		colDef = TableColumnFactory.SPEED.createColumn(fColumnManager, pixelConverter);
		colDef.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(final ViewerCell cell) {
				if (fSerieGradient != null) {
					final TimeSlice timeSlice = (TimeSlice) cell.getElement();
					fNumberFormatter.setMinimumFractionDigits(1);
					fNumberFormatter.setMaximumFractionDigits(1);

					cell.setText(fNumberFormatter.format((float) fSerieSpeed[timeSlice.serieIndex] / 10));

				}
			}
		});

		/*
		 * column: pace
		 */
		colDef = TableColumnFactory.PACE.createColumn(fColumnManager, pixelConverter);
		colDef.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(final ViewerCell cell) {
				if (fSerieGradient != null) {
					final TimeSlice timeSlice = (TimeSlice) cell.getElement();
					fNumberFormatter.setMinimumFractionDigits(1);
					fNumberFormatter.setMaximumFractionDigits(1);

					cell.setText(fNumberFormatter.format((float) fSeriePace[timeSlice.serieIndex] / 10));

				}
			}
		});

		/*
		 * column: power
		 */
		colDef = TableColumnFactory.POWER.createColumn(fColumnManager, pixelConverter);
		colDef.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(final ViewerCell cell) {
				if (fSerieGradient != null) {
					final TimeSlice timeSlice = (TimeSlice) cell.getElement();
					cell.setText(Integer.toString(fSeriePower[timeSlice.serieIndex]));

				}
			}
		});

		/*
		 * column: longitude
		 */
		colDef = TableColumnFactory.LONGITUDE.createColumn(fColumnManager, pixelConverter);
		colDef.setIsDefaultColumn();
		colDef.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(final ViewerCell cell) {
				if (fSerieLongitude != null) {

					final TimeSlice timeSlice = (TimeSlice) cell.getElement();
					cell.setText(Double.toString(fSerieLongitude[timeSlice.serieIndex]));
				}
			}
		});

		/*
		 * column: latitude
		 */
		colDef = TableColumnFactory.LATITUDE.createColumn(fColumnManager, pixelConverter);
		colDef.setIsDefaultColumn();
		colDef.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(final ViewerCell cell) {
				if (fSerieLatitude != null) {

					final TimeSlice timeSlice = (TimeSlice) cell.getElement();
					cell.setText(Double.toString(fSerieLatitude[timeSlice.serieIndex]));
				}
			}
		});

	}

	@Override
	public void dispose() {

		final IWorkbenchPage page = getSite().getPage();

		page.removePostSelectionListener(fPostSelectionListener);
		page.removePartListener(fPartListener);

		TourbookPlugin.getDefault().getPluginPreferences().removePropertyChangeListener(fPrefChangeListener);

		TourManager.getInstance().removePropertyListener(fTourPropertyListener);

		super.dispose();
	}

	/**
	 * enable controls when the data are from an editor
	 */
	private void enableControls() {

		final boolean isEditor = fTourEditor != null;

		fTextTitle.setEnabled(isEditor);
		fTextStartLocation.setEnabled(isEditor);
		fTextEndLocation.setEnabled(isEditor);
		fTextDescription.setEnabled(isEditor);
	}

	/**
	 * select the chart slider(s) according to the selected marker(s)
	 */
	private void fireSliderPosition(final StructuredSelection selection) {

		if (fTourChart == null) {

			final TourChart tourChart = TourManager.getInstance().getActiveTourChart();

			if (tourChart == null || tourChart.isDisposed()) {
				return;
			} else {
				fTourChart = tourChart;
			}
		}

		final Object[] selectedData = selection.toArray();

		if (selectedData.length > 1) {

			// two or more data are selected, set the 2 sliders to the first and last selected data

			fPostSelectionProvider.setSelection(new SelectionChartXSliderPosition(fTourChart,
					((TimeSlice) selectedData[0]).serieIndex,
					((TimeSlice) selectedData[selectedData.length - 1]).serieIndex));

		} else if (selectedData.length > 0) {

			// one data is selected

			fPostSelectionProvider.setSelection(new SelectionChartXSliderPosition(fTourChart,
					((TimeSlice) selectedData[0]).serieIndex,
					SelectionChartXSliderPosition.IGNORE_SLIDER_POSITION));
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object getAdapter(final Class adapter) {

		if (adapter == ColumnViewer.class) {
			return fDataViewer;
		}

		return Platform.getAdapterManager().getAdapter(this, adapter);
	}

	public ColumnManager getColumnManager() {
		return fColumnManager;
	}

	public ColumnViewer getViewer() {
		return fDataViewer;
	}

	@Override
	public void init(final IViewSite site, final IMemento memento) throws PartInitException {

		super.init(site, memento);

		// set the session memento if it's not yet set
		if (fSessionMemento == null) {
			fSessionMemento = memento;
		}
	}

	private void onChangeContent() {

		if (fTourEditor == null) {
			return;
		}

		// set changed data
		fTourData.setTourTitle(fTextTitle.getText());
		fTourData.setTourStartPlace(fTextStartLocation.getText());
		fTourData.setTourEndPlace(fTextEndLocation.getText());
		fTourData.setTourDescription(fTextDescription.getText());

		fTourEditor.setTourDirtyAndUpdateChart();
	}

	private void onChangeSelection(final ISelection selection) {

		if (selection instanceof SelectionTourData) {

			final SelectionTourData selectionTourData = (SelectionTourData) selection;
			final TourData tourData = selectionTourData.getTourData();
			if (tourData == null) {
				fTourEditor = null;
				fTourChart = null;
			} else {

				final TourChart tourChart = selectionTourData.getTourChart();

//				// prevent loading the same tour
//				if (tourChart != null && fTourChart != null && fTourChart.getTourData() == tourData) {
//					return;
//				}

				fTourEditor = null;
				fTourChart = tourChart;
				updateTourProperties(tourData);
			}

		} else if (selection instanceof SelectionTourId) {

			final Long tourId = ((SelectionTourId) selection).getTourId();

			onSelectTourId(tourId);

		} else if (selection instanceof SelectionActiveEditor) {

			final IEditorPart editor = ((SelectionActiveEditor) selection).getEditor();

			if (editor instanceof TourEditor) {
				final TourEditor tourEditor = (TourEditor) editor;

//				// prevent loading the same tour
//				// this is disabled because the data can be changed
//				if (tourEditor == fTourEditor) {
//					return;
//				}

				fTourEditor = tourEditor;
				fTourChart = fTourEditor.getTourChart();

				final TourData tourData = fTourChart.getTourData();
				updateTourProperties(tourData);
			}

		} else if (selection instanceof StructuredSelection) {

			final Object firstElement = ((StructuredSelection) selection).getFirstElement();
			if (firstElement instanceof TVICatalogComparedTour) {
				onSelectTourId(((TVICatalogComparedTour) firstElement).getTourId());
			}

		} else if (selection instanceof SelectionTourCatalogView) {
			// this selection is overwritten by another selection
//			onSelectTourId(((SelectionTourCatalogView) selection).getRefItem().getTourId());
		}
	}

	private void onResizeContainer() {

		fScrolledContainer.setMinSize(fContentContainer.computeSize(fScrolledContainer.getClientArea().width,
				SWT.DEFAULT));

	}

	private void onSelectTourId(final Long tourId) {

		// don't reload the same tour
		if (fTourData != null) {
			if (fTourData.getTourId().equals(tourId)) {
				return;
			}
		}

		final TourData tourData = TourManager.getInstance().getTourData(tourId);

		if (tourData != null) {
			fTourEditor = null;
			fTourChart = null;
			updateTourProperties(tourData);
		}
	}

	public void recreateViewer() {

		// preserve selection and focus
		final ISelection selection = fDataViewer.getSelection();
		final Table table = fDataViewer.getTable();
		final boolean isFocus = table.isFocusControl();

		fDataViewerContainer.setRedraw(false);
		{
			table.dispose();

			createDataViewer(fDataViewerContainer);
			fDataViewerContainer.layout();

			// update the viewer
			fDataViewer.setInput(new Object());

		}
		fDataViewerContainer.setRedraw(true);

		// restore selection and focus
		Display.getCurrent().asyncExec(new Runnable() {
			public void run() {
				fDataViewer.setSelection(selection, true);
				if (isFocus) {
					fDataViewer.getTable().setFocus();
				}
			}
		});
	}

	/**
	 * reload the content of the tag viewer
	 */
	public void reloadViewer() {

		final ISelection selection = fDataViewer.getSelection();

		final Table table = fDataViewer.getTable();
		table.setRedraw(false);
		{
			// update the viewer
			fDataViewer.setInput(new Object());

			fDataViewer.setSelection(selection, true);
		}
		table.setRedraw(true);

	}

	private void restoreState(final IMemento memento) {

		if (memento == null) {

			// memento is not set, set defaults

			fTabFolder.setSelection(0);

		} else {

			// restore from memento

			// select tab
			final Integer selectedTab = memento.getInteger(MEMENTO_SELECTED_TAB);
			if (selectedTab != null) {
				fTabFolder.setSelection(selectedTab);
			} else {
				fTabFolder.setSelection(0);
			}

			restoreViewerSettings(memento);
		}
	}

	private void restoreViewerSettings(final IMemento memento) {

		if (memento == null) {
			return;
		}

		// hide first column, this is a hack to align the "first" visible column to right
		fDataViewer.getTable().getColumn(0).setWidth(0);
	}

	private void saveSettings() {
		fSessionMemento = XMLMemento.createWriteRoot("TourPropertiesView"); //$NON-NLS-1$
		saveState(fSessionMemento);
	}

	@Override
	public void saveState(final IMemento memento) {

		// save selected tab
		memento.putInteger(MEMENTO_SELECTED_TAB, fTabFolder.getSelectionIndex());

		saveViewerSettings(memento);
	}

	private void saveViewerSettings(final IMemento memento) {

		if (memento == null) {
			return;
		}

		fColumnManager.saveState(memento);
	}

	@Override
	public void setFocus() {
		fDataViewer.getTable().setFocus();
	}

	private void updateTourProperties(final TourData tourData) {

		enableControls();

		// keep reference
		fTourData = tourData;

		/*
		 * location: time
		 */
		// tour date
		fLblDate.setText(TourManager.getTourDateFull(tourData));
		fLblDate.pack(true);

		fTourDate.setSelection(TourManager.getTourDate(tourData).toDate());

		// start time
		fCalendar.set(0, 0, 0, tourData.getStartHour(), tourData.getStartMinute(), 0);
		fLblStartTime.setText(fTimeFormatter.format(fCalendar.getTime()));

		// recording time
		final int recordingTime = tourData.getTourRecordingTime();
		if (recordingTime == 0) {
			fLblRecordingTime.setText(UI.EMPTY_STRING);
		} else {
			fCalendar.set(0, 0, 0, recordingTime / 3600, ((recordingTime % 3600) / 60), ((recordingTime % 3600) % 60));

			fLblRecordingTime.setText(fDurationFormatter.format(fCalendar.getTime()));
		}

		// driving time
		final int drivingTime = tourData.getTourDrivingTime();
		if (drivingTime == 0) {
			fLblDrivingTime.setText(UI.EMPTY_STRING);
		} else {
			fCalendar.set(0, 0, 0, drivingTime / 3600, ((drivingTime % 3600) / 60), ((drivingTime % 3600) % 60));

			fLblDrivingTime.setText(fDurationFormatter.format(fCalendar.getTime()));
		}

		// data points
		final int[] timeSerie = tourData.timeSerie;
		if (timeSerie == null) {
			fLblDatapoints.setText(UI.EMPTY_STRING);
		} else {
			final int dataPoints = timeSerie.length;
			fLblDatapoints.setText(Integer.toString(dataPoints));
		}

		// tour type
		final TourType tourType = tourData.getTourType();
		if (tourType == null) {
			fLblTourType.setText(UI.EMPTY_STRING);
		} else {
			fLblTourType.setText(tourType.getName());
		}

		// tour tags
		final Set<TourTag> tourTags = tourData.getTourTags();

		if (tourTags == null || tourTags.size() == 0) {

			fLblTourTags.setText(UI.EMPTY_STRING);

		} else {

			// sort tour tags by name
			final ArrayList<TourTag> tourTagList = new ArrayList<TourTag>(tourTags);
			Collections.sort(tourTagList, new Comparator<TourTag>() {
				public int compare(final TourTag tt1, final TourTag tt2) {
					return tt1.getTagName().compareTo(tt2.getTagName());
				}
			});

			final StringBuilder sb = new StringBuilder();
			int index = 0;
			for (final TourTag tourTag : tourTagList) {

				if (index > 0) {
					sb.append(", "); //$NON-NLS-1$
				}

				sb.append(tourTag.getTagName());

				index++;
			}
			fLblTourTags.setText(sb.toString());
		}

		// device name
		fLblDeviceName.setText(tourData.getDeviceName());

		/*
		 * tab: location
		 */
		fTextTitle.setText(fTourData.getTourTitle());
		fTextStartLocation.setText(fTourData.getTourStartPlace());
		fTextEndLocation.setText(fTourData.getTourEndPlace());
		fTextDescription.setText(fTourData.getTourDescription());

		onResizeContainer();
		fContentContainer.layout(true);

		/*
		 * tab: tour data
		 */
		reloadViewer();
	}

}
