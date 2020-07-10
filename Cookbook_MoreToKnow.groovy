//  Deephaven - More To Know Recipes Notebook - Groovy 
//  https://docs.deephaven.io/


//Table Tricks
trades = db.t("LearnDeephaven", "StockTrades").where("Date=`2017-08-25`")
     .view("Date", "ExchangeTimestamp", "Sym", "Last")
     .head(10)

time = convertDateTime("2017-08-25T04:05:00 NY")
custom = emptyTable(1).updateView("Date=`2017-08-25`", "ExchangeTimestamp=time", "Sym=`AAPL`", "Last=160.0")

tradesNewRow = merge(trades, custom)

indexes = tradesNewRow.updateView("Index=i")

prevPrice = indexes.updateView("PreviousPrice=Last_[i-1]")

difference = prevPrice.updateView("Difference=Last-PreviousPrice")

meta = difference.getMeta()

cellValue = difference.getColumn("Difference").get(5)
println cellValue


//Bad Math
trades = db.t("LearnDeephaven", "StockTrades").where("Date=`2017-08-25`")
     .view("Date", "ExchangeTimestamp", "Sym", "Last", "Size")

dollarsInvolved = trades.updateView("DollarsInvolved=Last*Size")

summary = dollarsInvolved.by(AggCombo(AggSum("TotalSize=Size", "TotalDollars=DollarsInvolved"), AggLast("Last")), "Sym")

spx = emptyTable(1).updateView("Sym=`SPX`", "TotalSize=(long)0", "TotalDollars=0.0", "Last=2465.84")
withSpx = merge(summary, spx)

vwap = withSpx.updateView("VWAP=TotalDollars/TotalSize")

sum = vwap.view("VWAP").sumBy()

vwapIfThen = withSpx.updateView("VWAP =(TotalSize != 0) ? (TotalDollars/TotalSize) : NULL_DOUBLE")

sumIfThen = vwapIfThen.view("VWAP").sumBy()

checkNull = vwapIfThen.updateView("NullOrNot=isNull(VWAP)")


//Custom Functionality
trades = db.t("LearnDeephaven", "StockTrades").where("Date=`2017-08-25`")
     .view("Sym", "Size")
     .headBy(100, "Sym")
symGroups = trades.by("Sym")

rollingSum = { rows, values ->
     // Create a new array that will store rolling sum calculations
     calculations = new int[values.size()]

     // Create a running sum
     sum = 0

     //Iterate through each value in the array
     for (int i = 0; i < values.size(); ++i)
     {
          // Add the current value to the running sum
          sum += values.get(i)

          // Subtract the outdated value
          if (i >= rows) sum -= values.get(i - rows)

          // Store the rolling sum upon each iteration
          calculations[i] = sum
     }

     // Return the array of rolling sums
     return calculations
}

sums = symGroups.updateView("RollingSum=(int[]) rollingSum.call(20, Size)")

ungroup = sums.ungroup()
