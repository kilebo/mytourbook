/*******************************************************************************
 * Copyright (C) 2005, 2019 Wolfgang Schramm and Contributors
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

import de.byteholder.geoclipse.map.UI;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import net.tourbook.common.time.TourDateTime;
import net.tourbook.common.util.SQL;
import net.tourbook.common.util.TreeViewerItem;
import net.tourbook.common.util.Util;
import net.tourbook.database.TourDatabase;
import net.tourbook.ui.SQLFilter;

public class TVITourBookRoot extends TVITourBookItem {

   TVITourBookRoot(final TourBookView view) {
      super(view);
   }

   @Override
   protected void fetchChildren() {

      /*
       * set the children for the root item, these are year items
       */
      final ArrayList<TreeViewerItem> children = new ArrayList<>();
      setChildren(children);

      final SQLFilter sqlFilter = new SQLFilter(SQLFilter.TAG_FILTER);
      String fromTourData;

      final String sqlFilterWhereClause = sqlFilter.getWhereClause().trim();
      final boolean isSqlWhereClause = sqlFilterWhereClause.length() > 0;
      final String sqlWhereClause = isSqlWhereClause
            ? " WHERE 1=1 " + sqlFilterWhereClause + NL //$NON-NLS-1$
            : UI.EMPTY_STRING;

      if (sqlFilter.isTagFilterActive()) {

         // with tag filter

         fromTourData = NL

               + "FROM (            " + NL //$NON-NLS-1$

               + " SELECT           " + NL //$NON-NLS-1$

               + "  StartYear,      " + NL //$NON-NLS-1$
               + SQL_SUM_FIELDS + NL

               + "  FROM " + TourDatabase.TABLE_TOUR_DATA + NL//$NON-NLS-1$

               // get tag id's
               + "  LEFT OUTER JOIN " + TourDatabase.JOINTABLE__TOURDATA__TOURTAG + " jTdataTtag" + NL //$NON-NLS-1$ //$NON-NLS-2$
               + "  ON tourID = jTdataTtag.TourData_tourId   " + NL //$NON-NLS-1$

               + sqlWhereClause

               + ") td              " + NL//$NON-NLS-1$
         ;

      } else {

         // without tag filter

         fromTourData = NL

               + " FROM " + TourDatabase.TABLE_TOUR_DATA + NL //$NON-NLS-1$

               + sqlWhereClause;
      }

      final boolean isShowSummaryRow = tourBookView.isShowSummaryRow();

      final String groupBy = isShowSummaryRow

            // show a summary row
            ? " GROUP BY ROLLUP(StartYear)   " + NL //$NON-NLS-1$
            : " GROUP BY StartYear           " + NL; //$NON-NLS-1$

      final String sql = NL +

            "SELECT                    " + NL //$NON-NLS-1$

            + " StartYear,             " + NL //$NON-NLS-1$
            + SQL_SUM_COLUMNS

            + fromTourData
            + groupBy

            + " ORDER BY StartYear     " + NL //$NON-NLS-1$
      ;

      Connection conn = null;

      try {

         conn = TourDatabase.getInstance().getConnection();

         final PreparedStatement statement = conn.prepareStatement(sql);
         sqlFilter.setParameters(statement, 1);

         TVITourBookYear yearItem = null;

         int yearIndex = 0;
         int summaryIndex = 0;

         final ResultSet result = statement.executeQuery();
         while (result.next()) {

            final int dbYear = result.getInt(1);

            yearItem = new TVITourBookYear(tourBookView, this);
            children.add(yearItem);

            yearItem.treeColumn = Integer.toString(dbYear);
            yearItem.tourYear = dbYear;

            yearItem.colTourDateTime = new TourDateTime(calendar8.withYear(dbYear));
            yearItem.addSumColumns(result, 2);

            if (isShowSummaryRow && dbYear == 0) {

               // this should be the summary row
               summaryIndex = yearIndex;

               // add summary flag to the last row
               yearItem.isRowSummary = true;
            }

            yearIndex++;
         }

         /**
          * It can happen that the summary row is not the last row (seems to be a bug in derby)
          */
         final int numChildren = children.size();

         if (isShowSummaryRow

               // ensure there are items
               && numChildren > 0

               // check last position
               && summaryIndex != numChildren - 1) {

            // wrong summary row detected

            // move summary to the end
            final TreeViewerItem summarytYearItem = children.remove(summaryIndex);

            children.add(summarytYearItem);
         }

      } catch (final SQLException e) {
         SQL.showException(e, sql);
      } finally {
         Util.closeSql(conn);
      }
   }

}
