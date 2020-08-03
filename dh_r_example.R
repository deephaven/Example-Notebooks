
home <- "/Users/userName"
system <- "dh-prod-demo"
keyfile <- sprintf("%s/.priv.dh-prod-demo.base64.txt",home)
workerHeapGB <- 4
jvmHeapGB <- 2
workerHost <- "dh-prod-demo-query4.int.illumon.com"

# Run the following to get java details:  R CMD javareconf
Sys.setenv(JAVA_HOME = '/Library/Java/JavaVirtualMachines/jdk1.8.0_121.jdk/Contents/Home/')
source(sprintf("%s/iris/.programfiles/%s/integrations/r/irisdb.R",home,system))

#' Initialize the Deephaven DB connection
#'
#' @param devroot devroot for Deephaven installation - must include trailing path separator
#' @param workspace Java workspace directory
#' @param propfile Iris Java propfile
#' @param userHome User's home directory
#' @param keyfile Path to private key file for DB user authentication
#' @param log4jconffile Log4j config file
#' @param librarypath Java library path
#' @param workerHeapGB Desired worker heap
#' @param jvmHeapGB Desired jvm heap
#' @param jvmArgs Java JVM arguments
#' @param verbose enable/disale verbose output
#' @param classpathAdditions Additional entries to add to the start of the classpath
#' @param jvmForceInit Force reinitialization of the JVM
idb.init(devroot = sprintf("%s/iris/.programfiles/%s/",home,system), 
         workspace = sprintf("%s/iris/workspaces/%s/workspaces/r/",home,system), 
         propfile = "iris-common.prop", 
         userHome = home,
         keyfile = keyfile, 
         librarypath = sprintf("%s/iris/.programfiles/%s/java_lib",home,system), 
         log4jconffile = NULL,
         workerHeapGB = workerHeapGB, 
         jvmHeapGB = jvmHeapGB,
         workerHost = workerHost,
         verbose = TRUE, 
         jvmArgs = c("-Dservice.name=iris_console",sprintf("-Ddh.config.client.bootstrap=%s/iris/.programfiles/%s/dh-config/clients",home,system)),
         classpathAdditions = c(sprintf("%s/iris/.programfiles/%s/resources",home,system),sprintf("%s/iris/.programfiles/%s/java_lib",home,system)),
         jvmForceInit = FALSE)

# Compute x=1+2 on the server, then pull the value back to the client.
idb.execute("x=1+2")
x <- idb.get("x")
print(x)

# Perform a basic DB opertaion on the server and pull the table back to the R client as a dataframe.
idb.execute('t = db.t("LearnDeephaven","StockTrades"); t1=t.countBy("Count","Date")')
t1 <- idb.get.df("t1")
print(t1)

# Manipulate the dataframe and then push it to the server with name "t2"
t2 <- t1[2:3,]
print(t2)
idb.push.df("t2",t2)

# Perform a more complex DB calculation using the dataframe, and pull the result to the R client as a dataframe.
idb.execute('t3 = t.whereIn(t2,"Date").view("USym","Dollars=Last*Size").sumBy("USym").sortDescending("Dollars")')
t3 <- idb.get.df("t3")
print(t3)

