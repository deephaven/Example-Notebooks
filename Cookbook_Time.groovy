//  Deephaven - Time Recipes Notebook - Groovy 
//  https://docs.deephaven.io/


//Filtering By Time
trades = db.t("LearnDeephaven", "StockTrades")
     .where("Date=`2017-08-25`")
     .view("Sym", "Last", "ExchangeTimestamp")
usnyseTime = trades.where("CALENDAR_USNYSE.isBusinessTime(ExchangeTimestamp)")
afternoon = usnyseTime.where("ExchangeTimestamp > '2017-08-25T12:00 NY'")



//DateTime Strings
trades = db.t("LearnDeephaven", "StockTrades")
     .where("Date=`2017-08-25`")
     .view("Sym", "Last", "ExchangeTimestamp")

time = convertDateTime("2017-08-25T12:00 NY")

afternoon = trades.where("ExchangeTimestamp > time")
        


//Adjusting Time Mathematically
trades = db.t("LearnDeephaven", "StockTrades")
     .where("Date=`2017-08-25`")
     .view("Sym", "Last", "ExchangeTimestamp")

hourAfter1 = trades.updateView("HourAfter = ExchangeTimestamp + '01:00'")
hourAfter2 = trades.updateView("HourAfter = ExchangeTimestamp + 'T1h'")
hourAfter3 = trades.updateView("HourAfter = ExchangeTimestamp + HOUR")

timeDifference = trades.updateView("Difference=diffDay(ExchangeTimestamp, currentTime())")
        