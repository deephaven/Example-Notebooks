##  Deephaven - Practical Examples Notebook - Python  
##  https://docs.deephaven.io/


import numpy
import math
from collections import defaultdict

from deephaven import *
# See print(sorted(dir())) or help('deephaven') for full namespace contents.

# generate shared tables
trades = db.t("LearnDeephaven", "StockTrades")\
     .where("Date=`2017-08-25`")\
     .view("Sym", "Last", "Size", "ExchangeTimestamp")


# Downsampling Trades
tradesByMin = trades.updateView("MinuteTimeBin=lowerBin(ExchangeTimestamp, MINUTE)")
downsample = tradesByMin.firstBy("Sym", "MinuteTimeBin")


# Price Volatility
summaries = db.t("LearnDeephaven", "EODTrades").where("ImportDate=`2017-11-01`")
returns = summaries.updateView("Return=(Ticker=Ticker_[i-1]) ? log(Close/Close_[i-1]) : NULL_DOUBLE")
std = returns.view("Ticker", "Std=Return").stdBy("Ticker")
trading_days = 252
annualization = trading_days ** 0.5
vol = std.updateView("Volatility=annualization*Std")


# Returns
tradesDayBefore = db.t("LearnDeephaven", "StockTrades")\
    .where("Date=`2017-08-24`")\
    .view("Sym", "Last", "ExchangeTimestamp")

currentDayLast = trades.lastBy("Sym")
dayBeforeLast = tradesDayBefore.lastBy("Sym")
dayDifference = currentDayLast.exactJoin(dayBeforeLast, "Sym", "YesterdayPrice=Last")\
    .updateView("Change=Last-YesterdayPrice")
dayReturns = dayDifference.updateView("SimpleReturn=Change/YesterdayPrice", "LogReturn=log(Last/YesterdayPrice)")\
    .formatColumns("SimpleReturn=Decimal(`0.000%`)", "LogReturn=Decimal(`0.000%`)")

tradesHourBefore = trades.updateView("HourBefore=ExchangeTimestamp - HOUR").sort("ExchangeTimestamp")
priceHourBefore = tradesHourBefore.aj(tradesHourBefore, "Sym, HourBefore=ExchangeTimestamp", "PriceHourBefore=Last")
removeEmpty = priceHourBefore.where("!isNull(PriceHourBefore)")
hourDifference = removeEmpty.updateView("Change=Last-PriceHourBefore", "SimpleReturn=Change/PriceHourBefore",
                                        "LogReturn=log(Last/PriceHourBefore)")\
    .formatColumns("SimpleReturn=Decimal(`0.000%`)", "LogReturn=Decimal(`0.000%`)")


########################################################
# Creating custom methods
#
# NOTE: these can also be accomplished using numpy, but the intent here
#       is to simply demonstrate using python functions inside queries

def rollingAvg(rows, values):
    """
    Perform a rolling average over a java array

    :param rows: size of the rolling average (i.e. number of rows to average over)
    :param values: java array of values
    :return: java double array of rolling averages
    """
    calculations = jpy.array('double', values.size())  # create an array of doubles
    sum_ = 0  # rolling sum (avoid builtin symbol sum)
    count = 0  # how many element in our rolling sum
    for i in range(values.size()):
        sum_ += values.get(i)  # add each value to sum

        if i < rows:
            count += 1  # count increments until = rows
        else:
            # subtract from rolling sum when needed
            sum_ -= values.get(i - rows)

        calculations[i] = sum_/float(count)

    return calculations


def rollingStd(rows, values):
    """
    Rolling standard deviation over a java array
    :param rows: size of the rolling average (i.e. number of rows to calculate over)
    :param values: java array of values
    :return: java double array of rolling std deviation
    """

    calculations = jpy.array('double', values.size())  # create an array of doubles
    sum_ = 0  # rolling sum (avoid builtin symbol sum)
    sum_2 = 0  # rolling sum of squared elements
    count = 0  # how many element in our rolling sum
    for i in range(values.size()):
        val = values.get(i)
        sum_ += val
        sum_2 += val*val

        if i < rows:
            count += 1  # count increments until = rows
        else:
            # subtract from rolling sum when needed
            val2 = values.get(i-rows)
            sum_ -= val2
            sum_2 -= val2*val2

        variance = sum_2/float(count) - (sum_*sum_/(count*count))
        calculations[i] = variance**0.5 if variance > 0 else 0.0  # account for numerical precision near 0

    return calculations

#######################


# Pair Trading Analysis

pair = convertJavaArray(['MSFT', 'GOOG'])  # create java array of desired symbols
latestDate = "2017-08-21"
trades = db.t("LearnDeephaven", "StockTrades")\
    .where("Date>=latestDate", "Sym in pair")\
    .view("Date", "Sym", "Last")
    
lastPrices = trades.by(caf.AggCombo(caf.AggLast("Last")), "Date", "Sym")

p0 = pair[0]
p1 = pair[1]
sym0 = lastPrices.where("Sym=p0").renameColumns("SymA=Sym", "PriceA=Last")
sym1 = lastPrices.where("Sym=p1").renameColumns("SymB=Sym", "PriceB=Last")
sideBySide = sym0.naturalJoin(sym1, "Date")

returns = sideBySide.updateView("ReturnA=log(PriceA/PriceA_[i-1])", "ReturnB=log(PriceB/PriceB_[i-1])")\
    .where("!isNull(ReturnA) && !isNull(ReturnB)")
calculations1 = returns.updateView("SquareA=ReturnA*ReturnA", "SquareB=ReturnB*ReturnB", "Product=ReturnA*ReturnB")
calculations2 = calculations1.by(caf.AggCombo(caf.AggCount("N"),caf.AggSum("ReturnA", "ReturnB", "SquareA", "SquareB", "Product")))

# note that values inside the strings passed to query are interpreted BY JAVA - sqrt & pow are java methods
correlation = calculations2.view("Correlation=(N*(Product - (ReturnA*ReturnB)))/"
                                 "sqrt((N*SquareA - pow(ReturnA, 2))*(N*SquareB - pow(ReturnB, 2)))")\
    .formatColumns("Correlation=Decimal(`0.000%`)")
priceRatio = sideBySide.updateView("PriceRatio=PriceA/PriceB")
rollingCalc = priceRatio.by().updateView("RatioAvg=(double[])rollingAvg.call(20, PriceRatio)",\
    "RatioStd=(double[])rollingStd.call(20, PriceRatio)")\
    .ungroup()
    
zScore = rollingCalc.updateView("Zscore=(PriceRatio-RatioAvg)/RatioStd",\
                                "UpperThreshold=RatioAvg+2*RatioStd",\
                                "LowerThreshold=RatioAvg-2*RatioStd")
                                
dataFinal = zScore.updateView("Date=convertDateTime(Date+`T17:30 NY`)")

#############################


############################################################
# Mean Reversion Simulation

trades = db.t("LearnDeephaven", "StockTrades")\
    .where("Date=`2017-08-25`")\
    .view("Sym", "Last", "Size", "ExchangeTimestamp")

trades30min = trades.updateView("TimeBin=lowerBin(ExchangeTimestamp, 30*MINUTE)")\
    .firstBy("Sym", "TimeBin")

rollingCalc = trades30min.by("Sym")\
    .update("Avg=(double[])rollingAvg.call(30, Last)", "Std=(double[])rollingStd.call(30, Last)")\
    .ungroup()

minEdge = 0.5
maxPos = 3.0
liquidity = 1e6

targetPos = rollingCalc.updateView("Zscore= Std > 0 ? (Avg-Last)/Std : NULL_DOUBLE",\
                                   "AdjZscore=signum(Zscore) * min(maxPos, max(abs(Zscore)-minEdge), 0.0)",\
                                   "TargetPosition=(int)(liquidity*AdjZscore/Last)")\
    .dropColumns("ExchangeTimestamp", "Avg", "Std", "Zscore", "AdjZscore")

timeBinIndexes = targetPos.leftJoin(trades30min, "Sym", "Times=ExchangeTimestamp, SharesTraded=Size")\
    .updateView("StartIndex=binSearchIndex(Times, TimeBin-30*MINUTE, BS_LOWEST)",\
                "EndIndex=binSearchIndex(Times, TimeBin, BS_HIGHEST)")\
    .dropColumns("Times")

shares30min = timeBinIndexes.updateView("SharesTraded30Min=sum(SharesTraded.subArray(StartIndex, EndIndex))")\
    .dropColumns("SharesTraded", "StartIndex", "EndIndex")


class SimulatorState:
    def __init__(self):
        self.hm = defaultdict(self.default_factory)

    @staticmethod
    def default_factory():
        return numpy.zeros((2,), dtype=numpy.float64)

    def __call__(self, sym, targetPos, shares10s):
        tradedAndPosition = self.hm[sym]  # defaults to {0.0, 0.0} initially
        posChange = 0.0
        currentPos = tradedAndPosition[1]
        if targetPos is not None:
            posChange = math.copysign(1, targetPos - currentPos)*min(abs(targetPos - currentPos), shares10s*0.1)
        tradedAndPosition[0] = posChange
        tradedAndPosition[1] += posChange
        self.hm[sym] = tradedAndPosition
        return convertToJavaArray(tradedAndPosition)  # java array of type double


ss = SimulatorState()
simulation = shares30min.update("Values=(double[])ss.call(Sym, TargetPosition, SharesTraded30Min)",\
                                "PositionChange=Values[0]", "Position=Values[1]")\
    .dropColumns("Values")
