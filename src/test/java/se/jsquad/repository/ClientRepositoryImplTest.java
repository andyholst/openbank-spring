/*
 * Copyright 2019 JSquad AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.jsquad.repository;

import org.apache.activemq.broker.BrokerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import se.jsquad.component.database.FlywayDatabaseMigration;
import se.jsquad.entity.Account;
import se.jsquad.entity.AccountTransaction;
import se.jsquad.entity.Client;
import se.jsquad.entity.RegularClient;
import se.jsquad.entity.TransactionType;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@TestPropertySource(locations = {"classpath:test/application.properties",
        "classpath:activemq.properties",
        "classpath:test/configuration/configuration_test.properties",
        "classpath:test/configuration/openbank_jpa.properties",
        "classpath:test/configuration/security_jpa.properties"},
        properties = {"jasypt.encryptor.password = testencryption"})
@Transactional(transactionManager = "transactionManagerOpenBank", propagation = Propagation.REQUIRED)
public class ClientRepositoryImplTest {
    @MockBean
    private BrokerService brokerService;

    @MockBean
    private FlywayDatabaseMigration flywayDatabaseMigration;

    @Autowired
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
