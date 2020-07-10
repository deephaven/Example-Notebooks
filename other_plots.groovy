// **************   Notebooks / Other Plotting / Groovy    ****************
// ************  Copyright 2019 Deephaven Data Labs, LLC   ****************
// 
//  This Notebook provides working examples for tutorial purposes, and is not
//  meant to replace the primary Deephaven documentation, which can be found at
//  https://docs.deephaven.io/



//************* HISTOGRAM PLOTTING *************

tHist = db.t("LearnDeephaven", "StockTrades")
    .where("Date=`2017-08-25`")
    .view("Sym", "Last", "Size", "ExchangeTimestamp")

plotPriceIntervals = histPlot("AAPL", tHist.where("Sym=`AAPL`"), "Last", 10)
    .chartTitle("Price Intervals")
    .show()




//************* CATEGORY HISTOGRAM PLOTTING *************

tHist = db.t("LearnDeephaven", "StockTrades")
    .where("Date=`2017-08-25`")
    .view("Sym", "Last", "Size", "ExchangeTimestamp")

catHistTradesBySym = catHistPlot("Number of Trades", tHist, "Sym")
    .chartTitle("Trades per Symbol")
    .show()




//************* PIE PLOTTING *************

tPie = db.t("LearnDeephaven", "StockTrades")
     .where("Date=`2017-08-25`")
     .view("Sym", "Last", "Size", "ExchangeTimestamp")

totalShares = tPie.view("Sym", "SharesTraded=Size").sumBy("Sym")

pieChart = piePlot("Shares Traded", totalShares, "Sym", "SharesTraded")
     .chartTitle("Total Shares")
     .show()




//************* OPEN HIGH LOW CLOSE (OHLC) PLOTTING *************

//OPEN HIGH LOW CLOSE (OHLC) - SINGLE SERIES

tOHLC = db.t("LearnDeephaven","EODTrades").where("Ticker=`AAPL`", "ImportDate=`2017-11-01`", "inRange(EODTimestamp, '2017-06-01T12:00 NY', '2017-07-31T12:00 NY')")

plotOHLC = ohlcPlot("AAPL", tOHLC, "EODTimestamp", "Open", "High", "Low", "Close")
    .xBusinessTime()
    .lineStyle(lineStyle(2))
    .chartTitle("AAPL OHLC - June-July 2017")
    .show()


//OPEN HIGH LOW CLOSE (OHLC) - MULTIPLE SERIES WITH TWINX

t2OHLC = db.t("LearnDeephaven","EODTrades").where("Ticker in `AAPL`, `MSFT`", "ImportDate=`2017-11-01`", "inRange(EODTimestamp, '2017-06-01T12:00 NY', '2017-07-31T12:00 NY')")

plotOHLC2 = ohlcPlot("AAPL", t2OHLC.where("Ticker = `AAPL`"),"EODTimestamp","Open","High","Low","Close")
    .lineStyle(lineStyle(2))
    .twinX()
    .ohlcPlot("MSFT", t2OHLC.where("Ticker = `MSFT`"),"EODTimestamp","Open","High","Low","Close")
    .xBusinessTime()
    .lineStyle(lineStyle(2))
    .chartTitle("AAPL vs MSFT OHLC - June-July 2017")
    .show()



//************* ERROR BAR PLOTTING *************

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



//************* SCATTERPLOT MATRIX PLOTTING *************

t_spm = db.t("LearnDeephaven","StockTrades")
    .where("Date > `2017-08`","USym in `AAPL`, `GOOG`, `MSFT`")
    .updateView("TimeBin=upperBin(Timestamp, 5 * MINUTE)")
    .where("isBusinessTime(TimeBin)")
    .firstBy("USym", "TimeBin")

AAPLTrades = t_spm.where("USym = `AAPL`").view("TimeBin", "AAPLLast = Last")
GOOGTrades = t_spm.where("USym = `GOOG`").view("TimeBin", "GOOGLast = Last")
MSFTTrades = t_spm.where("USym = `MSFT`").view("TimeBin", "MSFTLast = Last")

AllTrades = AAPLTrades.naturalJoin(GOOGTrades, "TimeBin", "GOOGLast")
AllTrades = AllTrades.naturalJoin(MSFTTrades, "TimeBin", "MSFTLast")

matrix = scatterPlotMatrix(AllTrades, "AAPLLast", "GOOGLast", "MSFTLast")
    .pointSize(0.5)
    .show()

 

//************* PLOTBY PLOTTING *************

// PLOTBY WITH PLOTSTYLE - STACKED AREA

t_pb = db.t("LearnDeephaven", "EODTrades").where("ImportDate = `2017-11-01`", "Ticker in `GOOG`,`AMZN`,`AAPL`, `MSFT`")
    .update("DateString = EODTimestamp.toDateString(TZ_NY)")
    .where("inRange(DateString, `2016-11-01`, `2016-12-01`)")

plotByExampleStackedArea = plotBy("TradesByDay", t_pb, "EODTimestamp", "Volume", "Ticker")
    .chartTitle("Trades Per Day By Ticker")
    .xLabel("Date")
    .yLabel("Volume")
    .plotStyle("stacked_area")
    .show()
