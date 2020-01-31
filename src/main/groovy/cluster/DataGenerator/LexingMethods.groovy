package cluster.DataGenerator

import GPP_Builder.ChanTypeEnum
class LexingMethods {

    String error = ""

    String preNetwork = "\n"
    String network = "\n"
    String postNetwork = "\n"

    String logChanAdd = "    visLogChan : logChan.out(),\n"

    def processNames = []
    def chanNumber = 1
    String currentOutChanName = "chan$chanNumber"
    String currentInChanName = "chan$chanNumber"
    ChanTypeEnum expectedInChan
    String chanSize

    List<String> inText = []
    List<String> outText = []
    int currentLine = 1
    int endLine = 0

    boolean pattern = false
    boolean logging = false

    //SH added: need logFileName gppVis command readLog()
    String logFileName

    def getInput(FileReader reader) {
        reader.each { String line ->
            if (line.size() == 0) line = " " else line = line.trim()
            inText << line
        }
        // copy package line to outText
        outText << inText[0] + "\n\n"
        outText << "import jcsp.lang.*\nimport groovyJCSP.*\n"
        reader.close()
    }

    def putOutput(FileWriter writer) {
        // now copy the source strings to outText
        outText << preNetwork
        outText << network
        outText << postNetwork
        // now copy outText to the output file
        outText.each { line -> writer.write(line)
        }
        println "Transformation Completed $error"
        writer.flush()
        writer.close()
    }

    def swapChannelNames = { ChanTypeEnum expected ->
        currentInChanName = currentOutChanName
        chanNumber += 1
        currentOutChanName = "chan$chanNumber"
        expectedInChan = expected
    }

    def confirmChannel = { String pName, ChanTypeEnum actualInChanType ->
        if (expectedInChan != actualInChanType) {
            network += "Expected a process with a *$expectedInChan* type input  found $pName with type $actualInChanType \n"
            error += " with errors, see the parsed output file"
        }
    }

    def nextProcSpan = { start ->
        int beginning = start
        while (!(inText[beginning] =~ /new/)) beginning++
        int ending = beginning
        while (!inText[ending].endsWith(")")) ending++
        return [beginning, ending]
    }

    def scanChanSize = { List l ->
        int line
        line = -1  // just to make sure it has a value
        for (i in (int) l[0]..(int) l[1]) {
            if ((inText[i] =~ /workers/) || (inText[i] =~ /mappers/) || (inText[i] =~ /reducers/) || (inText[i] =~ /groups/)) {
                line = i
                break
            }
        }
        // we now know we have found the right line
        int colon = inText[line].indexOf(":") + 1
        int end = inText[line].indexOf(",")
        if (end == -1) end = inText[line].indexOf(")")
//		println "$line, ${inText[line]}, $colon, $end"
        if (end != -1) {
            chanSize = inText[line].subSequence(colon, end).trim()
            return chanSize
        } else return null
    }

    // closure to find a process def assuming start is the index of a line containing such a def
    def findProcDef = { int start ->
        int ending = start
        while (!inText[ending].endsWith(")")) ending++
        int startIndex = inText[start].indexOf("new") + 4
        int endIndex = inText[start].indexOf("(")
        if (startIndex == -1 || endIndex == -1) {
            error += "string *new* found in an unexpected place\n${inText[currentLine]}\n"
            network += error
            return null
        } else {
            String processName = inText[start].subSequence(startIndex, endIndex).trim()
            startIndex = inText[start].indexOf("def") + 4
            endIndex = inText[start].indexOf("=")
            String procName = inText[start].subSequence(startIndex, endIndex)
            return [ending, processName, procName]
        }
    }

    def findNextProc = {
        currentLine = endLine + 1
        while (!(inText[currentLine] =~ /new/)) {
            network += inText[currentLine] + "\n" // add blank and comment lines
            currentLine++
        }

    }
    def findNumberWorkers = {
        currentLine = endLine + 1
        while (!(inText[currentLine] =~ /int workers/)) {

            currentLine++
        }
        String numbWorkers = inText[currentLine][inText[currentLine].size() - 1]
        return numbWorkers
    }

    def extractProcDefParts = { int line ->
        int len = inText[line].size()
        int openParen = inText[line].indexOf("(")
        int closeParen = inText[line].indexOf(")")  // could be -1
        String initialDef = inText[line].subSequence(0, openParen + 1) // includes the (
        String remLine = null
        String firstProperty = null
        if (closeParen > 0) {
            // single line definition
            remLine = inText[line].subSequence(openParen + 1, closeParen + 1).trim()
        } else {
            //multi line definition
            if (openParen == (len - 1)) firstProperty = " " // no property specified
            else firstProperty = inText[line].subSequence(openParen + 1, len).trim()
        }
        return [initialDef, remLine, firstProperty]    // known as rvs subsequently
    }

    def copyProcProperties = { List rvs, int starting, int ending ->
        if (rvs[2] == null) network += "    ${rvs[1]}\n" else {
            if (rvs[2] != " ") network += "    ${rvs[2]}\n"
            for (i in starting + 1..ending) network += "    " + inText[i] + "\n"
        }
    }

    def checkNoProperties = { List rvs ->
        if (rvs[1] != ")") {
            error += "expecting a closing ) on same line  but not found\n"
            network += error
        }
    }

    def getLogData = { int starting, String repeatWord ->
        String repeats = null   // only null when process is not repeated
        String phaseName = null // only null if logPhaseName(s) not found
        int currentLine
        currentLine = starting
        if (repeatWord != null) {
            // looking for repeatWord
            while (!(inText[currentLine] =~ repeatWord)) {
                currentLine++
            }
            int colon = inText[currentLine].indexOf(":") + 1
            int endLine = inText[currentLine].indexOf(",")
            if (endLine == -1) endLine = inText[currentLine].indexOf(")")
            if (endLine != -1) repeats = inText[currentLine].subSequence(colon, endLine).trim()
        }
        currentLine = starting
        // look for a line containing logPhaseName(s)
        while (!(inText[currentLine] =~ /logPhaseName/)) {
            currentLine++
        }
        if (inText[currentLine] =~ /logPhaseNames/) {
            // looking for a list of names,  logPhaseNames: [ "phase1", ... , "phase-n" ]
            int startBracket = inText[currentLine].indexOf("[") + 1
            int endBracket = inText[currentLine].indexOf("]")
            phaseName = inText[currentLine].subSequence(startBracket, endBracket)  // remove [ ]
        } else {
            // looking for a single name, we have a logPhaseName: "phase"
            int colon = inText[currentLine].indexOf(":") + 1
            int endLine = inText[currentLine].indexOf(",")
            if (endLine == -1) endLine = inText[currentLine].indexOf(")")
            if (endLine != -1) {
                // will include quotes if a string constant
                phaseName = inText[currentLine].subSequence(colon, endLine).trim()
                // now remove the quote marks around word
//        phaseName = phaseName.subSequence(1, phaseName.length() - 1)
            }
        }
        if ((repeats == null)&&(phaseName == null)){
            println " GPP_Builder logging specification inconsistency; check all required elements are present"
        }
        return [repeats, phaseName]
    }

    /**
     * Define a set of closures that process common combinations
     * of input and output channels some of which are left in line as they are unique
     *  eg Connectors: AnyFanOne, AnyFanAny and OneFanAny
     *  and Groups: AnyGroupList ListGroupAny plus OnePipelineOne and OnePipelineCollect
     */

    def patternProcess = { String processName, int starting, int ending ->
//    println "$processName: $starting, $ending"
        pattern = true
        def rvs = extractProcDefParts(starting)
        network += rvs[0] + "\n"
        copyProcProperties(rvs, starting, ending)
    }

    def oneOne = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
        confirmChannel(processName, ChanTypeEnum.one)
        def rvs = extractProcDefParts(starting)
        network += rvs[0] + "\n"
        network += "    input: ${currentInChanName}.in(),\n"
        network += "    output: ${currentOutChanName}.out(),\n"
        copyProcProperties(rvs, starting, ending)
        preNetwork = preNetwork + "def $currentOutChanName = Channel.one2one()\n"
        swapChannelNames(ChanTypeEnum.one)

        //SH added JMK modified
        if (logging) {
            def returned = getLogData(starting, null)
//      println "oneOne: $returned"
            if (returned == [null, null]) {
                network += "getLogData returned null in $processName :  LogPhaseName not found"
                error += " with errors, see the parsed output file"
            } else {
                network += "\n    //gppVis command\n"
                network += "    Visualiser.hb.getChildren().add(Visualiser.p.addWorker(" + returned[1] + ")) \n"
            }
        }
    } // end of OneOne

    def oneList = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
        confirmChannel(processName, ChanTypeEnum.one)
        def rvs = extractProcDefParts(starting)
        network += rvs[0] + "\n"
        network += "    input: ${currentInChanName}.in(),\n"
        network += "    outputList: ${currentOutChanName}OutList )\n"
        checkNoProperties(rvs)
        rvs = nextProcSpan(ending + 2)
        String returnedChanSize = scanChanSize(rvs)
        if (returnedChanSize != null) chanSize = returnedChanSize
        preNetwork = preNetwork + "def $currentOutChanName = Channel.one2oneArray($chanSize)\n"
        preNetwork = preNetwork + "def ${currentOutChanName}OutList = new ChannelOutputList($currentOutChanName)\n"
        preNetwork = preNetwork + "def ${currentOutChanName}InList = new ChannelInputList($currentOutChanName)\n"
        swapChannelNames(ChanTypeEnum.list)

        //SH added
        if (logging) {
            network += "\n    //gppVis command\n"
            network += "    Visualiser.hb.getChildren().add(new Connector(Connector.TYPE.SPREADER)) \n"
        }
    } // end of oneList

    def oneListPlus = { String processName, int starting, int ending ->
        // needed because some oneList spreaders have properties
//		println "$processName: $starting, $ending"
        confirmChannel(processName, ChanTypeEnum.one)
        def rvs = extractProcDefParts(starting)
        network += rvs[0] + "\n"
        network += "    input: ${currentInChanName}.in(),\n"
        network += "    outputList: ${currentOutChanName}OutList,\n"
        copyProcProperties(rvs, starting, ending)
        rvs = nextProcSpan(ending + 2)
        String returnedChanSize = scanChanSize(rvs)
        if (returnedChanSize != null) chanSize = returnedChanSize
        preNetwork = preNetwork + "def $currentOutChanName = Channel.one2oneArray($chanSize)\n"
        preNetwork = preNetwork + "def ${currentOutChanName}OutList = new ChannelOutputList($currentOutChanName)\n"
        preNetwork = preNetwork + "def ${currentOutChanName}InList = new ChannelInputList($currentOutChanName)\n"
        swapChannelNames(ChanTypeEnum.list)
    }  // end of oneListPlus


    def listOne = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
        confirmChannel(processName, ChanTypeEnum.list)
        def rvs = extractProcDefParts(starting)
        network += rvs[0] + "\n"
        network += "    inputList: ${currentInChanName}InList,\n"
        network += "    output: ${currentOutChanName}.out(),\n"
        checkNoProperties(rvs)
        copyProcProperties(rvs, starting, ending)
        preNetwork = preNetwork + "def $currentOutChanName = Channel.one2one()\n"
        swapChannelNames(ChanTypeEnum.one)
        //SH added modified by JMK
        if (logging) {
            network += "\n    //gppVis command\n"
            network += "    Visualiser.hb.getChildren().add(new Connector(Connector.TYPE.REDUCER)) \n"
        }
    } //end of ListOne

    def noneOne = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
        def rvs = extractProcDefParts(starting)
        network += rvs[0] + "\n"
        network += "    // input channel not required\n"
        network += "    output: ${currentOutChanName}.out(),\n"
        copyProcProperties(rvs, starting, ending)
        preNetwork = preNetwork + "def $currentOutChanName = Channel.one2one()\n"
        swapChannelNames(ChanTypeEnum.one)

        //SH added JMK modified
        if (logging) {
            def returned = getLogData(starting, null)
            if (returned == [null, null]) {
                network += "getLogData returned null in $processName : LogPhaseName not found"
                error += " with errors, see the parsed output file"
            } else {
                network += "\n    //gppVis command\n"
                network += "    Visualiser.hb.getChildren().add(Visualiser.p.addWorker(" + returned[1] + ")) \n"
            }
        }
    }

    def oneNone = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
        confirmChannel(processName, ChanTypeEnum.one)
        def rvs = extractProcDefParts(starting)
        network += rvs[0] + "\n"
        network += "    input: ${currentInChanName}.in(),\n"
        if (logging) network += logChanAdd
        network += "    // no output channel required\n"
        copyProcProperties(rvs, starting, ending)

        //SH added JMK modified
        if (logging) {
            def returned = getLogData(starting, null)
            if (returned == [null, null]) {
                network += "getLogData returned null in $processName : LogPhaseName not found"
                error += " with errors, see the parsed output file"
            } else {
                network += "\n    //gppVis command\n"
                network += "    Visualiser.hb.getChildren().add(Visualiser.p.addWorker(" + returned[1] + ")) \n"
            }
        }

    }

    def listListGroup = { String processName, int starting, int ending, String type, String size ->
        // type is used only for logging to ensure correct shape is produced
//		println "$processName: $starting, $ending, $type, $size"
        confirmChannel(processName, ChanTypeEnum.list)
        def rvs = extractProcDefParts(starting)
        network += rvs[0] + "\n"
        network += "    inputList: ${currentInChanName}InList,\n"
        network += "    outputList: ${currentOutChanName}OutList,\n"
        copyProcProperties(rvs, starting, ending)
        preNetwork = preNetwork + "def $currentOutChanName = Channel.one2oneArray($chanSize)\n"
        preNetwork = preNetwork + "def ${currentOutChanName}OutList = new ChannelOutputList($currentOutChanName)\n"
        preNetwork = preNetwork + "def ${currentOutChanName}InList = new ChannelInputList($currentOutChanName)\n"
        swapChannelNames(ChanTypeEnum.list)

        //SH added JMK modified
        if (logging) {
            def returned = getLogData(starting, size)
            if (returned == [null, null]) {
                network += "getLogData returned null in $processName : groups and/or LogPhaseNames not found"
                error += " with errors, see the parsed output file"
            } else {
                network += "\n    //gppVis command\n"
                network += "    Visualiser.hb.getChildren().add(Visualiser.p.add$type(" + returned[0] + ", " + returned[1] + ")) \n"
            }
        }
    }

    def anyAnyGroup = { String processName, int starting, int ending, String type, String size ->
//		println "$processName: $starting, $ending, $type $size"
        confirmChannel(processName, ChanTypeEnum.any)
        def rvs = extractProcDefParts(starting)
        network += rvs[0] + "\n"
        network += "    inputAny: ${currentInChanName}.in(),\n"
        network += "    outputAny: ${currentOutChanName}.out(),\n"
        copyProcProperties(rvs, starting, ending)
        preNetwork = preNetwork + "def $currentOutChanName = Channel.any2any()\n"
        swapChannelNames(ChanTypeEnum.any)
//    println "network so far:\n $network"

        //SH added JMK modified
        if (logging) {
            def returned = getLogData(starting, size)
//      println "aAG: $returned"
            if (returned == [null, null]) {
                network += "getLogData returned null in $processName : groups and/or LogPhaseNames not found"
                error += " with errors, see the parsed output file"
            } else {
                network += "\n    //gppVis command\n"
                network += "    Visualiser.hb.getChildren().add(Visualiser.p.add$type(" + returned[0] + ", " + returned[1] + ")) \n"
            }
        }
    }

    def listNoneGroup = { String processName, int starting, int ending, String type, String size ->
//		println "$processName: $starting, $ending, $type, $size"
        confirmChannel(processName, ChanTypeEnum.list)
        def rvs = extractProcDefParts(starting)
        network += rvs[0] + "\n"
        network += "    inputList: ${currentInChanName}InList,\n"
        if (logging) network += logChanAdd
        network += "    // no output channel required\n"
        copyProcProperties(rvs, starting, ending)

        //SH added JMK modified
        if (logging) {
            def returned = getLogData(starting, size)
            if (returned == [null, null]) {
                network += "getLogData returned null in $processName : groups and/or LogPhaseNames not found"
                error += " with errors, see the parsed output file"
            } else {
                network += "\n    //gppVis command\n"
                network += "    Visualiser.hb.getChildren().add(Visualiser.p.add$type(" + returned[0] + ", " + returned[1] + ")) \n"
            }
        }
    }

    def anyNoneGroup = { String processName, int starting, int ending, String type , String size->
//		println "$processName: $starting, $ending, $type, $size"
        confirmChannel(processName, ChanTypeEnum.any)
        def rvs = extractProcDefParts(starting)
        network += rvs[0] + "\n"
        network += "    inputAny: ${currentInChanName}.in(),\n"
        if (logging) network += logChanAdd
        network += "    // no output channel required\n"
        copyProcProperties(rvs, starting, ending)

        //SH added JMK modified
        if (logging) {
            def returned = getLogData(starting, size)
            if (returned == [null, null]) {
                network += "getLogData returned null in $processName : groups and/or LogPhaseNames not found"
                error += " with errors, see the parsed output file"
            } else {
                network += "\n    //gppVis command\n"
                network += "    Visualiser.hb.getChildren().add(Visualiser.p.add$type(" + returned[0] + ", " + returned[1] + ")) \n"
            }
        }
    }

//
// define the closures for each process type in the library
//
// cluster connectors
    def NodeRequestingFanAny = { String processName, int starting, int ending ->
        println "$processName: $starting, $ending"
        network += inText[starting]
    }

    def NodeRequestingFanList = { String processName, int starting, int ending ->
        println "$processName: $starting, $ending"
        network += inText[starting]
    }

    def NodeRequestingParCastList = { String processName, int starting, int ending ->
        println "$processName: $starting, $ending"
        network += inText[starting]
    }

    def NodeRequestingSeqCastList = { String processName, int starting, int ending ->
        println "$processName: $starting, $ending"
        network += inText[starting]
    }

    def OneNodeRequestedList = { String processName, int starting, int ending ->
        println "$processName: $starting, $ending"
        network += inText[starting]
    }

// reducers
    def AnyFanOne = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
        confirmChannel(processName, ChanTypeEnum.any)
        def rvs = extractProcDefParts(starting)
        network += rvs[0] + "\n"
        network += "    inputAny: ${currentInChanName}.in(),\n"
        network += "    output: ${currentOutChanName}.out(),\n"
        copyProcProperties(rvs, starting, ending)
        preNetwork = preNetwork + "def $currentOutChanName = Channel.one2one()\n"
        swapChannelNames(ChanTypeEnum.one)

        //SH added modified by JMK
        if (logging) {
            network += "\n    //gppVis command\n"
            network += "    Visualiser.hb.getChildren().add(new Connector(Connector.TYPE.REDUCER)) \n"
        }
    }  // end of AnyFanOne

    def ListFanOne = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
        listOne(processName, starting, ending)
    }

    def ListMergeOne = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
        listOne(processName, starting, ending)
    }

    def ListParOne = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
        listOne(processName, starting, ending)
    }

    def ListSeqOne = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
        listOne(processName, starting, ending)
    }

    def N_WayMerge = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
        // cannot be listOne because it is exprcting properties
        confirmChannel(processName, ChanTypeEnum.list)
        def rvs = extractProcDefParts(starting)
        network += rvs[0] + "\n"
        network += "    inputList: ${currentInChanName}InList,\n"
        network += "    output: ${currentOutChanName}.out(),\n"
        copyProcProperties(rvs, starting, ending)
        preNetwork = preNetwork + "def $currentOutChanName = Channel.one2one()\n"
        swapChannelNames(ChanTypeEnum.one)
    } // end of N_WayMerge


// spreaders
    def AnyFanAny = { String processName, int starting, int ending ->
//    println "$processName: $starting, $ending"
        confirmChannel(processName, ChanTypeEnum.any)
        def rvs = extractProcDefParts(starting)
        network += rvs[0] + "\n"
        network += "    inputAny: ${currentInChanName}.in(),\n"
        network += "    outputAny: ${currentOutChanName}.out(),\n"
        copyProcProperties(rvs, starting, ending)
        preNetwork = preNetwork + "def $currentOutChanName = Channel.one2any()\n"
        swapChannelNames(ChanTypeEnum.any)

        //SH added modified by JMK
        if (logging) {
            network += "\n    //gppVis command\n"
            network += "    Visualiser.hb.getChildren().add(new Connector(Connector.TYPE.SPREADER)) \n"
        }
    } //end of AnyFanAny

    def OneFanAny = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
        confirmChannel(processName, ChanTypeEnum.one)
        def rvs = extractProcDefParts(starting)
        network += rvs[0] + "\n"
        network += "    input: ${currentInChanName}.in(),\n"
        network += "    outputAny: ${currentOutChanName}.out(),\n"
        copyProcProperties(rvs, starting, ending)
        preNetwork = preNetwork + "def $currentOutChanName = Channel.one2any()\n"
        swapChannelNames(ChanTypeEnum.any)

        //SH added modified by JMK
        if (logging) {
            network += "\n    //gppVis command\n"
            network += "    Visualiser.hb.getChildren().add(new Connector(Connector.TYPE.SPREADER)) \n"
        }
    }// end of OneFanAny

    def OneDirectedList = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
        oneListPlus(processName, starting, ending)
    } //end of OneDirectedList

    def OneFanList = { String processName, int starting, int ending ->
//			println "$processName: $starting, $ending"
        oneList(processName, starting, ending)
    } //end of OneFanList

    def OneIndexedList = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
        oneListPlus(processName, starting, ending)
    }

    def OneParCastList = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
        oneList(processName, starting, ending)
    }

    def OneSeqCastList = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
        oneList(processName, starting, ending)
    }

// patterns
//  def DataParallelCollect = { String processName, int starting, int ending ->
////			println "$processName: $starting, $ending"
//    pattern = true
//    def rvs = extractProcDefParts(starting)
//    network += rvs[0] + "\n"
//    copyProcProperties(rvs, starting, ending)
//  } // end of DataParallelCollect
//
//  def TaskParallelCollect = { String processName, int starting, int ending ->
////			println "$processName: $starting, $ending"
//    pattern = true
//    def rvs = extractProcDefParts(starting)
//    network += rvs[0] + "\n"
//    copyProcProperties(rvs, starting, ending)
//  } // end of TaskParallelCollect
//
//  def TaskParallelOfGroupCollects = { String processName, int starting, int ending ->
////			println "$processName: $starting, $ending"
//    pattern = true
//    def rvs = extractProcDefParts(starting)
//    network += rvs[0] + "\n"
//    copyProcProperties(rvs, starting, ending)
//  } // end of TaskParallelOfGroupCollects

    def TaskParallelPattern = { String processName, int starting, int ending ->
        patternProcess ( processName, starting, ending )
    }

    def DataParallelPattern = { String processName, int starting, int ending ->
        patternProcess ( processName, starting, ending )
    }

    def PipelineOfGroupsPattern = { String processName, int starting, int ending ->
        patternProcess ( processName, starting, ending )
    }

    def GroupOfPipelinesPattern = { String processName, int starting, int ending ->
        patternProcess ( processName, starting, ending )
    }

    def PipelineOfGroupCollectPattern = { String processName, int starting, int ending ->
        patternProcess ( processName, starting, ending )
    }

    def GroupOfPipelineCollectPattern = { String processName, int starting, int ending ->
        patternProcess ( processName, starting, ending )
    }

// evolutionary

    def ParallelClientServerEngine = { String processName, int starting, int ending ->
//          println "$processName: $starting, $ending"
        pattern = true
        def rvs = extractProcDefParts(starting)
        network += rvs[0] + "\n"
        copyProcProperties(rvs, starting, ending)
    } // end of ParallelClientServerEngine

// composites
    def AnyGroupOfPipelineCollects = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
        anyNoneGroup(processName, starting, ending, "GoP", "groups")
    } // end of AnyGroupOfPipelineCollects

    def AnyGroupOfPipelines = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
        anyAnyGroup(processName, starting, ending,"GoP", "groups")
    } //end of AnyGroupOfPipelines

    def AnyPipelineOfGroupCollects = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
        anyNoneGroup(processName, starting, ending, "PoG", "workers")
    } //end of AnyPipelineOfGroupCollects

    def AnyPipelineOfGroups = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
        anyAnyGroup(processName, starting, ending, "PoG", "workers")
    } // end of AnyPipelineOfGroups

    def ListGroupOfPipelineCollects = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
        listNoneGroup(processName, starting, ending, "GoP", "groups")
    } // end of ListGroupOfPipelineCollects

    def ListGroupOfPipelines = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
        listListGroup(processName, starting, ending, "GoP", "groups")
    } // end of ListGroupOfPipelines

    def ListPipelineOfGroups = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
        listListGroup(processName, starting, ending, "PoG", "workers")
    } // end of ListPipelineOfGroups

    def ListPipelineOfGroupCollects = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
        listNoneGroup(processName, starting, ending, "PoG", "workers")
    } //end of ListPipelineOfGroupCollects


// groups
    def AnyGroupAny = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
        anyAnyGroup(processName, starting, ending, "Group", "workers")
    } //end of AnyGroupAny

    def AnyGroupCollect = { String processName, int starting, int ending ->
//      println "$processName: $starting, $ending"
        anyNoneGroup(processName, starting, ending, "Group", "workers")
    } // end of AnyGroupCollect

    def AnyGroupList = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
        confirmChannel(processName, ChanTypeEnum.any)
        def rvs = extractProcDefParts(starting)
        network += rvs[0] + "\n"
        network += "    inputAny: ${currentInChanName}.in(),\n"
        network += "    outputList: ${currentOutChanName}OutList,\n"
        copyProcProperties(rvs, starting, ending)
        rvs = nextProcSpan(ending + 2)
        String returnedChanSize = scanChanSize(rvs)
        if (returnedChanSize != null) chanSize = returnedChanSize
        preNetwork = preNetwork + "def $currentOutChanName = Channel.one2oneArray($chanSize)\n"
        preNetwork = preNetwork + "def ${currentOutChanName}OutList = new ChannelOutputList($currentOutChanName)\n"
        preNetwork = preNetwork + "def ${currentOutChanName}InList = new ChannelInputList($currentOutChanName)\n"
        swapChannelNames(ChanTypeEnum.list)

        //SH added JMK modified
        if (logging) {
            def returned = getLogData(starting, "workers")
            if (returned == [null, null]) {
                network += "getLogData returned null in $processName : workers and/or LogPhaseNames not found"
                error += " with errors, see the parsed output file"
            } else {
                network += "\n    //gppVis command\n"
                network += "    Visualiser.hb.getChildren().add(Visualiser.p.addGroup(" + returned[0] + ", " + returned[1] + " )) \n"
            }
        }

    } //end of AnyGroupList

    def ListGroupAny = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
        confirmChannel(processName, ChanTypeEnum.list)
        def rvs = extractProcDefParts(starting)
        network += rvs[0] + "\n"
        network += "    inputList: ${currentInChanName}InList,\n"
        network += "    outputAny: ${currentOutChanName}.out(),\n"
        copyProcProperties(rvs, starting, ending)
        rvs = nextProcSpan(ending + 2)
        preNetwork = preNetwork + "def $currentOutChanName = Channel.any2any()\n"
        swapChannelNames(ChanTypeEnum.any)

        //SH added JMK modified
        if (logging) {
            def returned = getLogData(starting, "workers")
            if (returned == [null, null]) {
                network += "getLogData returned null in $processName : workers and/or LogPhaseNames not found"
                error += " with errors, see the parsed output file"
            } else {
                network += "\n    //gppVis command\n"
                network += "    Visualiser.hb.getChildren().add(Visualiser.p.addGroup(" + returned[0] + ", " + returned[1] + " )) \n"
            }
        }

    } // end of ListGroupAny

    def ListGroupCollect = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
        listNoneGroup(processName, starting, ending, "Group", "workers")
    } // end of ListGroupCollect

    def ListGroupList = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
        listListGroup(processName, starting, ending, "Group","workers")
    }  //end of ListGroupList


    def ListThreePhaseWorkerList = { String processName, int starting, int ending ->
//			println "$processName: $starting, $ending"
        listListGroup(processName, starting, ending, "Group", "workers")
    } //end ListThreePhaseWorkerList

//matrix
    def MultiCoreEngine = { String processName, int starting, int ending ->
//      println "$processName: $starting, $ending"
        confirmChannel(processName, ChanTypeEnum.one)
        def rvs = extractProcDefParts(starting)
        network += rvs[0] + "\n"
        network += "    input: ${currentInChanName}.in(),\n"
        network += "    output: ${currentOutChanName}.out(),\n"
        copyProcProperties(rvs, starting, ending)
        preNetwork = preNetwork + "def $currentOutChanName = Channel.one2one()\n"
        swapChannelNames(ChanTypeEnum.one)

        if (logging) {
            def returned = getLogData(starting, "nodes")
            if (returned == [null, null]) {
                network += "getLogData returned null in $processName : stages and/or LogPhaseNames not found"
                error += " with errors, see the parsed output file"
            } else {
                network += "\n    //gppVis command\n"
                network += "    Visualiser.hb.getChildren().add(Visualiser.p.addMCEngine (" + returned[0] + " )) \n"
            }
        }  }  // end of MultiCoreEngine

    def StencilEngine = { String processName, int starting, int ending ->
        MultiCoreEngine(processName, starting, ending)
    }  // end of StencilEngine

// pipelines
    def OnePipelineCollect = { String processName, int starting, int ending ->
        //println "$processName: $starting, $ending"
        confirmChannel(processName, ChanTypeEnum.one)
        def rvs = extractProcDefParts(starting)
        network += rvs[0] + "\n"
        network += "    input: ${currentInChanName}.in(),\n"
        if (logging) network += logChanAdd
        network += "    // no output channel required\n"
        copyProcProperties(rvs, starting, ending)

        //SH added JMK modified
        if (logging) {
            def returned = getLogData(starting, "stages")
            if (returned == [null, null]) {
                network += "getLogData returned null in $processName : stages and/or LogPhaseNames not found"
                error += " with errors, see the parsed output file"
            } else {
                network += "\n    //gppVis command\n"
                network += "    Visualiser.hb.getChildren().add(Visualiser.p.addPipe (" + returned[0] + ", " + returned[1] + " )) \n"
            }
        }
    } // end of OnePipelineCollect

    def OnePipelineOne = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
        confirmChannel(processName, ChanTypeEnum.one)
        def rvs = extractProcDefParts(starting)
        network += rvs[0] + "\n"
        network += "    input: ${currentInChanName}.in(),\n"
        network += "    output: ${currentOutChanName}.out(),\n"
        copyProcProperties(rvs, starting, ending)
        preNetwork = preNetwork + "def $currentOutChanName = Channel.one2one()\n"
        swapChannelNames(ChanTypeEnum.one)

        //SH added JMK modified
        if (logging) {
            def returned = getLogData(starting, "stages")
            if (returned == [null, null]) {
                network += "getLogData returned null in $processName : stages and/or LogPhaseNames not found"
                error += " with errors, see the parsed output file"
            } else {
                network += "\n    //gppVis command\n"
                network += "    Visualiser.hb.getChildren().add(Visualiser.p.addPipe (" + returned[0] + ", " + returned[1] + " )) \n"
            }
        }
    } // end of OnePipelineOne

// terminals
    def Collect = { String processName, int starting, int ending ->
//			println "$processName: $starting, $ending"
        oneNone (processName, starting, ending)
    }

    def CollectUI = { String processName, int starting, int ending ->
//			println "$processName: $starting, $ending"
        oneNone (processName, starting, ending)
    }

    def Emit = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
        noneOne(processName, starting, ending)
    }

    def EmitFromInput = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
        oneOne(processName, starting, ending)
    }


    def EmitWithLocal = { String processName, int starting, int ending ->
//			println "$processName: $starting, $ending"
        noneOne(processName, starting, ending)
    } //end of Emit with Local

    def TestPoint = { String processName, int starting, int ending ->
//    println "$processName: $starting, $ending"
        oneNone(processName, starting, ending)
    }

// transformers
    def CombineNto1 = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
        oneOne(processName, starting, ending)
    }  //end of CombineNto1

// workers
    def ThreePhaseWorker = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
        oneOne(processName, starting, ending)
    }  //end of ThreePhaseWorker

    def Worker = { String processName, int starting, int ending ->
//		println "$processName: $starting, $ending"
        oneOne(processName, starting, ending)
    } //end of worker

// closures used in file processing
    def processImports = {
        while (inText[currentLine] == " ") {
            outText << inText[currentLine] + "\n"
            currentLine++
        }
        while (inText[currentLine].startsWith("import") || inText[currentLine] == " ") {
            outText << inText[currentLine] + "\n"
            currentLine++
        }
    } //end of processImports

    def processLogDetails = {
        List logTokens = inText[currentLine].tokenize()
        String collectors = logTokens[1]
        logFileName = logTokens[2]
        logging = true
        preNetwork += "\nimport GPP_Library.Logger\n"
        preNetwork += "import GPP_Library.LoggingVisualiser\n"
        preNetwork += "import GPP_Library.gppVis.Visualiser\n"
        preNetwork += "import GPP_Library.gppVis.Connector\n"
        preNetwork += "import javafx.application.Platform\n\n"

        preNetwork += "def logChan = Channel.any2one()\n"
        preNetwork += "Logger.initLogChannel(logChan.out())\n"
        preNetwork += "def logVis = new LoggingVisualiser ( logInput: logChan.in(), \n"
        preNetwork += "                     collectors: $collectors,\n"
        preNetwork += "                     logFileName: $logFileName )\n\n"
        processNames << "logVis"

        //SH added
        preNetwork += "//gppVis command\n"
        preNetwork += "new Thread() {\n"
        preNetwork += "	@Override\n"
        preNetwork += "	public void run() {\n"
        preNetwork += "		Visualiser.main() \n"
        preNetwork += "	}\n"
        preNetwork += "}.start() \n"
    }// end of processLogDetails

    def processPreNetwork = {
        boolean startProcess = ((inText[currentLine] =~ /Emit/) || (inText[currentLine] =~ /Pattern/))
        while (!startProcess) {
            preNetwork += inText[currentLine] + "\n"
            // added to deal with logging
            if (inText[currentLine].startsWith("//@log")) processLogDetails()
            currentLine++
            startProcess = ((inText[currentLine] =~ /Emit/) || (inText[currentLine] =~ /Pattern/))
        }
        preNetwork = preNetwork + "\n//NETWORK\n\n"
    } // end of processPreNetwork

    def processPostNetwork = {

        //SH added
        if (logging) {
            postNetwork += "//gppVis command\n"
            postNetwork += "//short delay to give JavaFx time to start up.\n"
            postNetwork += "sleep(2000)\n"
            postNetwork += "Platform.runLater(new Runnable() {\n"
            postNetwork += "	@Override\n"
            postNetwork += "	void run() {\n"
            postNetwork += "		Visualiser.networkScene($logFileName)\n"
            postNetwork += "	}\n"
            postNetwork += "}) \n"

            postNetwork += "\n//short delay to give JavaFx time to display.\n"
            postNetwork += "sleep(3000) \n\n"
        }

        if (!pattern) postNetwork += "PAR network = new PAR()\n network = new PAR($processNames)\n network.run()\n network.removeAllProcesses()" else postNetwork += "${processNames[0]}.run()\n"
        postNetwork += "\n//END\n\n"

        //SH added
        if (logging) {
            postNetwork += "//gppVis command\n"
            postNetwork += "//Now that the network has completed, tell the vis where the log file is so it\n"
            postNetwork += "//can access the data so it can replay it.\n"
            postNetwork += "Platform.runLater(new Runnable() {\n"
            postNetwork += "	@Override\n"
            postNetwork += "	void run() {\n"
            postNetwork += "		Visualiser.readLog(\"" + logFileName.replace("\"", "") + "log.csv\")\n"
            postNetwork += "	}\n"
            postNetwork += "}) \n"
        }


        while (currentLine < inText.size()) {
            postNetwork += inText[currentLine] + "\n"
            currentLine++
        }
    } // end of processPostNetwork

}

