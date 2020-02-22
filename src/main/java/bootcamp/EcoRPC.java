package bootcamp;

import net.corda.client.rpc.CordaRPCClient;
import net.corda.client.rpc.CordaRPCClientConfiguration;
import net.corda.client.rpc.CordaRPCConnection;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.messaging.FlowHandle;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.utilities.NetworkHostAndPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EcoRPC {
    private static final Logger logger = LoggerFactory.getLogger(EcoRPC.class);

    public void issueEco( String docNo, String ecoXML ) {

        logger.info("begin");
        final NetworkHostAndPort nodeAddress = NetworkHostAndPort.parse( "localhost:10004" );
        String username = "user1";
        String password = "test";

        final CordaRPCClient client = new CordaRPCClient(nodeAddress, CordaRPCClientConfiguration.DEFAULT);

        final CordaRPCConnection connection = client.start(username, password);

        final CordaRPCOps proxy = connection.getProxy();

        //cordaRPCOperations.startFlowDynamic( )
        CordaX500Name partyBName = new CordaX500Name("PartyA", "London", "GB");
        Party partyA = proxy.wellKnownPartyFromX500Name(partyBName);

        partyBName = new CordaX500Name("PartyB", "New York", "US");
        Party partyB = proxy.wellKnownPartyFromX500Name(partyBName);

        EcoState ecoState = new EcoState( partyA, partyB, new UniqueIdentifier(docNo), ecoXML );

        System.out.println("ecoState=" + ecoState );

        try {
            FlowHandle<SignedTransaction> flowHandle1 = proxy.startFlowDynamic(EcoIssueFlowInitiator.class, ecoState);
        }catch (Exception e) {
            e.printStackTrace();
        }
        logger.info(proxy.currentNodeTime().toString());

        connection.notifyServerAndClose();

        System.out.println("end 3");
    }
}
