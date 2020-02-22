package bootcamp;

import net.corda.core.contracts.ContractState;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.testing.core.TestIdentity;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class StateTests {
/*    private final Party fti = new TestIdentity(new CordaX500Name("FTI", "", "TH")).getParty();
    private final Party vcc = new TestIdentity(new CordaX500Name("VCC", "", "SG")).getParty();

    @Test
    public void eCOStateHasIssuerOwnerAndAmountParamsOfCorrectTypeInConstructor() {
        new EcoState(fti, vcc, "ecoXmlUBL");
    }

    @Test
    public void eCOStateHasGettersForIssuerOwnerAndAmount() {
        EcoState eCOState = new EcoState(fti, vcc, "ecoXmlUBL");
        assertEquals(fti, eCOState.getFti());
        assertEquals(vcc, eCOState.getVcc());
        assertEquals("ecoXmlUBL", eCOState.getEcoContent());
    }

    @Test
    public void eCOStateImplementsContractState() {
        assertTrue(new EcoState(fti, vcc, "ecoXmlUBL") instanceof ContractState);
    }

    @Test
    public void eCOStateHasTwoParticipantsTheIssuerAndTheOwner() {
        EcoState eCOState = new EcoState(fti, vcc, "ecoXmlUBL");
        assertEquals(2, eCOState.getParticipants().size());
        assertTrue(eCOState.getParticipants().contains(fti));
        assertTrue(eCOState.getParticipants().contains(vcc));
    }*/
}