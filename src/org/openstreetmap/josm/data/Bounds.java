// License: GPL. Copyright 2007 by Immanuel Scholz and others
package org.openstreetmap.josm.data;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.text.MessageFormat;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.tools.CheckParameterUtil;

/**
 * This is a simple data class for "rectangular" areas of the world, given in
 * lat/lon min/max values.  The values are rounded to LatLon.OSM_SERVER_PRECISION
 *
 * @author imi
 */
public class Bounds {
    /**
     * The minimum and maximum coordinates.
     */
    private double minLat, minLon, maxLat, maxLon;

    public LatLon getMin() {
        return new LatLon(minLat, minLon);
    }

    public LatLon getMax() {
        return new LatLon(maxLat, maxLon);
    }
    
    public enum ParseMethod {
        MINLAT_MINLON_MAXLAT_MAXLON,
        LEFT_BOTTOM_RIGHT_TOP
    }
    
    /**
     * Construct bounds out of two points
     */
    public Bounds(LatLon min, LatLon max) {
        this(min.lat(), min.lon(), max.lat(), max.lon());
    }

    public Bounds(LatLon b) {
        // Do not call this(b, b) to avoid GPX performance issue (see #7028) until roundToOsmPrecision() is improved
        this.minLat = LatLon.roundToOsmPrecision(b.lat());
        this.minLon = LatLon.roundToOsmPrecision(b.lon());
        this.maxLat = this.minLat;
        this.maxLon = this.minLon;
    }

    public Bounds(double minlat, double minlon, double maxlat, double maxlon) {
        this.minLat = LatLon.roundToOsmPrecision(minlat);
        this.minLon = LatLon.roundToOsmPrecision(minlon);
        this.maxLat = LatLon.roundToOsmPrecision(maxlat);
        this.maxLon = LatLon.roundToOsmPrecision(maxlon);
    }

    public Bounds(double [] coords) {
        CheckParameterUtil.ensureParameterNotNull(coords, "coords");
        if (coords.length != 4)
            throw new IllegalArgumentException(MessageFormat.format("Expected array of length 4, got {0}", coords.length));
        this.minLat = LatLon.roundToOsmPrecision(coords[0]);
        this.minLon = LatLon.roundToOsmPrecision(coords[1]);
        this.maxLat = LatLon.roundToOsmPrecision(coords[2]);
        this.maxLon = LatLon.roundToOsmPrecision(coords[3]);
    }

    public Bounds(String asString, String separator) throws IllegalArgumentException {
        this(asString, separator, ParseMethod.MINLAT_MINLON_MAXLAT_MAXLON);
    }

    public Bounds(String asString, String separator, ParseMethod parseMethod) throws IllegalArgumentException {
        CheckParameterUtil.ensureParameterNotNull(asString, "asString");
        String[] components = asString.split(separator);
        if (components.length != 4)
            throw new IllegalArgumentException(MessageFormat.format("Exactly four doubles expected in string, got {0}: {1}", components.length, asString));
        double[] values = new double[4];
        for (int i=0; i<4; i++) {
            try {
                values[i] = Double.parseDouble(components[i]);
            } catch(NumberFormatException e) {
                throw new IllegalArgumentException(MessageFormat.format("Illegal double value ''{0}''", components[i]));
            }
        }
        
        switch (parseMethod) {
            case LEFT_BOTTOM_RIGHT_TOP:
                this.minLat = initLat(values[1]);
                this.minLon = initLon(values[0]);
                this.maxLat = initLat(values[3]);
                this.maxLon = initLon(values[2]);
                break;
            case MINLAT_MINLON_MAXLAT_MAXLON:
            default:
                this.minLat = initLat(values[0]);
                this.minLon = initLon(values[1]);
                this.maxLat = initLat(values[2]);
                this.maxLon = initLon(values[3]);
        }
    }
    
    protected static double initLat(double value) {
        if (!LatLon.isValidLat(value))
            throw new IllegalArgumentException(tr("Illegal latitude value ''{0}''", value));
        return LatLon.roundToOsmPrecision(value);
    }

    protected static double initLon(double value) {
        if (!LatLon.isValidLon(value))
            throw new IllegalArgumentException(tr("Illegal longitude value ''{0}''", value));
        return LatLon.roundToOsmPrecision(value);
    }

    public Bounds(Bounds other) {
        this(other.getMin(), other.getMax());
    }

    public Bounds(Rectangle2D rect) {
        this(rect.getMinY(), rect.getMinX(), rect.getMaxY(), rect.getMaxX());
    }

    /**
     * Creates new bounds around a coordinate pair <code>center</code>. The
     * new bounds shall have an extension in latitude direction of <code>latExtent</code>,
     * and in longitude direction of <code>lonExtent</code>.
     *
     * @param center  the center coordinate pair. Must not be null.
     * @param latExtent the latitude extent. > 0 required.
     * @param lonExtent the longitude extent. > 0 required.
     * @throws IllegalArgumentException thrown if center is null
     * @throws IllegalArgumentException thrown if latExtent <= 0
     * @throws IllegalArgumentException thrown if lonExtent <= 0
     */
    public Bounds(LatLon center, double latExtent, double lonExtent) {
        CheckParameterUtil.ensureParameterNotNull(center, "center");
        if (latExtent <= 0.0)
            throw new IllegalArgumentException(MessageFormat.format("Parameter ''{0}'' > 0.0 exptected, got {1}", "latExtent", latExtent));
        if (lonExtent <= 0.0)
            throw new IllegalArgumentException(MessageFormat.format("Parameter ''{0}'' > 0.0 exptected, got {1}", "lonExtent", lonExtent));

        this.minLat = LatLon.roundToOsmPrecision(LatLon.toIntervalLat(center.lat() - latExtent / 2));
        this.minLon = LatLon.roundToOsmPrecision(LatLon.toIntervalLon(center.lon() - lonExtent / 2));
        this.maxLat = LatLon.roundToOsmPrecision(LatLon.toIntervalLat(center.lat() + latExtent / 2));
        this.maxLon = LatLon.roundToOsmPrecision(LatLon.toIntervalLon(center.lon() + lonExtent / 2));
    }

    @Override public String toString() {
        return "Bounds["+minLat+","+minLon+","+maxLat+","+maxLon+"]";
    }

    public String toShortString(DecimalFormat format) {
        return
        format.format(minLat) + " "
        + format.format(minLon) + " / "
        + format.format(maxLat) + " "
        + format.format(maxLon);
    }

    /**
     * @return Center of the bounding box.
     */
    public LatLon getCenter()
    {
        if (crosses180thMeridian()) {
            LatLon result = new LatLon(minLat, minLon-360.0).getCenter(getMax());
            if (result.lon() < -180.0) {
                result.setLocation(result.lon()+360.0, result.lat());
            }
            return result;
        } else {
            return getMin().getCenter(getMax());
        }
    }

    /**
     * Extend the bounds if necessary to include the given point.
     */
    public void extend(LatLon ll) {
        if (ll.lat() < minLat) {
            minLat = LatLon.roundToOsmPrecision(ll.lat());
        }
        if (ll.lat() > maxLat) {
            maxLat = LatLon.roundToOsmPrecision(ll.lat());
        }
        if (crosses180thMeridian()) {
            if (ll.lon() > maxLon && ll.lon() < minLon) {
                if (Math.abs(ll.lon() - minLon) <= Math.abs(ll.lon() - maxLon)) {
                    minLon = LatLon.roundToOsmPrecision(ll.lon());
                } else {
                    maxLon = LatLon.roundToOsmPrecision(ll.lon());
                }
            }
        } else {
            if (ll.lon() < minLon) {
                minLon = LatLon.roundToOsmPrecision(ll.lon());
            }
            if (ll.lon() > maxLon) {
                maxLon = LatLon.roundToOsmPrecision(ll.lon());
            }
        }
    }

    public void extend(Bounds b) {
        extend(b.getMin());
        extend(b.getMax());
    }
    
    /**
     * Is the given point within this bounds?
     */
    public boolean contains(LatLon ll) {
        if (ll.lat() < minLat || ll.lat() > maxLat)
            return false;
        if (crosses180thMeridian()) {
            if (ll.lon() > maxLon && ll.lon() < minLon)
                return false;
        } else {
            if (ll.lon() < minLon || ll.lon() > maxLon)
                return false;
        }
        return true;
    }

    private static boolean intersectsLonCrossing(Bounds crossing, Bounds notCrossing) {
        return notCrossing.minLon <= crossing.maxLon || notCrossing.maxLon >= crossing.minLon;
    }
    
    /**
     * The two bounds intersect? Compared to java Shape.intersects, if does not use
     * the interior but the closure. (">=" instead of ">")
     */
    public boolean intersects(Bounds b) {
        if (b.maxLat < minLat || b.minLat > maxLat)
            return false;
        
        if (crosses180thMeridian() && !b.crosses180thMeridian()) {
            return intersectsLonCrossing(this, b);
        } else if (!crosses180thMeridian() && b.crosses180thMeridian()) {
            return intersectsLonCrossing(b, this);
        } else if (crosses180thMeridian() && b.crosses180thMeridian()) {
            return true;
        } else {
            return b.maxLon >= minLon && b.minLon <= maxLon;
        }
    }

    /**
     * Determines if this Bounds object crosses the 180th Meridian.
     * See http://wiki.openstreetmap.org/wiki/180th_meridian
     * @return true if this Bounds object crosses the 180th Meridian.
     */
    public boolean crosses180thMeridian() {
        return this.minLon > this.maxLon;
    }
    
    /**
     * Converts the lat/lon bounding box to an object of type Rectangle2D.Double
     * @return the bounding box to Rectangle2D.Double
     */
    public Rectangle2D.Double asRect() {
        double w = maxLon-minLon + (crosses180thMeridian() ? 360.0 : 0.0);
        return new Rectangle2D.Double(minLon, minLat, w, maxLat-minLat);
    }

    public double getArea() {
        double w = maxLon-minLon + (crosses180thMeridian() ? 360.0 : 0.0);
        return w * (maxLat - minLat);
    }

    public String encodeAsString(String separator) {
        StringBuffer sb = new StringBuffer();
        sb.append(minLat).append(separator).append(minLon)
        .append(separator).append(maxLat).append(separator)
        .append(maxLon);
        return sb.toString();
    }

    /**
     * <p>Replies true, if this bounds are <em>collapsed</em>, i.e. if the min
     * and the max corner are equal.</p>
     * 
     * @return true, if this bounds are <em>collapsed</em>
     */
    public boolean isCollapsed() {
        return getMin().equals(getMax());
    }

    public boolean isOutOfTheWorld() {
        return
        minLat < -90 || minLat > 90 ||
        maxLat < -90 || maxLat > 90 ||
        minLon < -180 || minLon > 180 ||
        maxLon < -180 || maxLon > 180;
    }

    public void normalize() {
        minLat = LatLon.toIntervalLat(minLat);
        maxLat = LatLon.toIntervalLat(maxLat);
        minLon = LatLon.toIntervalLon(minLon);
        maxLon = LatLon.toIntervalLon(maxLon);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(maxLat);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(maxLon);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(minLat);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(minLon);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Bounds other = (Bounds) obj;
        if (Double.doubleToLongBits(maxLat) != Double.doubleToLongBits(other.maxLat))
            return false;
        if (Double.doubleToLongBits(maxLon) != Double.doubleToLongBits(other.maxLon))
            return false;
        if (Double.doubleToLongBits(minLat) != Double.doubleToLongBits(other.minLat))
            return false;
        if (Double.doubleToLongBits(minLon) != Double.doubleToLongBits(other.minLon))
            return false;
        return true;
    }
}
