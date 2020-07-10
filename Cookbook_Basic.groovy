//  Deephaven - Basic Recipes Notebook - Groovy 
//  https://docs.deephaven.io/


//Tidying Things Up

trades = db.t("LearnDeephaven", "StockTrades").where("Date=`2017-08-25`")
trim = trades.view("Exchange", "Sym", "Last", "Size", "ExchangeTimestamp")
rename = trim.renameColumns("Symbol=Sym", "LastPrice=Last")
move = rename.moveColumns(4, "Exchange").moveUpColumns("ExchangeTimestamp")

//Formatting Tables

trades = db.t("LearnDeephaven", "StockTrades").where("Date=`2017-08-25`")
trim = trades.view("Sym", "Last", "Size", "ExchangeTimestamp")
dollarFormat = trim.formatColumns("Last=Decimal(`\$0.00`)")
green = dollarFormat.formatColumns("Last=`GREEN`")
green2 = dollarFormat.formatColumnWhere("Last", "Last >= 160.00", "GREEN")
rowFormat = green.formatRowWhere("Last >= 160", "BLUE")
first20 = dollarFormat.head(20)
heatmap = first20.formatColumns("Size=heatmap(Size, 0, 100, YELLOW, RED)")

//Adding Columns

quotes = db.t("LearnDeephaven", "StockQuotes")
   .where("Date=`2017-08-25`")
   .view("Sym", "Bid", "Ask", "ExchangeTimestamp")
newColumn = quotes.updateView("NewColumn=5")
midPrice = quotes.updateView("MidPrice=(Bid+Ask)/2")
difference = midPrice.updateView("Difference=abs(Bid-Ask)")

//Table Summaries

trades = db.t("LearnDeephaven", "StockTrades").where("Date=`2017-08-25`")
totalShares2 = trades.view("Size").sumBy()
lastTime = trades.view("ExchangeTimestamp").lastBy()
summary = trades.by(AggCombo(AggSum("Size"), AggLast("ExchangeTimestamp")))

//Table Subset Summaries

trades = db.t("LearnDeephaven", "StockTrades").where("Date=`2017-08-25`")
totalShares = trades.view("Sym", "Size").sumBy("Sym")
lastTime = trades.view("Sym", "ExchangeTimestamp").lastBy("Sym")
summary = trades.by(AggCombo(AggSum("Size"), AggLast("ExchangeTimestamp")), "Sym")

//Grouping

trades = db.t("LearnDeephaven", "StockTrades")
   .where("Date=`2017-08-25`")
   .view("Sym", "Last", "Size", "ExchangeTimestamp")
firstThree = trades.head(3)
groups = firstThree.by()
symFirstThree = trades.headBy(3, "Sym")
trim = symFirstThree.view("Sym", "Last")
symGroups = trim.by("Sym")
averages = symGroups.updateView("AvgPrice=avg(Last)")
ungroup = averages.ungroup()
difference = ungroup.updateView("Difference=AvgPrice-Last")