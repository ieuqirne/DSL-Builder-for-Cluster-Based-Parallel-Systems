package cluster.builtNetScripts

import GPP_Library.DataDetails
import GPP_Library.ResultDetails
import GPP_Library.cluster.connectors.OneNodeRequestedList
import GPP_Library.connectors.reducers.AnyFanOne
import GPP_Library.terminals.Collect
import GPP_Library.terminals.Emit
import cluster.data.MCpiData
import cluster.data.MCpiResultsSerialised
import groovyJCSP.ChannelInputList
import groovyJCSP.ChannelOutputList
import groovyJCSP.PAR
import jcsp.lang.Channel
import jcsp.net2.NetChannel
import jcsp.net2.NetChannelInput
import jcsp.net2.NetChannelOutput
import jcsp.net2.Node
import jcsp.net2.tcpip.TCPIPNodeAddress


/**
 * The script used to create a host process network containing an emit and collector processes.
 * Thus BasicHost has to be modified for each solution.
 */

int nodes = 2   // this value should be modified using clusterScript.gpp by the Builder from //@ Cluster integer

// this part works on a loop-back network and
// should be commented out when running on a real network
//String hostIP127 = "127.0.0.1"
//def hostAddress = new TCPIPNodeAddress(hostIP127, 1000)

// this part works on a real network
def hostAddress = new TCPIPNodeAddress(1000)

// the rest of the script is common to all network environments
Node.getInstance().init(hostAddress)
String hostIP = hostAddress.getIpAddress()
println "Host running on $hostIP for $nodes worker nodes"

// create request channel
def hostRequest = NetChannel.numberedNet2One(1)
// read IP addresses  from nodes and create response channels
def responseChannels = new ChannelOutputList()
def nodeIPs = []
println "Host waiting for node IPs"
for ( n in 0 ..< nodes ) {
  String nodeIP = hostRequest.read()
  println "Node  $n = $nodeIP"
  nodeIPs << nodeIP
  def nodeAddress = new TCPIPNodeAddress(nodeIP, 1000)
  responseChannels.append( NetChannel.one2net(nodeAddress, 1) )
}

// respond to each of the nodes so they know their IP address has been received
for ( n in 0 ..< nodes ){
  responseChannels[n].write(hostIP) // use hostIP as signal for ease of checking
}

// now create all the net input channels required by Emit and Collect processes
// this bit is filled in by the Builder for the Emit and Collect processes
NetChannelInput inChan1 = NetChannel.numberedNet2One(100)
NetChannelInput inChan2 = NetChannel.numberedNet2One(101)
NetChannelInput inChan3 = NetChannel.numberedNet2One(102)


// wait for nodes to have created their net input channels
for ( n in 0 ..< nodes){
  String message = hostRequest.read()  //message will be one of the nodeIP value
  assert (nodeIPs.contains(message)): "RunHost: net channel input creation phase, Unexpected node IP : $message"
}
// respond to each of the nodes so they can create their output channels
for ( n in 0 ..< nodes ){
  responseChannels[n].write(hostIP) // use hostIP as signal for ease of checking
}

// now create all the net output channels required by the Emit and Collect processes
// this bit is filled in by the Builder
def otherNode1Address = new TCPIPNodeAddress(nodeIPs[0], 1000)
NetChannelOutput outChan1 = NetChannel.one2net(otherNode1Address, 100)
def otherNode2Address = new TCPIPNodeAddress(nodeIPs[1], 1000)
NetChannelOutput outChan2 = NetChannel.one2net(otherNode2Address, 100)

// wait for nodes to have created their net output channels
for ( n in 0 ..< nodes){
  String message = hostRequest.read()  //message will be one of the nodeIP value
  assert (nodeIPs.contains(message)): "RunHost: net channel input creation phase, Unexpected node IP : $message"
}
// respond to each of the nodes so they can define their processes
for ( n in 0 ..< nodes ){
  responseChannels[n].write(hostIP) // use hostIP as signal for ease of checking
}

// now define the processes required at host node Emit, Collect plus typically ONRL and AFO
// this bit filled in by Builder

def chan1 = Channel.one2one()
def chan2 = Channel.one2one()
def requestListONRL = new ChannelInputList()
requestListONRL.append(inChan1)
requestListONRL.append(inChan2)
def responseListONRL = new ChannelOutputList()
responseListONRL.append(outChan1)
responseListONRL.append(outChan2)

// now create the node's processes - derived from cluster definition script
// uses some previously created net channels
def emitDetails = new DataDetails(dName: MCpiData.getName(),
    dInitMethod: MCpiData.init,
    dInitData: [2048],
    dCreateMethod: MCpiData.create,
    dCreateData: [1000000]
)

def resultDetails = new ResultDetails(rName: MCpiResultsSerialised.getName(),
    rInitMethod: MCpiResultsSerialised.init,
    rCollectMethod: MCpiResultsSerialised.collector,
    rFinaliseMethod: MCpiResultsSerialised.finalise
)

def emit = new Emit (
    eDetails: emitDetails,
    output: chan1.out()
)
def onrl = new OneNodeRequestedList(
    request: requestListONRL,
    response: responseListONRL,
    input: chan1.in()
)
def afo = new AnyFanOne(
    inputAny: inChan3,
    output: chan2.out(),
    sources: nodes
)
def collector = new Collect(
    input: chan2.in(),
    rDetails: resultDetails
)



// wait for nodes to have defined their processes
for ( n in 0 ..< nodes){
  String message = hostRequest.read()  //message will be one of the nodeIP value
  assert (nodeIPs.contains(message)): "RunHost: net channel input creation phase, Unexpected node IP : $message"
}
// respond to each of the nodes so they can run their processes
for ( n in 0 ..< nodes ){
  responseChannels[n].write(hostIP) // use hostIP as signal for ease of checking
}

// now invoke a Process Manager to run the processes
// this bit filled in by Builder

println "Host starting"

new PAR([emit, onrl, afo, collector]).run()

println "RunHost has terminated"

