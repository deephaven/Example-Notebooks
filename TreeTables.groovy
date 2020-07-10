//**************     Notebooks / Tree Tables / Groovy      ************** 
//**************   Copyright 2019 Deephaven Data Labs, LLC  ************** 

// This Notebook provides working examples for tutorial purposes, and is not meant to  
// replace the primaryDeephaven documentation, which can be found at 
// https://docs.deephaven.io/latest/Content/User/writeQueries/tableOperations/treeTables.htm.


// The following example creates a simple Tree Table branched by Sym
syms1 = db.t("LearnDeephaven","StockTrades")
    .firstBy("Sym")
    .updateView("ID=Sym","Parent=(String)null","Date=(String) null", "Timestamp=(com.illumon.iris.db.tables.utils.DBDateTime) null", "SecurityType = (String) null", "Exchange=(String)null", "Last=(Double) null", "Size=(Integer)null", "Source=(String)null", "ExchangeId=(Long) null", "ExchangeTimestamp=(DBDateTime)null", "SaleCondition=(String)null")

data1 = db.t("LearnDeephaven","StockTrades").where()
    .updateView("ID=Long.toString(k)","Parent=Sym")

combo1 = merge(syms1,data1)
			
comboTree1 = combo1.treeTable("ID","Parent")

//The following example creates a Tree Table with one main branch (Sym) and sub-level (Date)
syms2= db.t("LearnDeephaven" , "StockTrades").where()
    .firstBy("Sym")
    .updateView("ID=Sym","Parent=(String)null","Date=(String) null", "Timestamp=(com.illumon.iris.db.tables.utils.DBDateTime) null", "SecurityType = (String) null", "Exchange=(String)null","Last=(Double) null","Size=(Integer)null","Source=(String)null","ExchangeId=(Long) null","ExchangeTimestamp=(DBDateTime)null","SaleCondition=(String)null", "USym=(String)null")

dates2 = db.t("LearnDeephaven","StockTrades").where()
    .firstBy("Sym","Date")
    .updateView("ID=Sym+Date", "Parent=Sym", "Timestamp=(com.illumon.iris.db.tables.utils.DBDateTime)null", "SecurityType=(String)null", "Exchange=(String)null", "Last=(Double) null", "Size=(Integer)null","Source=(String)null", "ExchangeId=(Long) null", "ExchangeTimestamp=(DBDateTime)null", "SaleCondition=(String)null","USym=(String)null")

data2 = db.t("LearnDeephaven","StockTrades").where()
    .updateView("ID=Long.toString(k)","Parent=Sym+Date")

combo2 = merge(dates2,syms2,data2)

comboTree2 = combo2.treeTable("ID","Parent")