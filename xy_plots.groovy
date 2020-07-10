//  ************  Notebooks / XY Series Plotting / Groovy   ****************
//  ************  Copyright 2019 Deephaven Data Labs, LLC   ****************
//
//  This Notebook provides working examples for tutorial purposes, and is not
//  meant to replace the primary Deephaven documentation, which can be found at
//  https://docs.deephaven.io/



//************* XY SERIES PLOTTING *************

// XY SERIES - SINGLE SERIES

t1 = db.t("LearnDeephaven","StockTrades")
    .where("Date=`2017-08-24`","USym=`AAPL`")

PlotSingle = plot("AAPL", t1.where("USym = `AAPL`"), "Timestamp", "Last")
    .xBusinessTime()
    .show()


// XY SERIES - MULTIPLE SERIES

t2 = db.t("LearnDeephaven","StockTrades")
    .where("Date=`2017-08-24`","USym in `INTC`,`CSCO`")

plotSharedAxis = plot("INTC", t2.where("USym = `INTC`"), "Timestamp", "Last")
    .plot("CSCO", t2.where("USym = `CSCO`"), "Timestamp", "Last")
    .xBusinessTime()
    .show()



// XY SERIES - MULTIPLE SERIES WITH TWINX

t3 = db.t("LearnDeephaven","StockTrades")
    .where("Date=`2017-08-24`","USym in `AAPL`,`GOOG`")

plotSharedTwinX = plot("AAPL", t3.where("USym = `AAPL`"), "Timestamp", "Last")
    .twinX()
    .plot("GOOG", t3.where("USym = `GOOG`"), "Timestamp", "Last")
    .xBusinessTime()
    .show()


//********* XY SERIES WITH PLOTSTYLES

// XY SERIES DEFAULT (NO PLOTSTYLE)

t4 = db.t("LearnDeephaven","StockTrades")
    .where("Date=`2017-08-24`","USym=`GOOG`")

plotXYDefault = plot("GOOG", t4.where("USym = `GOOG`"), "Timestamp", "Last")
    .xBusinessTime()
    .show()


// XY SERIES - SINGLE WITH PLOTSTYLE - AREA

t4 = db.t("LearnDeephaven","StockTrades")
    .where("Date=`2017-08-24`","USym=`GOOG`")

plotXYArea = plot("GOOG", t4.where("USym = `GOOG`"), "Timestamp", "Last")
    .xBusinessTime()
    .plotStyle("Area")
    .show()


// XY SERIES - MULTIPLE SERIES WITH PLOTSTYLE - STACKED AREA

t5 = db.t("LearnDeephaven", "EODTrades")
    .where("ImportDate = `2017-11-01`", "Ticker in `AAPL`, `MSFT`")
    .update("DateString = EODTimestamp.toDateString(TZ_NY)")
    .where("inRange(DateString, `2016-11-01`, `2016-12-01`)")

plotXYStackedArea = plot("AAPL", t5.where("Ticker = `AAPL`"), "EODTimestamp", "Volume")
    .plot("MSFT", t5.where("Ticker = `MSFT`"), "EODTimestamp", "Volume")
    .chartTitle("Trades Per Day By Ticker")
    .xLabel("Date")
    .yLabel("Volume")
    .plotStyle("stacked_area")
    .show()


// XY SERIES - MULTIPLE WITH PLOTSTYLE - SCATTER PLOT

t6 = db.t("LearnDeephaven", "StockTrades")
    .where("Date = `2017-08-25`", "USym in `AAPL`, `GOOG`, `MSFT`")
    .update("TimeBin = lowerBin(Timestamp, SECOND)")
    .firstBy("TimeBin")
    .where("TimeBin > '2017-08-25T10:00 NY' && TimeBin < '2017-08-25T11:00 NY'")

plotXYScatter = plot("AAPL", t6.where("USym = `AAPL`"), "Timestamp", "Last")
    .plotStyle("scatter")
    .pointSize(0.5)
    .pointColor(colorRGB(0,0,255,50))
    .pointShape("circle")
    .twinX()
    .plot("MSFT", t6.where("USym = `MSFT`"), "Timestamp", "Last")
    .plotStyle("scatter")
    .pointSize(0.8)
    .pointColor(colorRGB(255,0,0,100))
    .pointShape("up_triangle")
    .chartTitle("AAPL vs MSFT (10-11am ET)")
    .show()


// XY SERIES - SINGLE SERIES WITH PLOTSTYLE - STEP

t7 = db.t("LearnDeephaven","StockTrades")
    .where("Date=`2017-08-24`","USym=`GOOG`")
    .updateView("TimeBin=upperBin(Timestamp, 30 * MINUTE)")
    .where("isBusinessTime(TimeBin)")

plotXYStep = plot("GOOG", t7.where("USym = `GOOG`")
    .lastBy("TimeBin"), "TimeBin", "Last")
    .plotStyle("Step")
    .lineStyle(lineStyle(3))
    .show()



// ERROR BAR on XY SERIES
 
//source the data
t_EB = db.t("LearnDeephaven", "StockTrades")
    .where("Date = `2017-08-23`", "USym = `GOOG`")
    .updateView("TimeBin=upperBin(Timestamp, 20 * MINUTE)")
    .where("isBusinessTime(TimeBin)")

//calculate standard deviations for the upper and lower error values
t_EB_StdDev = t_EB.by(AggCombo(AggAvg("AvgPrice = Last"), AggStd("StdPrice = Last")), "TimeBin")

//plot the data
ebY_Trades = errorBarY("Trades: GOOG", t_EB_StdDev.update("AvgPriceLow = AvgPrice - StdPrice",
                                                          "AvgPriceHigh = AvgPrice + StdPrice"),
                       "TimeBin", "AvgPrice", "AvgPriceLow", "AvgPriceHigh")
    .show()




// PLOTBY WITH PLOTSTYLE - STACKED AREA

t_pb = db.t("LearnDeephaven", "EODTrades")
    .where("ImportDate = `2017-11-01`", "Ticker in `GOOG`,`AMZN`,`AAPL`, `MSFT`")
    .update("DateString = EODTimestamp.toDateString(TZ_NY)")
    .where("inRange(DateString, `2016-11-01`, `2016-12-01`)")

plotByExampleStackedArea = plotBy("TradesByDay", t_pb, "EODTimestamp", "Volume", "Ticker")
    .chartTitle("Trades Per Day By Ticker")
    .xLabel("Date")
    .yLabel("Volume")
    .plotStyle("stacked_area")
    .show()
