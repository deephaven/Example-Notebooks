//  Deephaven - Practical Example Recipes Notebook - Groovy 
//  https://docs.deephaven.io/


//Downsampling Trades
trades = db.t("LearnDeephaven", "StockTrades")
     .where("Date=`2017-08-25`")
     .view("Sym", "Last", "Size", "ExchangeTimestamp")

tradesByMin = trades.updateView("MinuteTimeBin=lowerBin(ExchangeTimestamp, MINUTE)")

downsample = tradesByMin.firstBy("Sym", "MinuteTimeBin")



//Price Volatility
summaries = db.t("LearnDeephaven", "EODTrades").where("ImportDate=`2017-11-01`")

returns = summaries.updateView("Return=(Ticker=Ticker_[i-1]) ? log(Close/Close_[i-1]) : NULL_DOUBLE")

std = returns.view("Ticker", "Std=Return").stdBy("Ticker")

trading_days = 252
annualization = sqrt(trading_days)

vol = std.updateView("Volatility=annualization*Std")



//Returns
trades = db.t("LearnDeephaven", "StockTrades")
     .where("Date=`2017-08-25`")
     .view("Sym", "Last", "ExchangeTimestamp")

tradesDayBefore = db.t("LearnDeephaven", "StockTrades")
     .where("Date=`2017-08-24`")
     .view("Sym", "Last", "ExchangeTimestamp")

currentDayLast = trades.lastBy("Sym")
dayBeforeLast = tradesDayBefore.lastBy("Sym")

dayDifference = currentDayLast.exactJoin(dayBeforeLast, "Sym", "YesterdayPrice=Last")
     .updateView("Change=Last-YesterdayPrice")

dayReturns = dayDifference.updateView("SimpleReturn=Change/YesterdayPrice", "LogReturn=log(Last/YesterdayPrice)")
     .formatColumns("SimpleReturn=Decimal(`0.000%`)", "LogReturn=Decimal(`0.000%`)")

tradesHourBefore = trades.updateView("HourBefore=ExchangeTimestamp - HOUR").sort("ExchangeTimestamp")

priceHourBefore = tradesHourBefore.aj(tradesHourBefore, "Sym, HourBefore=ExchangeTimestamp", "PriceHourBefore=Last")

removeEmpty = priceHourBefore.where("!isNull(PriceHourBefore)")
hourDifference = removeEmpty.updateView("Change=Last-PriceHourBefore", "SimpleReturn=Change/PriceHourBefore",
                                        "LogReturn=log(Last/PriceHourBefore)")
     .formatColumns("SimpleReturn=Decimal(`0.000%`)", "LogReturn=Decimal(`0.000%`)")



//Pair Trading Analysis
rollingAvg = { rows, values ->
    calculations = new double[values.size()]
    sum = 0
    n = 0
    // I'm unsure of the type for rows
    // values will be a dbArray instance, if it's defined via query

    for (long i = 0; i < values.size(); ++i)
    {
        if (i < rows) n++ //n increments with i until n=rows

        sum += values.get(i) //add each value to sum
        if (i >= rows) sum -= values.get((long)(i - rows)) //subtract when needed

        calculations[(int)i] = sum/n //store running average
    }
    return calculations //return an array of rolling averages
}

rollingStd = { rows, values ->
    // NOTE: variance(X) is most efficiently calculated as E(X^2) - E(X)^2

    calculations = new double[values.size()]
    n = 0
    sum = 0
    sum2 = 0
    // I'm unsure of the type for rows
    // values will be a dbArray instance, if it's defined via query

    for (long i = 0; i < values.size(); ++i)
    {
        if (i < rows) n++ //n increments with i until n=rows
        val = values.get(i)
        sum2 += val*val
        sum += val

        if (i >= rows){
            val = values.get((long)(i - rows))
            sum -= val //subtract when needed
            sum2 -= val*val
        }

        variance = sum2/n - (sum*sum)/(n*n)
        calculations[(int)i] = (variance > 0) ? Math.sqrt(variance): 0 // account for numerical imprecision near 0
    }

    return calculations
}

pair = ["MSFT", "GOOG"]
latestDate = "2017-08-21"

trades = db.t("LearnDeephaven", "StockTrades")
     .where("Date>=latestDate", "Sym in pair")
     .view("Date", "Sym", "Last")

lastPrices = trades.by(AggCombo(AggLast("Last")), "Date", "Sym")

sym0 = lastPrices.where("Sym=pair[0]")
     .renameColumns("SymA=Sym", "PriceA=Last")
sym1 = lastPrices.where("Sym=pair[1]")
     .renameColumns("SymB=Sym", "PriceB=Last")

sideBySide = sym0.naturalJoin(sym1, "Date")

returns = sideBySide.updateView("ReturnA=log(PriceA/PriceA_[i-1])", "ReturnB=log(PriceB/PriceB_[i-1])")
     .where("!isNull(ReturnA) && !isNull(ReturnB)")

calculations1 = returns.updateView("SquareA=ReturnA*ReturnA", "SquareB=ReturnB*ReturnB", "Product=ReturnA*ReturnB")

calculations2 = calculations1.by(AggCombo(AggCount("N"), AggSum("ReturnA", "ReturnB", "SquareA", "SquareB", "Product")))

correlation = calculations2.view("Correlation=((N * Product) - (ReturnA * ReturnB)) / sqrt((N * SquareA - pow(ReturnA, 2)) * (N * SquareB - pow(ReturnB, 2)))")
     .formatColumns("Correlation=Decimal(`0.000%`)")

priceRatio = sideBySide.updateView("PriceRatio=PriceA/PriceB")

rollingCalc = priceRatio.by()
        .updateView("RatioAvg=(double[])rollingAvg.call(20, PriceRatio)", "RatioStd=(double[])rollingStd.call(20, PriceRatio)")
        .ungroup()

zScore = rollingCalc.updateView("Zscore=(RatioStd != 0) ? (PriceRatio-RatioAvg)/RatioStd: NULL_DOUBLE",
        "UpperThreshold=RatioAvg+2*RatioStd", "LowerThreshold=RatioAvg-2*RatioStd")

dataFinal = zScore.updateView("Date=convertDateTime(Date+`T17:30 NY`)")

pricePlot = plot(pair[0], dataFinal, "Date", "PriceA")
     .twinX()
     .plot(pair[1], dataFinal, "Date", "PriceB")
     .show()

ratioPlot = plot("Ratio", dataFinal, "Date", "PriceRatio")     .plot("Average", dataFinal, "Date", "RatioAvg")
     .plot("Upper", dataFinal, "Date", "UpperThreshold")
     .lineStyle(lineStyle([4,4]))
     .plot("Lower", dataFinal, "Date", "LowerThreshold")
     .lineStyle(lineStyle([4,4]))
     .show()



//Mean Reversion Simulation
rollingAvg = { rows, values ->
    calculations = new double[values.size()]
    sum = 0
    n = 0
    // I'm unsure of the type for rows
    // values will be a dbArray instance, if it's defined via query

    for (long i = 0; i < values.size(); ++i)
    {
        if (i < rows) n++ //n increments with i until n=rows

        sum += values.get(i) //add each value to sum
        if (i >= rows) sum -= values.get((long)(i - rows)) //subtract when needed

        calculations[(int)i] = sum/n //store running average
    }
    return calculations //return an array of rolling averages
}

rollingStd = { rows, values ->
    // NOTE: variance(X) is most efficiently calculated as E(X^2) - E(X)^2

    calculations = new double[values.size()]
    n = 0
    sum = 0
    sum2 = 0
    // I'm unsure of the type for rows
    // values will be a dbArray instance, if it's defined via query

    for (long i = 0; i < values.size(); ++i)
    {
        if (i < rows) n++ //n increments with i until n=rows
        val = values.get(i)
        sum2 += val*val
        sum += val

        if (i >= rows){
            val = values.get((long)(i - rows))
            sum -= val //subtract when needed
            sum2 -= val*val
        }

        variance = sum2/n - (sum*sum)/(n*n)
        calculations[(int)i] = (variance > 0) ? Math.sqrt(variance): 0 // account for numerical imprecision near 0
    }

    return calculations
}

trades = db.t("LearnDeephaven", "StockTrades")
     .where("Date=`2017-08-25`")
     .view("Sym", "Last", "Size", "ExchangeTimestamp")

trades30min = trades.updateView("TimeBin=lowerBin(ExchangeTimestamp, 30*MINUTE)")
     .firstBy("Sym", "TimeBin")

rollingCalc = trades30min.by("Sym")
      .update("Avg=(double[])rollingAvg.call(30, Last)","Std=(double[])rollingStd.call(30, Last)")
      .ungroup()

minEdge = 0.5d
maxPos = 3.0d
liquidity = 1e6d

targetPos = rollingCalc.updateView("Zscore=(Std > 0) ? (Avg-Last)/Std : NULL_DOUBLE", "AdjZscore=signum(Zscore) * min(maxPos, max(abs(Zscore)-minEdge), 0.0)", "TargetPosition=(int)(liquidity*AdjZscore/Last)")
     .dropColumns("ExchangeTimestamp", "Avg", "Std", "Zscore", "AdjZscore")

timeBinIndexes = targetPos.leftJoin(trades30min, "Sym", "Times=ExchangeTimestamp, SharesTraded=Size")
     .updateView("StartIndex=binSearchIndex(Times, TimeBin-30*MINUTE, BS_LOWEST)", "EndIndex=binSearchIndex(Times, TimeBin, BS_HIGHEST)")
     .dropColumns("Times")

shares30min = timeBinIndexes.updateView("SharesTraded30Min=sum(SharesTraded.subArray(StartIndex, EndIndex))")
     .dropColumns("SharesTraded", "StartIndex", "EndIndex")


class SimulatorState
{
    private HashMap<String, double[]> hm = new HashMap<>()

    double[] update(String sym, int targetPos, int shares10s){
        if (!hm.containsKey(sym)) hm.put(sym, new double[2])

        double[] tradedAndPosition = hm.get(sym)

        tradedAndPosition[0] = isNull(targetPos) ? 0.0 : signum(targetPos - tradedAndPosition[1]) * min(abs(targetPos - tradedAndPosition[1]), shares10s * 0.1d)
        tradedAndPosition[1] += tradedAndPosition[0]

        return Arrays.copyOf(tradedAndPosition, tradedAndPosition.length)
   }
}
ss = new SimulatorState()

simulation = shares30min.update("Values=(double[])ss.update(Sym, TargetPosition, SharesTraded30Min)", "PositionChange=Values[0]", "Position=Values[1]")
      .dropColumns("Values")