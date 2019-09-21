package se.jsquad.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import se.jsquad.entity.Account;
import se.jsquad.entity.AccountTransaction;
import se.jsquad.entity.Client;
import se.jsquad.entity.RegularClient;
import se.jsquad.entity.TransactionType;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@TestPropertySource(locations = {"classpath:application.properties", "classpath:activemq.properties",
        "classpath:database.properties"})
@Transactional(propagation = Propagation.REQUIRED)
public class ClientRepositoryImplTest {
    @Autowired
    @Qualifier("clientRepository")
    private ClientRepository clientRepository;

    @Test
    void testGetClientInformation() {
        // Given
        String personIdentification = "191212121212";

        // When
        Client client = clientRepository.getClientByPersonIdentification(personIdentification);

        // Then
        assertEquals(personIdentification, client.getPerson().getPersonIdentification());

        assertEquals("John", client.getPerson().getFirstName());
        assertEquals("Doe", client.getPerson().getLastName());
        assertEquals("john.doe@test.se", client.getPerson().getMail());
        assertEquals(client, client.getPerson().getClient());

        assertEquals(500, ((RegularClient) client.getClientType()).getRating().longValue());
        assertEquals(client, client.getClientType().getClient());

        Account account = client.getAccountSet().iterator().next();

        assertEquals("1000", account.getAccountNumber());
        assertEquals(500, account.getBalance().longValue());
        assertEquals(client, account.getClient());

        AccountTransaction accountTransaction = account.getAccountTransactionSet().iterator().next();

        assertEquals(account, accountTransaction.getAccount());
        assertEquals("500$ in deposit", accountTransaction.getMessage());
        assertEquals(TransactionType.DEPOSIT, accountTransaction.getTransactionType());
    }
}
