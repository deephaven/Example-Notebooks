//  Deephaven - Joins Recipes Notebook - Groovy 
//  https://docs.deephaven.io/


//Exact Join and Natural Join Recipe
trades = db.t("LearnDeephaven", "StockTrades")
     .where("Date=`2017-08-25`")
     .headBy(3, "Sym")
     .view("Sym", "Last", "Size")

summary = trades.by(AggCombo(AggAvg("AvgPrice=Last"), AggSum("TotalShares=Size")), "Sym")
					
ej = trades.exactJoin(summary, "Sym", "AvgPrice, TotalShares")

summaryTrim = summary.tail(9)

nj = trades.naturalJoin(summaryTrim, "Sym", "AvgPrice, TotalShares")
        


//Join
trades = db.t("LearnDeephaven", "StockTrades").where("Date=`2017-08-25`")
     .headBy(3, "Sym")
     .view("Sym", "Last", "Size")

summary = trades.by(AggCombo(AggAvg("AvgPrice=Last"), AggSum("TotalShares=Size")), "Sym")

summaryTrim = summary.tail(9)

j1 = trades.join(summaryTrim, "Sym", "AvgPrice, TotalShares")

duplicate = emptyTable(1)
     .updateView("Sym=`MSFT`", "AvgPrice=72.9", "TotalShares=(long)23")

summaryDup = merge(summary, duplicate)

j2 = trades.join(summaryDup, "Sym", "AvgPrice, TotalShares")
        


//Left Join
trades = db.t("LearnDeephaven", "StockTrades").where("Date=`2017-08-25`")
     .headBy(3, "Sym")
     .view("Sym", "Last")

summary = trades.view("Sym", "AvgPrice=Last").avgBy("Sym")

lj = summary.leftJoin(trades, "Sym", "Last")
        


//As-of Join and Reverse As-of Join
trades = db.t("LearnDeephaven", "StockTrades")
     .where("Date=`2017-08-25`")
     .view("Sym", "TradeTime=ExchangeTimestamp", "Last")

quotes = db.t("LearnDeephaven", "StockQuotes")
     .where("Date=`2017-08-25`")
     .view("Sym", "QuoteTime=ExchangeTimestamp", "Bid", "Ask")

quotesMid = quotes.updateView("Mid=(Bid+Ask)/2").sort("Sym")

aj = trades.aj(quotesMid, "Sym, TradeTime=QuoteTime", "Mid, QuoteTime")

raj = trades.raj(quotesMid, "Sym, TradeTime=QuoteTime", "Mid, QuoteTime")
        
