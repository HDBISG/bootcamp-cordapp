package bootcamp;

import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.ImmutableList;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.ContractState;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static net.corda.core.contracts.ContractsDSL.requireThat;

public class EcoCancelFlow {

    @InitiatingFlow
    @StartableByRPC
    public static class EcoCancelFlowInitiator extends FlowLogic<SignedTransaction> {

        private final UniqueIdentifier stateLinearId;

        private  final Logger logger = LoggerFactory.getLogger(EcoCancelFlowInitiator.class);

        public EcoCancelFlowInitiator( UniqueIdentifier stateLinearId) {
            this.stateLinearId = stateLinearId;
        }

        private final ProgressTracker progressTracker = new ProgressTracker();

        @Override
        public ProgressTracker getProgressTracker() {
            return progressTracker;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {

            // 1. Retrieve the IOU State from the vault using LinearStateQueryCriteria
            // List<UUID> listOfLinearIds = Arrays.asList(stateLinearId.getId());
            QueryCriteria queryCriteria = new QueryCriteria.LinearStateQueryCriteria(
                    null, null,
                    ImmutableList.of(stateLinearId.getExternalId()), Vault.StateStatus.UNCONSUMED,null);

            Vault.Page results = getServiceHub().getVaultService().queryBy(EcoState.class, queryCriteria);

            // 2. Get a reference to the inputState data that we are going to settle.
            StateAndRef inputStateAndRefToSettle = (StateAndRef) results.getStates().get(0);
            EcoState ecoState = (EcoState) ((StateAndRef) results.getStates().get(0)).getState().getData();

            // We choose our transaction's notary (the notary prevents double-spends).
            Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);
            // We get a reference to our own identity.
            Party fti = getOurIdentity();
            /////////////////////////////////////////////////


            /* ============================================================================
             *         TODO 1 - Create our EcoState to represent on-ledger tokens!
             * ===========================================================================*/
            // We create our new TokenState.
            final Command<EcoIssueContract.Commands.Issue> txCommand = new Command<>(
                    new EcoIssueContract.Commands.Issue(),
                    ecoState.getParticipants()
                            .stream().map(AbstractParty::getOwningKey)
                            .collect(Collectors.toList()) );
            /* ============================================================================
             *      TODO 3 - Build our Eco issuance transaction to update the ledger!
             * ===========================================================================*/
            // We build our transaction.
            TransactionBuilder transactionBuilder = new TransactionBuilder( notary );
            transactionBuilder.addCommand( txCommand );
            transactionBuilder.addInputState(inputStateAndRefToSettle);



            /* ============================================================================
             *          TODO 2 - Write our EcoContract to control token issuance!
             * ===========================================================================*/
            // We check our transaction is valid based on its contracts.
            transactionBuilder.verify(getServiceHub());

            logger.info("cancel Before initiateFlow()");
            FlowSession session = initiateFlow( ecoState.getVcc() );

            // We sign the transaction with our private key, making it immutable.
            logger.info("Before signInitialTransaction()");
            SignedTransaction signedTransaction = getServiceHub().signInitialTransaction(transactionBuilder);

            // The counterparty signs the transaction
            logger.info("Before CollectSignaturesFlow()");
            SignedTransaction fullySignedTransaction = subFlow(new CollectSignaturesFlow(signedTransaction, singletonList(session)));

            // We get the transaction notarised and recorded automatically by the platform.
            logger.info("Before FinalityFlow()");
            return subFlow(new FinalityFlow(fullySignedTransaction, singletonList(session)));
        }
    }


    @InitiatedBy(EcoCancelFlowInitiator.class)
    public static class EcoCancelFlowResponder extends FlowLogic<Void> {

        private final FlowSession otherSide;

        private final Logger logger = LoggerFactory.getLogger(EcoCancelFlowResponder.class);

        public EcoCancelFlowResponder(FlowSession otherSide) {
            this.otherSide = otherSide;
        }

        @Override
        @Suspendable
        public Void call() throws FlowException {
            SignedTransaction signedTransaction = subFlow(new SignTransactionFlow(otherSide) {
                @Suspendable
                @Override
                protected void checkTransaction(SignedTransaction stx) throws FlowException {
                    // Implement responder flow transaction checks here

                }
            });
            logger.info("before ReceiveFinalityFlow()"  );
            subFlow(new ReceiveFinalityFlow(otherSide, signedTransaction.getId()));
            logger.info("end ReceiveFinalityFlow()"  );

            return null;
        }
    }
}
