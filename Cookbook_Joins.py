##  Deephaven - Joins Recipes Notebook - Python  
##  https://docs.deephaven.io/


from deephaven import *
# See print(sorted(dir())) or help('deephaven') for full namespace contents.


# generate shared tables
trades = db.t("LearnDeephaven", "StockTrades")\
   .where("Date=`2017-08-25`")\
   .headBy(3, "Sym")\
   .view("Sym", "Last", "Size")

summary = trades.by(caf.AggCombo(caf.AggAvg("AvgPrice=Last"), caf.AggSum("TotalShares=Size")), "Sym").update("TotalShares=(long)TotalShares")
summaryTrim = summary.tail(9)


# Exact Join
ej = trades.exactJoin(summary, "Sym", "AvgPrice, TotalShares")


# Natural Join Recipe
nj = trades.naturalJoin(summaryTrim, "Sym", "AvgPrice, NewNameTotShares = TotalShares")
#  Note: In this case, TotalShares from the right table is renamed  
#  to NewNameTotShares when it is joined to the left table.  This is    
#  important for cases where the left table already has TotalShares
#  as a column name, for example. 


# Join
j1 = trades.join(summaryTrim, "Sym", "AvgPrice, TotalShares")
duplicate = ttools.emptyTable(1).updateView("Sym=`MSFT`", "AvgPrice=72.9", "TotalShares=(long)23")
# Note: using (long)23 ensures 23 is interpreted as type long

summaryDup = ttools.merge(summary, duplicate)
j2 = trades.join(summaryDup, "Sym", "AvgPrice, TotalShares")


# Left Join
lj = summary.leftJoin(trades, "Sym", "Last")


# As-of Join and Reverse As-of Join
trades = db.t("LearnDeephaven", "StockTrades")\
   .where("Date=`2017-08-25`")\
   .view("Sym", "TradeTime=ExchangeTimestamp", "Last")

quotes = db.t("LearnDeephaven", "StockQuotes")\
   .where("Date=`2017-08-25`")\
   .sort("ExchangeTimestamp")\
   .view("Sym", "QuoteTime=ExchangeTimestamp", "Bid", "Ask")
    
quotesMid = quotes.updateView("Mid=(Bid+Ask)/2")

aj = trades.aj(quotesMid, "Sym=Sym,TradeTime=QuoteTime", "Mid, QuoteTime")
raj = trades.raj(quotesMid, "Sym=Sym,TradeTime=QuoteTime", "Mid, QuoteTime")
