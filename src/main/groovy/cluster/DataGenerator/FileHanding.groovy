package cluster.DataGenerator

class FileHanding {

    String outFileName, inFileName
    int numberWorkers
    File outFile = null
    FileReader reader = null
    FileWriter writer = null

    LexingMethods gppLex = new LexingMethods()

    def openFiles (String inName, String outName) {
        inFileName = inName
        outFileName = outName
        outFile = new File(outFileName)
        if (outFile.exists())outFile.delete()
        reader = new FileReader(inFileName)
        writer = new FileWriter(outFile)
    }

    def readInFile = {
        gppLex.getInput(reader)
    }

    def writeOutFile = {
        gppLex.putOutput(writer)
    }

    String parse (int num) {
        readInFile()
        switch (num){
            case 1:
                numberWorkers = gppLex.findNumberWorkers()
                break
            case 2:
            gppLex.with{
                processImports()
                processPreNetwork()
                boolean processing = true
                while (processing){
                    def rvs = findProcDef(currentLine)
                    if (rvs == null) break
                    endLine = rvs[0]
                    String processName = rvs[1]
                    processNames << rvs[2]
                    // use the name of the process as the method call using Groovy GString
                    println "calling $processName, $currentLine, $endLine"
                    "$processName"(processName, currentLine, endLine)
                    if ((processName =~ /Collect/) || (processName =~ /Pattern/) ) {
                        processing = false
                        currentLine = endLine + 1
                    }
                    else findNextProc()
                }
                processPostNetwork()
            }
            writeOutFile()
            return gppLex.error
            case 3:
                break
            default:
                break
        }

    }

}

