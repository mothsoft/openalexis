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
package com.mothsoft.alexis.engine.numeric;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.tika.io.IOUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.mothsoft.alexis.dao.DataSetDao;
import com.mothsoft.alexis.dao.DataSetPointDao;
import com.mothsoft.alexis.dao.DataSetTypeDao;
import com.mothsoft.alexis.domain.DataSet;
import com.mothsoft.alexis.domain.DataSetPoint;
import com.mothsoft.alexis.domain.DataSetType;
import com.mothsoft.alexis.util.HttpClientResponse;
import com.mothsoft.alexis.util.NetworkingUtil;

public class StockQuoteDataSetImporter implements DataSetImporter {

    private static final Logger logger = Logger.getLogger(StockQuoteDataSetImporter.class);

    private static final String STOCK_QUOTES = "Stock Quotes";

    private PlatformTransactionManager transactionManager;
    private TransactionTemplate transactionTemplate;

    private DataSetDao dataSetDao;
    private DataSetPointDao dataSetPointDao;
    private DataSetTypeDao dataSetTypeDao;

    private List<String> stockSymbols;

    public StockQuoteDataSetImporter() {
        super();
    }

    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public void setDataSetDao(DataSetDao dataSetDao) {
        this.dataSetDao = dataSetDao;
    }

    public void setDataSetPointDao(DataSetPointDao dataSetPointDao) {
        this.dataSetPointDao = dataSetPointDao;
    }

    public void setDataSetTypeDao(DataSetTypeDao dataSetTypeDao) {
        this.dataSetTypeDao = dataSetTypeDao;
    }

    public void setStockSymbols(final List<String> stockSymbols) {
        this.stockSymbols = stockSymbols;
    }

    @Override
    public void importData() {
        if (this.transactionTemplate == null) {
            this.transactionTemplate = new TransactionTemplate(this.transactionManager);
        }

        final String base = "http://download.finance.yahoo.com/d/quotes.csv";
        final String s;
        try {
            s = URLEncoder.encode(StringUtils.join(this.stockSymbols, ","), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // if UTF-8 is not supported, we have big issues
            throw new IllegalStateException(e);
        }

        final String url = String.format("%s?s=%s&f=snl1", base, s);

        logger.info("Importing stock quote activity from web service URL: " + url);

        try {
            final HttpClientResponse response = NetworkingUtil.get(new URL(url), null, null);
            importStockQuotes(response);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void importStockQuotes(final HttpClientResponse response) {
        try {
            this.transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    final DataSetType type = StockQuoteDataSetImporter.this.dataSetTypeDao
                            .findSystemDataSetType(STOCK_QUOTES);

                    BufferedReader reader = null;
                    try {
                        reader = new BufferedReader(new InputStreamReader(response.getInputStream(), response
                                .getCharset()));
                        String line = null;
                        while ((line = reader.readLine()) != null) {
                            final String[] tokens = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
                            final String symbolName = StringUtils.replace(tokens[0], "\"", "");
                            final Double price;

                            try {
                                price = Double.valueOf(tokens[2]);
                            } catch (final Exception e) {
                                logger.error("Unable to parse stock price, exception: " + e, e);
                                return;
                            }

                            DataSet dataSet = StockQuoteDataSetImporter.this.dataSetDao.findSystemDataSet(type,
                                    symbolName);

                            if (dataSet == null) {
                                dataSet = new DataSet(symbolName, type);
                                StockQuoteDataSetImporter.this.dataSetDao.add(dataSet);
                            }

                            final DataSetPoint point = new DataSetPoint(dataSet, new Date(), price);
                            StockQuoteDataSetImporter.this.dataSetPointDao.add(point);
                        }
                    } catch (IOException e) {
                        logger.warn(e, e);
                        throw new RuntimeException(e);
                    } finally {
                        IOUtils.closeQuietly(reader);
                    }

                }
            });
        } catch (final Exception e) {
            response.abort();
            logger.warn(e, e);
            throw new RuntimeException(e);
        } finally {
            response.close();
        }
    }

}
