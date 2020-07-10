##  Deephaven - Basic Recipes Notebook - Python  
##  https://docs.deephaven.io/


from deephaven import *
# See print(sorted(dir())) or help('deephaven') for full namespace contents.


# generate shared tables
trades = db.t("LearnDeephaven", "StockTrades").where("Date=`2017-08-25`")
quotes = db.t("LearnDeephaven", "StockQuotes").where("Date=`2017-08-25`")\
    .view("Sym", "Bid", "Ask", "ExchangeTimestamp")


# Tidying Things Up
trim = trades.view("Exchange", "Sym", "Last", "Size", "ExchangeTimestamp")
rename = trim.renameColumns("Symbol=Sym", "LastPrice=Last")
move = rename.moveColumns(4, "Exchange").moveUpColumns("ExchangeTimestamp")


# Formatting Tables
dollarFormat = trim.formatColumns("Last=Decimal(`$0.00`)")
green = dollarFormat.formatColumns("Last=`GREEN`")
green2 = dollarFormat.formatColumnWhere("Last", "Last >= 160.00", "GREEN")
rowFormat = green.formatRowWhere("Last >= 160", "BLUE")
first20 = dollarFormat.head(20)
heatmap = first20.formatColumns("Size=heatmap(Size, 0, 100, YELLOW, RED)")


# Adding Columns
newColumn = quotes.updateView("NewColumn=5")
midPrice = quotes.updateView("MidPrice=(Bid+Ask)/2")
difference = midPrice.updateView("Difference=abs(Bid-Ask)")


# Table Summaries
totalShares = trades.view("Size").sumBy()
lastTime = trades.view("ExchangeTimestamp").lastBy()
summary = trades.by(caf.AggCombo(caf.AggSum("Size"), caf.AggLast("ExchangeTimestamp")))


# Table Subset Summaries
totalShares2 = trades.view("Sym", "Size").sumBy("Sym")
lastTime2 = trades.view("Sym", "ExchangeTimestamp").lastBy("Sym")
summary2 = trades.by(caf.AggCombo(caf.AggSum("Size"), caf.AggLast("ExchangeTimestamp")), "Sym")


# Grouping
firstThree = trim.head(3)
groups = firstThree.by()
symFirstThree = trades.headBy(3, "Sym")
trim2 = symFirstThree.view("Sym", "Last")
symGroups = trim2.by("Sym")
averages = symGroups.updateView("AvgPrice=avg(Last)")
ungroup = averages.ungroup()
difference2 = ungroup.updateView("Difference=AvgPrice-Last")
