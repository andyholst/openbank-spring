/*
 * Copyright 2021 JSquad AB
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

package se.jsquad.business;

import org.apache.activemq.broker.BrokerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import se.jsquad.AbstractSpringBootConfiguration;
import se.jsquad.api.client.AccountApi;
import se.jsquad.api.client.AccountTransactionApi;
import se.jsquad.api.client.ClientApi;
import se.jsquad.api.client.TransactionTypeApi;
import se.jsquad.component.database.FlywayDatabaseMigration;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OpenBankServiceTest extends AbstractSpringBootConfiguration {
    @MockBean
    private BrokerService brokerService;

    @MockBean
    private FlywayDatabaseMigration flywayDatabaseMigration;

    @Autowired
    private OpenBankService openBankService;

    @Test
    void testGetClientInformation() {
        // Given
        String personIdentification = "191212121212";

        // When
        ClientApi clientApi = openBankService.getClientInformationByPersonIdentification(personIdentification);

        // Then
        assertEquals(personIdentification, clientApi.getPerson().getPersonIdentification());

        assertEquals("John", clientApi.getPerson().getFirstName());
        assertEquals("Doe", clientApi.getPerson().getLastName());
        assertEquals("john.doe@test.se", clientApi.getPerson().getMail());

        assertEquals(500, clientApi.getClientType().getRating());

        AccountApi accountApi = clientApi.getAccountList().get(0);

        assertEquals(500, accountApi.getBalance());

        AccountTransactionApi accountTransactionApi = accountApi.getAccountTransactionList().get(0);

        assertEquals("500$ in deposit", accountTransactionApi.getMessage());
        assertEquals(TransactionTypeApi.DEPOSIT, accountTransactionApi.getTransactionType());
    }
}
