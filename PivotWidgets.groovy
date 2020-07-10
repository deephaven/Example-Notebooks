//**************     Notebooks / Pivot Widgets / Groovy      ************** 
//**************   Copyright 2019 Deephaven Data Labs, LLC  ************** 

// This Notebook provides working examples for tutorial purposes, and is not meant to 
// replace the primary Deephaven documentation, which can be found at 
// https://docs.deephaven.io/latest/Content/User/writeQueries/tableOperations/pivotWidgets.htm.


//Run this block to import the PivotWidgetBuilder class and open the source tables used in each example

import com.illumon.iris.console.utils.PivotWidgetBuilder
t=db.t("LearnDeephaven", "StockTrades").where("Date=`2017-08-21`")
t2=db.t("LearnDeephaven", "StockQuotes").where("Date=`2017-08-25`")

//Basic Pivot Widget
pw = PivotWidgetBuilder.pivot(t, "USym", "Exchange", "Last").show()



//Adding Row Columns
pwAddRows = PivotWidgetBuilder.pivot(t2, "USym", "Exchange", "Bid")
    .addRows("Status")
    .show()



//Adding Value Columns
pwAddValues = PivotWidgetBuilder.pivot(t, "USym", "Exchange", "Last")
    .addValueColumns("Size")
    .show()



//Adding Aggregations
pwAvg = PivotWidgetBuilder.pivot(t, "USym", "Exchange", "Last")
    .addValueColumns("Size")
    .avg()
    .show()

pwSum = PivotWidgetBuilder.pivot(t, "USym", "Exchange", "Last")
    .addValueColumns("Size")
    .sum()
    .show()



//Adding Grand Totals
pwTotals = PivotWidgetBuilder.pivot(t, "USym", "Exchange", "Last")
    .addValueColumns("Size")
    .sum()
    .across()
    .down()
    .show()



//Adding AutoFilter Columns
pwFilters = PivotWidgetBuilder.pivot(t, "USym", "Exchange", "Last")
    .addValueColumns("Size")
    .addFilterColumns("USym", "SaleCondition")
    .sum()
    .across()
    .down()
    .show()

//Adding Required Filter Columns
pwReqFilters = PivotWidgetBuilder.pivot(t, "USym", "Exchange", "Last")
    .addValueColumns("Size")
    .addFilterColumns("SaleCondition")
    .requireFilteredColumns("Sym")
    .sum()
    .across()
    .down()
    .show()



//Renaming Header Columns
pwRename = PivotWidgetBuilder.pivot(t, "USym", "Exchange", "Last")
    .addValueColumns("Size")
    .addFilterColumns("USym", "SaleCondition")
    .sum().across()
    .setColumnNameTransform({value -> value.equals("Chicago") ? "Chic" : value})
    .down()
    .show()



//Color Formatting
A = {val -> val < 50000 ? "BLUE" : "GREEN"}
pwA = PivotWidgetBuilder.pivot(t, "USym", "Exchange", "Last")
    .sum().across().down()
    .heatMap().setColorFormat(A)
    .show()

import com.illumon.iris.db.util.DBColorUtil
B = {val -> val < 50000 ? DBColorUtil.VIVID_GREEN : DBColorUtil.VIVID_BLUE}
pwB = PivotWidgetBuilder.pivot(t, "USym", "Exchange", "Last")
    .sum().across().down()
    .heatMap().setColorFormat(B)
    .show()

import com.illumon.iris.db.util.DBColorUtil
a = DBColorUtil.toLong("BLUE")
b = DBColorUtil.toLong("GREEN")
C = {val -> val < 50000 ? a : b}
pwC = PivotWidgetBuilder.pivot(t, "USym", "Exchange", "Last")
    .sum().across().down()
    .heatMap().setColorFormat(C)
    .show()