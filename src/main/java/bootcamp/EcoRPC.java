package bootcamp;

import net.corda.client.rpc.CordaRPCClient;
import net.corda.client.rpc.CordaRPCClientConfiguration;
import net.corda.client.rpc.CordaRPCConnection;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.messaging.FlowHandle;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.utilities.NetworkHostAndPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class EcoRPC {
    private static final Logger logger = LoggerFactory.getLogger(EcoRPC.class);

    private CordaRPCConnection getConnection() {
        final NetworkHostAndPort nodeAddress = NetworkHostAndPort.parse( "localhost:10004" );
        String username = "user1";
        String password = "test";

        final CordaRPCClient client = new CordaRPCClient(nodeAddress, CordaRPCClientConfiguration.DEFAULT);

        final CordaRPCConnection connection = client.start(username, password);

        return connection;
    }

    public void issueEco( String docNo, String ecoXML ) {

        logger.info("begin");

        final CordaRPCConnection connection = getConnection();

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

        System.out.println("end issue");
    }

    public void cancelEco( String docNo  ) {

        logger.info("begin");

        final CordaRPCConnection connection = getConnection();

        final CordaRPCOps proxy = connection.getProxy();

        System.out.println("docNo=" + docNo );

        try {
            FlowHandle<SignedTransaction> flowHandle1 = proxy.startFlowDynamic( EcoCancelFlow.EcoCancelFlowInitiator.class, new UniqueIdentifier(docNo) );
        }catch (Exception e) {
            e.printStackTrace();
        }
        logger.info(proxy.currentNodeTime().toString());

        connection.notifyServerAndClose();

        System.out.println("end cancel");
    }
    public void queryEco( String docNo  ) {

        logger.info("begin");

        final CordaRPCConnection connection = getConnection();

        final CordaRPCOps proxy = connection.getProxy();

        System.out.println("docNo=" + docNo );

        try {
            List<StateAndRef<EcoState>> stateAndRefs = proxy.vaultQuery( EcoState.class ).getStates();
            System.out.println("stateAndRefs= " + stateAndRefs.size() );
            System.out.println("stateAndRefs= " + stateAndRefs);

        }catch (Exception e) {
            e.printStackTrace();
        }
        logger.info(proxy.currentNodeTime().toString());

        connection.notifyServerAndClose();

        System.out.println("end cancel");
    }
}
