package net.tourbook.weather;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * A Java representation of a World Weather Online query result "weather" element.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class WWOResults {

   private String maxtempC;

   private String mintempC;

   private String avgtempC;

   private String hourly;

   public String getavgtempC() {
      return avgtempC;
   }

   // public String gethourly() {
   //     return hourly;
   //  }

   public String getmaxtempC() {
      return maxtempC;
   }

   public String getmintempC() {
      return mintempC;
   }

/*
 * public void setAttributes(final String attributes) {
 * this.attributes = attributes;
 * }
 * public void setDatatype(final String datatype) {
 * this.datatype = datatype;
 * }
 * public void setDate(final String date) {
 * this.date = date;
 * }
 * public void setStation(final String station) {
 * this.station = station;
 * }
 * public void setValue(final String value) {
 * this.value = value;
 * }
 */

}
