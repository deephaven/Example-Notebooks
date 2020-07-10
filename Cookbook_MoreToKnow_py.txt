##  Deephaven - More to Know Recipes Notebook - Python  
##  https://docs.deephaven.io/


from deephaven import *
# See print(sorted(dir())) or help('deephaven') for full namespace contents.


# Table Tricks
trades = db.t("LearnDeephaven", "StockTrades").where("Date=`2017-08-25`")\
    .view("Date", "ExchangeTimestamp", "Sym", "Last")\
    .head(10)
time = dbtu.convertDateTime("2017-08-25T04:05:00 NY")
custom = ttools.emptyTable(1).updateView("Date=`2017-08-25`", "ExchangeTimestamp=time", "Sym=`AAPL`", "Last=160.0")

tradesNewRow = ttools.merge(trades, custom)
indexes = tradesNewRow.updateView("Index=i")
prevPrice = indexes.updateView("PreviousPrice=Last_[i-1]")
difference = prevPrice.updateView("Difference=Last-PreviousPrice")
meta = difference.getMeta()
cellValue = difference.getColumn("Difference").get(5)
print (cellValue)


# Bad Math
trades = db.t("LearnDeephaven", "StockTrades").where("Date=`2017-08-25`")\
    .view("Date", "ExchangeTimestamp", "Sym", "Last", "Size")
dollarsInvolved = trades.updateView("DollarsInvolved=Last*Size")
summary = dollarsInvolved.by(caf.AggCombo(caf.AggSum("TotalSize=Size", "TotalDollars=DollarsInvolved"), caf.AggLast("Last")), "Sym")
spx = ttools.emptyTable(1).updateView("Sym=`SPX`", "TotalSize=(long)0", "TotalDollars=0.0", "Last=2465.84")
withSpx = ttools.merge(summary, spx)
vwap = withSpx.updateView("VWAP=TotalDollars/TotalSize")
sum = vwap.view("VWAP").sumBy()
vwapIfThen = withSpx.updateView("VWAP = (TotalSize != 0) ? (TotalDollars/TotalSize) : NULL_DOUBLE")
sumIfThen = vwapIfThen.view("VWAP").sumBy()
checkNull = vwapIfThen.updateView("NullOrNot=isNull(VWAP)")


# Custom Functionality


def rollingSum(rows, values):
    """
    Calculate a rolling sum from a java int array
    :param rows: size of the rolling sum (i.e. number of rows to sum over)
    :param values:
    :return:
    """

    calculations = jpy.array('int', values.size())  # create an array of integers for our rolling sum value
    sum_ = 0  # our running sum (avoid builtin symbol sum)

    for i in range(values.size()):
        sum_ += values.get(i)
        if i >= rows:
            # subtract from the rolling sum when needed
            sum_ -= values.get(i - rows)
        calculations[i] = sum_

    return calculations


trades = db.t("LearnDeephaven", "StockTrades").where("Date=`2017-08-25`")\
    .view("Sym", "Size")\
    .headBy(100, "Sym")

symGroups = trades.by("Sym")
sums = symGroups.updateView("RollingSum=(int[]) rollingSum.call(20, Size)")
ungroup = sums.ungroup()
