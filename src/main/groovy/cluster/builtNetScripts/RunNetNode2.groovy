package cluster.builtNetScripts

import GPP_Library.cluster.connectors.NodeRequestingFanAny
import GPP_Library.connectors.reducers.AnyFanOne
import GPP_Library.functionals.groups.AnyGroupAny
import cluster.data.SerializedMCpiData
import groovyJCSP.PAR
import jcsp.lang.Channel
import jcsp.net2.NetChannel
import jcsp.net2.NetChannelInput
import jcsp.net2.NetChannelOutput
import jcsp.net2.Node
import jcsp.net2.tcpip.TCPIPNodeAddress
import jcsp.userIO.Ask

/**
 * The script used to create a node process network containing an emit and collector processes.
 * Thus BasicNode has to be modified for each solution.
 * There will be a version of this script for each node
 * There will be no code loading over network in this solution
 * due to problems with Java Groovy and class loaders resulting from
 * changes in both Java and groovy as we move from Java 8 and Groovy 2 to 3
 */

// the first part assumes the Nodes are running on a loop-back 127.0.0.? network
// it assumes the host is running on 127.0.0.1
//String nodeAddress4 = Ask.Int( "what is the fourth part of the node's IP-address?  ", 2, 254)
//String nodeIP = "127.0.0." + nodeAddress4
//def nodeAddress = new TCPIPNodeAddress(nodeIP, 1000)
//String hostIP = "127.0.0.1"
fa//Node.getInstance().init(nodeAddress)

// this section assumes the system is running on a real network
// there are two ways of getting hostIP either by interaction of by argument passing
// choose one only!
//String hostIP = args[0]
String hostIP = Ask.string("Host IP address? ")
def nodeAddress = new TCPIPNodeAddress(1000)  // create nodeAddress for most global IP address
Node.getInstance().init(nodeAddress)
String nodeIP = nodeAddress.getIpAddress()

// the rest of the script is common

println "Run Node is located at $nodeIP and host is $hostIP"
NetChannelInput hostResponse = NetChannel.numberedNet2One(1, )
def hostAddress = new TCPIPNodeAddress(hostIP, 1000)
// create host request channel
def hostRequest = NetChannel.any2net(hostAddress, 1)
// send host the IP of this Node
hostRequest.write(nodeIP)
println "Run Node written $nodeIP to host"
String message = hostResponse.read()
assert (message == hostIP): "Run Node - $nodeIP: expected $hostIP received $message : initial interaction"

// now create all the net input channels
// this bit filled in by Builder
NetChannelInput inChan1 = NetChannel.numberedNet2One(100)

// inform host that input channels have been created
hostRequest.write(nodeIP)
message = hostResponse.read()
assert (message == hostIP): "Run Node - $nodeIP: expected $hostIP received $message : input channel creation"

// now create all the net output channels
// this bit filled in by Builder
def otherNode1Address = new TCPIPNodeAddress(hostIP, 1000)
NetChannelOutput outChan1 = NetChannel.one2net(otherNode1Address, 101)
def otherNode2Address = new TCPIPNodeAddress(hostIP, 1000)
NetChannelOutput outChan2 = NetChannel.one2net(otherNode2Address, 102)


// inform host that output channels have been created
hostRequest.write(nodeIP)
message = hostResponse.read()
assert (message == hostIP): "Run Node - $nodeIP: expected $hostIP received $message : output channel creation"

println "Run Node - $nodeIP: defining network"
// now define the processes for the node including the additional ones required
int workers = 2   //copied from cluster definition file

def chan1 = Channel.one2any()
def chan2 = Channel.any2one()
def nrfa = new NodeRequestingFanAny(
        request: outChan1,
        response: inChan1,
        outputAny: chan1.out(),
        destinations: workers
)
def group = new AnyGroupAny(
        inputAny: chan1.in(),
        outputAny: chan2.out(),
        workers: workers,
        function: SerializedMCpiData.withinOp
)
def afo = new AnyFanOne(
        inputAny: chan2.in(),
        output: outChan2,
        sources: workers
)
println "Run Node - $nodeIP: defined network"


// inform host that process network has been defined
hostRequest.write(nodeIP)
message = hostResponse.read()
assert (message == hostIP): "Run Node - $nodeIP: expected $hostIP received $message: process definition"

// now invoke Process Manager to run process network
// this bit filled in by Builder

println "Node $nodeIP starting"
new PAR([nrfa, group, afo]).run()

println "Run Node - $nodeIP: has terminated"