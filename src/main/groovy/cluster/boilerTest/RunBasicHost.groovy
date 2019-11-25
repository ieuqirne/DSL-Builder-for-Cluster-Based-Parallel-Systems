package cluster.boilerTest

import groovyJCSP.ChannelOutputList
import jcsp.net2.NetChannel
import jcsp.net2.Node
import jcsp.net2.mobile.CodeLoadingChannelFilter
import jcsp.net2.tcpip.TCPIPNodeAddress


/**
 * The script used to create a host process network containing an emit and collector processes.
 * Thus RunBasicHost has to be modified for each solution.
 */

int nodes = 2   // this value should be modified using clusterScript.gpp by the Builder

// this part works on a loop-back network and
// should be commented out when running on a real network
String hostIP = "127.0.0.1"
def hostAddress = new TCPIPNodeAddress(hostIP, 1000)

// this part works on a real network
//def hostAddress = new TCPIPNodeAddress(1000)  // creates most global IP address
//String hostIP = hostAddress.getIpAddress()

// the rest of the script is common to all network environments
Node.getInstance().init(hostAddress)
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
println " RunHost create input net channels"

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
println " RunHost create output net channels"

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
println " RunHost create process definitions"


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
println " RunHost run process network"

println "RunHost has terminated"
