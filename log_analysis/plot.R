library(zoo)
options(digits.secs = 3)

fileTable <- read.delim('../bee_model.log', header = FALSE, sep='\n')
nectarFound <- data.frame(matrix(ncol = 3))
colnames(nectarFound) <- c('Time', 'Bee', 'flower')
dates <- c()
nectarCnt <- 0
nectarCum <- c()
for(i in 1:nrow(fileTable)) {
  line <- fileTable[i,]
  begin <- strtrim(fileTable[i,], 4);
  if(begin == "INFO") {
    # Get Info
    action <- strsplit(line, ": ")[[1]][2]
    if(action == 'NECTAR FOUND') {
      # add to data frame
      bee <- strsplit(fileTable[i + 1,], ": ")[[1]][2]
      flower <- strsplit(fileTable[i + 2,], ": ")[[1]][2]
      # TODO fix with hour two digits
      date <- substr(fileTable[i - 1,], 1, 28)
      nectarFound <- rbind(nectarFound, c(date, bee, flower))
      date <- as.numeric(as.POSIXct(date, format = "%b %d, %Y %l:%M:%OS %p"))
      dates <- c(dates, date)
      nectarCnt <- nectarCnt + 1
      nectarCum <- c(nectarCum, nectarCnt)
    }
  }
}
nectarFound <- nectarFound[-c(1),]
nectarFound$dates <- dates
nectarFound$cumulative <- nectarCum

plot(irts(dates, nectarCum))

nectarFound$step <- 1

library(plyr)
df <- ddply(nectarFound, .(), transform, step=cumsum(step))
plot(step~dates, data=df, type="s")



