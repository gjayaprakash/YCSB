#
# Simple python script to parse ycsb stats output into csv files for graphing.
#

import sys
import re

# Default output file name
filename = 'out.csv'

#
# Print basic usage information
#

def printUsage():
    print "parse_output.py <file name>"

#
# Print a header for the CSV file that represents the column names
# 

def writeHeader(outfile):
    outfile.write("Time, Operations, TPS, READ Average Latencies, UPDATE Average Latencies, INSERT Average Latencies\n")
    
#
# Parse out useful information from useful log lines and write out to file. 
# Ignore lines starting with 'nnn sec'.
#

def writeLine(line, outfile):
    line = line.strip()
    if(re.match('\d+\s+sec', line)):
        recordList = splitLogLine(line)
        outLine = ",".join(recordList)
        outfile.write(outLine+"\n")

# 
# Split a log line by ';' and return values for each column
#

def splitLogLine(line):
    fields = []
    # [0] = 123 sec: 123 operations, [1] 123 ops/sec, [2] 
    # [UPDATE AverageLatency(ms)=5.38] [READ AverageLatency(ms)=30.58]
    splitLines = line.split(';')
    fields.extend(getTimeAndOps(splitLines[0]))
    if len(splitLines) > 1:
        fields.append(getTPS(splitLines[1]))
    if len(splitLines) > 2:
        fields.extend(getLatency(splitLines[2]))

    return fields

# 
# Parse out the Time and number of operations string.
#
def getTimeAndOps(timeOpsString):
    [time, ops] = timeOpsString.split(':')
    time = re.match("(\d+)", time.strip()).group(1)
    ops = re.match("(\d+)", ops.strip()).group(1)
    return [time, ops]

# 
# Get Operations/second
#

def getTPS(tpsString):
    if (tpsString.strip() == ""):
        return ""
    tps = re.match("([\d\.]+)", tpsString.strip()).group(1)
    return tps

# 
# Get latency
#

def getLatency(updateString):
    if (updateString.strip() == ""):
        return ["", "", ""]
    readLatency = re.search("READ\s.*?=([\d\.]+)", updateString.strip())
    updateLatency = re.search("UPDATE\s.*?=([\d\.]+)", updateString.strip())
    insertLatency = re.search("INSERT\s.*?=([\d\.]+)", updateString.strip())
    insertLatency = re.search("READ-MODIFY-WRITE\s.*?=([\d\.]+)", updateString.strip())
    if(not readLatency):
        readLatency = ""
    else :
        readLatency = readLatency.group(1)
    if(not updateLatency):
        updateLatency = ""
    else :
        updateLatency = updateLatency.group(1)
    if(not insertLatency):
        insertLatency = ""
    else :
        insertLatency = insertLatency.group(1)
    return [readLatency, updateLatency, insertLatency]

# 
# Entry point.
#

def main():
    if(len(sys.argv) == 2):
        filename = sys.argv[1]
    elif (len(sys.argv) > 2):
        print "Too many arguments"
        printUsage()
        sys.exit(1)

    csvFilename = filename.split('.')[0]+'.csv'
    infile = open(filename, 'r')
    outfile = open(csvFilename, 'w')
    writeHeader(outfile)

    for line in infile.readlines():
        writeLine(line, outfile)
    print "Wrote file to "+csvFilename
    sys.exit(0)

if __name__ == "__main__":
    main()
