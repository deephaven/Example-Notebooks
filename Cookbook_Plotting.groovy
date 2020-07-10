//  Deephaven - Plotting Recipes Notebook - Groovy 
//  https://docs.deephaven.io/


// XY Series
trades = db.t("LearnDeephaven", "StockTrades")
    .where("Date=`2017-08-25`")
    .view("Sym", "Last", "Size", "ExchangeTimestamp")

timePlot = plot("Microsoft", trades.where("Sym=`MSFT`"), "ExchangeTimestamp", "Last")
    .show()

multiSeries = plot("Microsoft", trades.where("Sym=`MSFT`"), "ExchangeTimestamp", "Last")
    .twinX()
    .plot("Apple", trades.where("Sym=`AAPL`"), "ExchangeTimestamp", "Last")
    .chartTitle("Price Over Time")
    .show()

    
// Category
trades = db.t("LearnDeephaven", "StockTrades")
    .where("Date=`2017-08-25`")
    .view("Sym", "Last", "Size", "ExchangeTimestamp")

totalShares = trades.view("Sym", "SharesTraded=Size").sumBy("Sym")

categoryPlot = catPlot("Shares Traded", totalShares, "Sym", "SharesTraded")
    .chartTitle("Total Shares")
    .show()


// Pie
trades = db.t("LearnDeephaven", "StockTrades")
    .where("Date=`2017-08-25`")
    .view("Sym", "Last", "Size", "ExchangeTimestamp")

pieChart = piePlot("Shares Traded", totalShares, "Sym", "SharesTraded")
    .chartTitle("Total Shares")
    .show()
    

// Histogram
trades = db.t("LearnDeephaven", "StockTrades")
    .where("Date=`2017-08-25`")
    .view("Sym", "Last", "Size", "ExchangeTimestamp")

histogram = histPlot("MSFT", trades.where("Sym=`MSFT`"), "Last", 3)
    .chartTitle("Price Intervals")
    .show()
    

// Category Histogram
trades = db.t("LearnDeephaven", "StockTrades")
    .where("Date=`2017-08-25`")
    .view("Sym", "Last", "Size", "ExchangeTimestamp")

catHist = catHistPlot("Number of Trades", trades, "Sym")
    .chartTitle("Trades per Symbol")
    .show()

    
// Open, High, Low and Close
summaries = db.t("LearnDeephaven", "EODTrades").where("ImportDate=`2017-11-01`")

ohlcPlot = ohlcPlot("MSFT", summaries.where("Ticker=`MSFT`"), "EODTimestamp", "Open", "High", "Low","Close")
    .chartTitle("Microsoft Activity")
    .show()
    
    
// Grouping Multiple Plots in the Same Figure
    
trades = db.t("LearnDeephaven", "StockTrades")
    .where("Date=`2017-08-25`")
    .view("Sym", "Last", "Size", "ExchangeTimestamp")

summaries = db.t("LearnDeephaven", "EODTrades").where("ImportDate=`2017-11-01`")


multipleCharts = figure(2,3)
    .figureTitle("Trade Plots")
.colSpan(3)
    .plot("Microsoft", trades.where("Sym=`MSFT`"), "ExchangeTimestamp", "Last")
    .twinX()
    .plot("Apple", trades.where("Sym=`AAPL`"), "ExchangeTimestamp", "Last")
    .chartTitle("Price Over Time")
.newChart()
    .histPlot("MSFT", trades.where("Sym=`MSFT`"), "Last", 3)
    .chartTitle("Price Intervals")
.newChart()
    .colSpan(2)
    .ohlcPlot("MSFT", summaries.where("Ticker=`MSFT`"), "EODTimestamp", "Open", "High", "Low","Close")
    .chartTitle("Microsoft Activity")
.show()
