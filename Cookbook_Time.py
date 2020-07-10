##  Deephaven - Time Recipes Notebook - Python  
##  https://docs.deephaven.io/


from deephaven import *
# See print(sorted(dir())) or help('deephaven') for full namespace contents.

# generate shared tables
trades = db.t("LearnDeephaven", "StockTrades")\
    .where("Date=`2017-08-25`")\
    .view("Sym", "Last", "ExchangeTimestamp")

#########################


# Filtering By Time
usnyse = cals.calendar("USNYSE")  # select business calendar using Calendars module method
usnyseTime = trades.where("usnyse.isBusinessTime(ExchangeTimestamp)")  # limit to trades during USNYSE business hours
afternoon = usnyseTime.where("ExchangeTimestamp > '2017-08-25T12:00 NY'")  # limit additionally to afternoon hours


# DateTime Strings
time = dbtu.convertDateTime("2017-08-25T12:00 NY")  # parse string to DBDateTime using DBTimeUtils module method
afternoon = trades.where("ExchangeTimestamp > time")  # limit again to afternoon hours


# Adjusting Time Mathematically
hourAfter1 = trades.updateView("HourAfter = ExchangeTimestamp + '01:00'")
hourAfter2 = trades.updateView("HourAfter = ExchangeTimestamp + 'T1h'")
hourAfter3 = trades.updateView("HourAfter = ExchangeTimestamp + HOUR")
timeDifference = trades.updateView("Difference=diffDay(ExchangeTimestamp, currentTime())")
