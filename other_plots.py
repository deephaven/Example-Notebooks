##  Deephaven - Other Plotting Notebook - Python  
##  https://docs.deephaven.io/


from deephaven import Plot
# See print(sorted(dir())) or help('deephaven') for full namespace contents.


# note that we are constraining the data to NYSE business time
import deephaven.Calendars as Calendars
cal = Calendars.calendar("USNYSE")
trades = trades.where("cal.isBusinessTime(ExchangeTimestamp)")


#  ************* HISTOGRAM PLOTTING *************

tHist = db.t("LearnDeephaven", "StockTrades")\
     .where("Date=`2017-08-25`")\
     .view("Sym", "Last", "Size", "ExchangeTimestamp")

plotPriceIntervals = Plot.histPlot("AAPL", tHist.where("Sym=`AAPL`"), "Last", 10)\
     .chartTitle("Price Intervals")\
     .show()


#  ************* CATEGORY HISTOGRAM PLOTTING *************

tHist = db.t("LearnDeephaven", "StockTrades")\
     .where("Date=`2017-08-25`")\
     .view("Sym", "Last", "Size", "ExchangeTimestamp")

catHistTradesBySym = Plot.catHistPlot("Number of Trades", tHist, "Sym")\
     .chartTitle("Trades per Symbol")\
     .show()


#  ************* PIE PLOTTING *************

tPie = db.t("LearnDeephaven", "StockTrades")\
     .where("Date=`2017-08-25`")\
     .view("Sym", "Last", "Size", "ExchangeTimestamp")

totalShares = tPie.view("Sym", "SharesTraded=Size").sumBy("Sym")

pieChart = Plot.piePlot("Shares Traded", totalShares, "Sym", "SharesTraded")\
     .chartTitle("Total Shares")\
     .show()


#  ************* OPEN HIGH LOW CLOSE (OHLC) PLOTTING *************

#  OPEN HIGH LOW CLOSE (OHLC) - SINGLE SERIES

tOHLC = db.t("LearnDeephaven","EODTrades")\
     .where("Ticker=`AAPL`", "ImportDate=`2017-11-01`",
            "inRange(EODTimestamp, '2017-06-01T12:00 NY', '2017-07-31T12:00 NY')")

plotOHLC = Plot.ohlcPlot("AAPL", tOHLC, "EODTimestamp", "Open", "High", "Low", "Close")\
     .xBusinessTime()\
     .lineStyle(Plot.lineStyle(2))\
     .chartTitle("AAPL OHLC - June-July 2017")\
     .show()


#  OPEN HIGH LOW CLOSE (OHLC) - MULTIPLE SERIES WITH TWINX

t2OHLC = db.t("LearnDeephaven", "EODTrades").where("Ticker in `AAPL`, `MSFT`", "ImportDate=`2017-11-01`",
                                              "inRange(EODTimestamp, '2017-06-01T12:00 NY', '2017-07-31T12:00 NY')")

plotOHLC2 = Plot.ohlcPlot("AAPL", t2OHLC.where("Ticker = `AAPL`"), "EODTimestamp", "Open", "High", "Low", "Close")\
     .lineStyle(Plot.lineStyle(2))\
     .twinX()\
     .ohlcPlot("MSFT", t2OHLC.where("Ticker = `MSFT`"),"EODTimestamp","Open","High","Low","Close")\
     .xBusinessTime()\
     .lineStyle(Plot.lineStyle(2))\
     .chartTitle("AAPL vs MSFT OHLC - June-July 2017")\
     .show()


#  ************* ERROR BAR PLOTTING *************

# ERROR BAR on XY SERIES
 
# source the data
t_EB = db.t("LearnDeephaven", "StockTrades")\
     .where("Date = `2017-08-23`", "USym = `GOOG`")\
     .updateView("TimeBin=upperBin(Timestamp, 20 * MINUTE)")\
     .where("isBusinessTime(TimeBin)")

# calculate standard deviations for the upper and lower error values
t_EB_StdDev = t_EB.by(caf.AggCombo(caf.AggAvg("AvgPrice = Last"), caf.AggStd("StdPrice = Last")), "TimeBin")

# plot the data
ebY_Trades = Plot.errorBarY("Trades: GOOG", t_EB_StdDev.update("AvgPriceLow = AvgPrice - StdPrice",
                                                              "AvgPriceHigh = AvgPrice + StdPrice"),
                           "TimeBin", "AvgPrice", "AvgPriceLow", "AvgPriceHigh")\
     .show()


# ERROR BAR on CATEGORY PLOT

# source the data
t_cat_EB = db.t("LearnDeephaven", "StockTrades")\
     .where("Date = `2017-08-23`", "USym = `GOOG`")\
     .updateView("TimeBin=upperBin(Timestamp, 20 * MINUTE)")\
     .where("isBusinessTime(TimeBin)")

# calculate standard deviations for the upper and lower error values
t_cat_EB_StdDev = t_cat_EB.by(caf.AggCombo(caf.AggAvg("AvgPrice = Last"), caf.AggStd("StdPrice = Last")), "TimeBin")

# plot the data
eb_CatTrades = Plot.catErrorBar("Trades: GOOG", t_cat_EB_StdDev.update("AvgPriceLow = AvgPrice - StdPrice",
                                                                      "AvgPriceHigh = AvgPrice + StdPrice"),
                               "TimeBin", "AvgPrice", "AvgPriceLow", "AvgPriceHigh")\
     .show()


# ************* SCATTERPLOT MATRIX PLOTTING *************

t_spm = db.t("LearnDeephaven", "StockTrades")\
     .where("Date > `2017-08`", "USym in `AAPL`, `GOOG`, `MSFT`")\
     .updateView("TimeBin=upperBin(Timestamp, 5 * MINUTE)")\
     .where("isBusinessTime(TimeBin)")\
     .firstBy("USym", "TimeBin")

AAPLTrades = t_spm.where("USym = `AAPL`").view("TimeBin", "AAPLLast = Last")
GOOGTrades = t_spm.where("USym = `GOOG`").view("TimeBin", "GOOGLast = Last")
MSFTTrades = t_spm.where("USym = `MSFT`").view("TimeBin", "MSFTLast = Last")

AllTrades = AAPLTrades.naturalJoin(GOOGTrades, "TimeBin", "GOOGLast")
AllTrades = AllTrades.naturalJoin(MSFTTrades, "TimeBin", "MSFTLast")

matrix = Plot.scatterPlotMatrix(AllTrades, "AAPLLast", "GOOGLast", "MSFTLast")\
     .pointSize(0.5)\
     .show()


#  ************* PLOTBY PLOTTING *************

# PLOTBY WITH PLOTSTYLE - STACKED AREA

t_pb = db.t("LearnDeephaven", "EODTrades").where("ImportDate = `2017-11-01`", "Ticker in `GOOG`,`AMZN`,`AAPL`, `MSFT`")\
     .update("DateString = EODTimestamp.toDateString(TZ_NY)")\
     .where("inRange(DateString, `2016-11-01`, `2016-12-01`)")

plotByExampleStackedArea = Plot.plotBy("TradesByDay", t_pb, "EODTimestamp", "Volume", "Ticker")\
     .chartTitle("Trades Per Day By Ticker")\
     .xLabel("Date")\
     .yLabel("Volume")\
     .plotStyle("stacked_area")\
     .show()
