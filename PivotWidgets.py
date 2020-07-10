##  Deephaven - Pivot Widgets Notebook - Python  
##  https://docs.deephaven.io/


from deephaven import *  # imports PivotWidgetBuilder

# generate shared tables
t = db.t("LearnDeephaven", "StockTrades").where("Date=`2017-08-21`")
t2 = db.t("LearnDeephaven", "StockQuotes").where("Date=`2017-08-25`")


# Basic Pivot Widget -  note this is directly using the java class
pw = PivotWidgetBuilder.pivot(t, "USym", "Exchange", "Last").show()


# Adding Row Columns
pwAddRows = PivotWidgetBuilder.pivot(t2, "USym", "Exchange", "Bid").addRows("Status").show()


# Adding Value Columns
pwAddValues = PivotWidgetBuilder.pivot(t, "USym", "Exchange", "Last").addValueColumns("Size").show()


# Adding Aggregations
pwAvg = PivotWidgetBuilder.pivot(t, "USym", "Exchange", "Last").addValueColumns("Size").avg().show()
pwSum = PivotWidgetBuilder.pivot(t, "USym", "Exchange", "Last").addValueColumns("Size").sum().show()


# Adding Grand Totals
pwTotals = PivotWidgetBuilder.pivot(t, "USym", "Exchange", "Last").addValueColumns("Size")\
    .sum().across().down().show()


# Adding Filter Columns
pwFilters = PivotWidgetBuilder.pivot(t, "USym", "Exchange", "Last").addValueColumns("Size")\
    .addFilterColumns("USym", "SaleCondition").sum().across().down().show()

# Renaming Header Columns
renamer = PythonFunction(lambda value: "Chic" if value == "Chicago" else value, 'java.lang.String')
pwRename = PivotWidgetBuilder.pivot(t, "USym", "Exchange", "Last")\
    .addValueColumns("Size")\
    .addFilterColumns("USym", "SaleCondition")\
    .sum()\
    .across()\
    .setColumnNameTransform(renamer)\
    .down()\
    .show()

# Color Formatting
A = PythonFunction(lambda value: "BLUE" if value < 50000 else "GREEN", 'java.lang.String')
pwA = PivotWidgetBuilder.pivot(t, "USym", "Exchange", "Last")\
    .sum()\
    .across()\
    .down()\
    .heatMap()\
    .setColorFormat(A)\
    .show()

DBColorUtilString = 'com.illumon.iris.db.util.DBColorUtil'
DBColorUtil = jpy.get_type(DBColorUtilString)
B = PythonFunction(lambda value: DBColorUtil.VIVID_GREEN if value < 50000 else DBColorUtil.VIVID_BLUE, DBColorUtilString)
pwB = PivotWidgetBuilder.pivot(t, "USym", "Exchange", "Last")\
    .sum()\
    .across()\
    .down()\
    .heatMap()\
    .setColorFormat(B)\
    .show()

C = PythonFunction(lambda value: DBColorUtil.toLong("BLUE") if value < 50000 else DBColorUtil.toLong("GREEN"), 'long')
pwC = PivotWidgetBuilder.pivot(t, "USym", "Exchange", "Last")\
    .sum()\
    .across()\
    .down()\
    .heatMap()\
    .setColorFormat(C)\
    .show()
