##  Deephaven - Plotting Recipes Cookbook - Python  
##  https://docs.deephaven.io/


from deephaven import Plot
# See print(sorted(dir())) or help('deephaven') for full namespace contents.


# generate shared tables
trades = db.t("LearnDeephaven", "StockTrades")\
   .where("Date=`2017-08-25`")\
   .view("Sym", "Last", "Size", "ExchangeTimestamp")

# note that we are constraining the data to NYSE business time
import deephaven.Calendars as Calendars
cal = Calendars.calendar("USNYSE")
trades = trades.where("cal.isBusinessTime(ExchangeTimestamp)")

totalShares = trades.view("Sym", "SharesTraded=Size").sumBy("Sym")
summaries = db.t("LearnDeephaven", "EODTrades").where("ImportDate=`2017-11-01`")


# XY Series
timePlot = Plot.plot("Microsoft", trades.where("Sym=`MSFT`"), "ExchangeTimestamp", "Last")\
   .show()

multiSeries = Plot.plot("Microsoft", trades.where("Sym=`MSFT`"), "ExchangeTimestamp", "Last")\
   .twinX()\
   .plot("Apple", trades.where("Sym=`AAPL`"), "ExchangeTimestamp", "Last")\
   .chartTitle("Price Over Time")\
   .show()

    
# Category
categoryPlot = Plot.catPlot("Shares Traded", totalShares, "Sym", "SharesTraded")\
   .chartTitle("Total Shares")\
   .show()


# Pie
pieChart = Plot.piePlot("Shares Traded", totalShares, "Sym", "SharesTraded")\
   .chartTitle("Total Shares")\
   .show()


# Histogram
histogram = Plot.histPlot("MSFT", trades.where("Sym=`MSFT`"), "Last", 3)\
   .chartTitle("Price Intervals")\
   .show()


# Category Histogram
catHist = Plot.catHistPlot("Number of Trades", trades, "Sym")\
   .chartTitle("Trades per Symbol")\
   .show()

    
# Open, High, Low and Close
ohlcPlot = Plot.ohlcPlot("MSFT", summaries.where("Ticker=`MSFT`"), "EODTimestamp", "Open", "High", "Low", "Close")\
   .chartTitle("Microsoft Activity")\
   .show()


# Grouping Multiple Plots in the Same Figure
multipleCharts = Plot.figure(2, 3)\
   .figureTitle("Trade Plots")\
   \
   .colSpan(3)\
   .plot("Microsoft", trades.where("Sym=`MSFT`"), "ExchangeTimestamp", "Last")\
   .twinX()\
   .plot("Apple", trades.where("Sym=`AAPL`"), "ExchangeTimestamp", "Last")\
   .chartTitle("Price Over Time")\
   \
   .newChart()\
   .histPlot("MSFT", trades.where("Sym=`MSFT`"), "Last", 3)\
   .chartTitle("Price Intervals")\
   \
   .newChart()\
   .colSpan(2)\
   .ohlcPlot("MSFT", summaries.where("Ticker=`MSFT`"), "EODTimestamp", "Open", "High", "Low", "Close")\
   .chartTitle("Microsoft Activity")\
   \
   .show()
