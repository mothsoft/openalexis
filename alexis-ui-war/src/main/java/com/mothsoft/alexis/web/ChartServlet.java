/*   Copyright 2012 Tim Garrett, Mothsoft LLC
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.mothsoft.alexis.web;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.mothsoft.alexis.domain.DataSet;
import com.mothsoft.alexis.domain.DataSetPoint;
import com.mothsoft.alexis.domain.TimeUnits;
import com.mothsoft.alexis.security.CurrentUserUtil;
import com.mothsoft.alexis.service.DataSetService;

/**
 * Servlet implementation class ChartServlet
 */
public class ChartServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = Logger.getLogger(ChartServlet.class);

    private static final Paint[] PAINTS;
    private static final Font DEFAULT_FONT = new Font("SansSerif", Font.PLAIN, 10);

    static {
        // modeled after Office 2007 charting palette
        PAINTS = new Paint[] { new Color(0x385D8A), new Color(0x8C3836), new Color(0x71893F), new Color(0x5C4776),
                new Color(0x357D91), new Color(0xB66D31), new Color(0x426DA1), new Color(0xA44340),
                new Color(0x849F4B), new Color(0x6C548A), new Color(0x3F92A9), new Color(0xD37F3A),
                new Color(0x4B7BB4), new Color(0xB74C49), new Color(0x94B255), new Color(0x7A5F9A),
                new Color(0x47A4BD), new Color(0xEC8F42), new Color(0x7394C5), new Color(0xC87372),
                new Color(0xA9C379), new Color(0x9480AE), new Color(0x70B7CD), new Color(0xF8A56E),
                new Color(0xA1B4D4), new Color(0xD6A1A0), new Color(0xC0D2A4), new Color(0xB3A8C4),
                new Color(0xA0CAD9), new Color(0xF9BE9E) };
    }

    /**
     * @see HttpServlet#HttpServlet()
     */
    public ChartServlet() {
        super();
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final String[] pathComponents = request.getPathInfo().split("/");
        final String graphType = pathComponents[1];

        if ("line".equals(graphType)) {

            final String[] dataSets = request.getParameterValues("ds");

            final String w = request.getParameter("w");
            final Integer width = w == null ? 405 : Integer.valueOf(w);

            final String h = request.getParameter("h");
            final Integer height = h == null ? 325 : Integer.valueOf(h);

            final String n = request.getParameter("n");
            final Integer numberOfSamples = n == null ? 12 : Integer.valueOf(n);

            final String title = request.getParameter("title");

            final long start = System.currentTimeMillis();
            doLineGraph(request, response, title, dataSets, width, height, numberOfSamples);
            logger.debug("Graph took: " + (System.currentTimeMillis() - start) + " milliseconds");
        }
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {
        throw new ServletException("Unsupported method: POST");
    }

    private void doLineGraph(final HttpServletRequest request, final HttpServletResponse response, final String title,
            final String[] dataSetIds, final Integer width, final Integer height, final Integer numberOfSamples)
            throws ServletException, IOException {

        final DataSetService dataSetService = WebApplicationContextUtils.getWebApplicationContext(
                this.getServletContext()).getBean(DataSetService.class);

        final OutputStream out = response.getOutputStream();
        response.setContentType("image/png");
        response.setHeader("Cache-Control", "max-age: 5; must-revalidate");

        final XYSeriesCollection seriesCollection = new XYSeriesCollection();

        final TimeZone timeZone = CurrentUserUtil.getTimeZone();
        final DateAxis dateAxis = new DateAxis(title != null ? title : "Time", timeZone);
        dateAxis.setLabelFont(DEFAULT_FONT);
        dateAxis.setTickLabelFont(DEFAULT_FONT);

        final SimpleDateFormat dateFormat = new SimpleDateFormat("ha");
        dateFormat.setTimeZone(timeZone);
        final DateFormat chartFormatter = dateFormat;
        dateAxis.setDateFormatOverride(chartFormatter);

        final DateTickUnit unit = new DateTickUnit(DateTickUnit.HOUR, 1);
        dateAxis.setTickUnit(unit);

        if (numberOfSamples > 12) {
            dateAxis.setTickLabelFont(new Font(DEFAULT_FONT.getFamily(), Font.PLAIN,
                    (int) (DEFAULT_FONT.getSize() * .8)));
        }

        final NumberAxis yAxis = new NumberAxis("Activity");

        final StandardXYItemRenderer renderer = new StandardXYItemRenderer(StandardXYItemRenderer.SHAPES_AND_LINES);

        int colorCounter = 0;

        if (dataSetIds != null) {
            for (final String dataSetIdString : dataSetIds) {
                final Long dataSetId = Long.valueOf(dataSetIdString);
                final DataSet dataSet = dataSetService.get(dataSetId);

                // go back for numberOfSamples, but include current hour
                final Calendar calendar = new GregorianCalendar();
                calendar.setTimeZone(timeZone);
                calendar.add(Calendar.HOUR_OF_DAY, -1 * (numberOfSamples - 1));
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                final Timestamp startDate = new Timestamp(calendar.getTimeInMillis());

                calendar.add(Calendar.HOUR_OF_DAY, numberOfSamples);
                calendar.set(Calendar.MINUTE, 59);
                calendar.set(Calendar.SECOND, 59);
                calendar.set(Calendar.MILLISECOND, 999);
                final Timestamp endDate = new Timestamp(calendar.getTimeInMillis());

                logger.debug(String.format("Generating chart for period: %s to %s", startDate.toString(),
                        endDate.toString()));

                final List<DataSetPoint> dataSetPoints = dataSetService.findAndAggregatePointsGroupedByUnit(dataSetId,
                        startDate, endDate, TimeUnits.HOUR);

                final boolean hasData = addSeries(seriesCollection, dataSet.getName(), dataSetPoints, startDate,
                        numberOfSamples, renderer);

                if (dataSet.isAggregate()) {
                    renderer.setSeriesPaint(seriesCollection.getSeriesCount() - 1, Color.BLACK);
                } else if (hasData) {
                    renderer.setSeriesPaint(seriesCollection.getSeriesCount() - 1, PAINTS[colorCounter++
                            % PAINTS.length]);
                } else {
                    renderer.setSeriesPaint(seriesCollection.getSeriesCount() - 1, Color.LIGHT_GRAY);
                }
            }
        }

        final XYPlot plot = new XYPlot(seriesCollection, dateAxis, yAxis, renderer);

        // create the chart...
        final JFreeChart chart = new JFreeChart(plot);

        // set the background color for the chart...
        chart.setBackgroundPaint(Color.white);

        // get a reference to the plot for further customisation...
        plot.setBackgroundPaint(new Color(253, 253, 253));
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

        // set the range axis to display integers only...
        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setLabelFont(DEFAULT_FONT);
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        rangeAxis.setLowerBound(0.00d);

        ChartUtilities.writeChartAsPNG(out, chart, width, height);
    }

    private boolean addSeries(final XYSeriesCollection seriesCollection, final String dataSetName,
            final List<DataSetPoint> points, final Timestamp startDate, final Integer numberOfSamples,
            final StandardXYItemRenderer renderer) {

        // create the series
        final XYSeries series = new XYSeries(dataSetName);
        Double total = 0.0d;

        final Map<Date, Double> rawPoints = new LinkedHashMap<Date, Double>();

        final Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date(startDate.getTime()));

        for (int i = 0; i < numberOfSamples; i++) {
            rawPoints.put(calendar.getTime(), 0.0d);
            calendar.add(Calendar.HOUR_OF_DAY, 1);
        }

        for (final DataSetPoint ith : points) {
            final Date date = new Date(ith.getX().getTime());
            rawPoints.put(date, ith.getY());
            total += ith.getY();
        }

        for (final Map.Entry<Date, Double> entry : rawPoints.entrySet()) {
            final long x = entry.getKey().getTime();
            final Double y = entry.getValue();

            logger.debug("Adding point to series: (" + entry.getKey() + ", " + y + ")");

            series.add(new XYDataItem(x, (Number) y));
        }

        seriesCollection.addSeries(series);

        return total > 0;
    }
}
