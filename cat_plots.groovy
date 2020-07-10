// ************  Notebooks / Category Plotting / Groovy   ****************
// ************  Copyright 2019 Deephaven Data Labs, LLC   ****************
// 
// This Notebook provides working examples for tutorial purposes, and is not
// meant to replace the primary Deephaven documentation, which can be found at
// https://docs.deephaven.io/


// CATEGORY PLOTTING - SINGLE

t1c = db.t("LearnDeephaven", "StockTrades")
    .where("Date >`2017-08-20`", "USym = `MSFT`")
    .view("Date", "USym", "Last", "Size", "ExchangeTimestamp")

totalSharesByUSym = t1c.view("Date", "USym", "SharesTraded=Size").sumBy("Date", "USym")

categoryPlot = catPlot("MSFT", totalSharesByUSym.where("USym = `MSFT`"), "Date", "SharesTraded")
    .chartTitle("Shares Traded")
    .show()


// CATEGORY PLOTTING - MULTIPLE

t2c = db.t("LearnDeephaven", "StockTrades")
    .where("Date >`2017-08-20`", "USym in `AAPL`, `MSFT`")
    .view("Date", "USym", "Last", "Size", "ExchangeTimestamp")

totalSharesByUSym2 = t2c.view("Date", "USym", "SharesTraded=Size").sumBy("Date", "USym")

categoryPlot2 = catPlot("MSFT", totalSharesByUSym2.where("USym = `MSFT`"), "Date", "SharesTraded")
    .catPlot("AAPL", totalSharesByUSym2.where("USym = `AAPL`"), "Date", "SharesTraded")
    .chartTitle("Shares Traded")
    .show()


// CATEGORY PLOTTING - MULTIPLE WITH PLOTSTYLE - STACKED BAR

t2c = db.t("LearnDeephaven", "StockTrades")
    .where("Date >`2017-08-20`", "USym in `AAPL`, `MSFT`")
    .view("Date", "USym", "Last", "Size", "ExchangeTimestamp")

totalSharesByUSym2 = t2c.view("Date", "USym", "SharesTraded=Size").sumBy("Date", "USym")

categoryPlotStacked = catPlot("MSFT", totalSharesByUSym2.where("USym = `MSFT`"), "Date", "SharesTraded")
    .catPlot("AAPL", totalSharesByUSym2.where("USym = `AAPL`"), "Date", "SharesTraded")
    .plotStyle("stacked_bar")
    .chartTitle("Shares Traded")
    .show()


// CATEGORY PLOTTING - MULTIPLE WITH PLOTSTYLE AND GROUPING - STACKED BAR

t3c = db.t("LearnDeephaven", "StockTrades")
    .where("Date >`2017-08-20`", "USym in `AAPL`, `MSFT`, `IBM`, `CSCO`")
    .view("Date", "USym", "Last", "Size", "ExchangeTimestamp")

totalSharesByUSym3 = t3c.view("Date", "USym", "SharesTraded=Size").sumBy("Date", "USym")

multiStackCatPlot = catPlot("MSFT", totalSharesByUSym3.where("USym = `MSFT`"), "Date", "SharesTraded")
    .group(1)
    .catPlot("AAPL", totalSharesByUSym3.where("USym = `AAPL`"), "Date", "SharesTraded")
    .group(1)
    //the two series above are assigned to group 1
    .catPlot("IBM", totalSharesByUSym3.where("USym = `IBM`"), "Date", "SharesTraded")
    .group(2)
    .catPlot("CSCO", totalSharesByUSym3.where("USym = `CSCO`"), "Date", "SharesTraded")
    .group(2)
    //the two series above are assigned to group 2
    .plotStyle("stacked_bar")
    .chartTitle("Shares Traded")
    .show()



// ERROR BAR on CATEGORY PLOT

//source the data
t_cat_EB = db.t("LearnDeephaven", "StockTrades")
    .where("Date = `2017-08-23`", "USym = `GOOG`")
    .updateView("TimeBin=upperBin(Timestamp, 20 * MINUTE)")
    .where("isBusinessTime(TimeBin)")

//calculate standard deviations for the upper and lower error values
t_cat_EB_StdDev = t_cat_EB.by(AggCombo(AggAvg("AvgPrice = Last"), AggStd("StdPrice = Last")), "TimeBin")

//plot the data
eb_CatTrades = catErrorBar("Trades: GOOG", t_cat_EB_StdDev.update("AvgPriceLow = AvgPrice - StdPrice",
                                                                  "AvgPriceHigh = AvgPrice + StdPrice"),
                           "TimeBin", "AvgPrice", "AvgPriceLow", "AvgPriceHigh")
    .show()
