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

package se.jsquad.soap;

import org.apache.activemq.broker.BrokerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import se.jsquad.AbstractSpringBootConfiguration;
import se.jsquad.component.database.FlywayDatabaseMigration;
import se.jsquad.getclientservice.GetClientRequest;
import se.jsquad.getclientservice.GetClientResponse;
import se.jsquad.getclientservice.StatusType;
import se.jsquad.getclientservice.TransactionType;
import se.jsquad.getclientservice.Type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GetClientInformationSoapControllerTest extends AbstractSpringBootConfiguration {
    @MockBean
    private BrokerService brokerService;

    @MockBean
    private FlywayDatabaseMigration flywayDatabaseMigration;

    @Autowired
    private GetClientInformationSoapController getClientInformationSoapController;

    @Test
    public void testGetClientInformationWs() {
        // Given
        String personIdentification = "191212121213";

        GetClientRequest getClientRequest = new GetClientRequest();
        getClientRequest.setPersonIdentification(personIdentification);

        // When
        GetClientResponse getClientResponse = getClientInformationSoapController.getClientResponse(getClientRequest);

        // Then
        assertNotNull(getClientResponse);
        assertEquals(StatusType.OK, getClientResponse.getStatus());
        assertEquals("Client found.", getClientResponse.getMessage());

        assertEquals(personIdentification, getClientResponse.getClient().getPerson().getPersonIdentification());
        assertEquals("Alice", getClientResponse.getClient().getPerson().getFirstName());
        assertEquals("Doe", getClientResponse.getClient().getPerson().getLastName());
        assertEquals("alice.doe@test.se", getClientResponse.getClient().getPerson().getMail());

        assertEquals(1, getClientResponse.getClient().getAccountList().size());
        assertEquals(1, getClientResponse.getClient().getAccountList().get(0).getAccountTransactionList().size());

        assertEquals(1000, getClientResponse.getClient().getAccountList().get(0).getBalance());
        assertEquals(TransactionType.WITHDRAWAL,
                getClientResponse.getClient().getAccountList().get(0).getAccountTransactionList().get(0)
                        .getTransactionType());
        assertEquals("500$ in withdrawal",
                getClientResponse.getClient().getAccountList().get(0).getAccountTransactionList().get(0).getMessage());

        assertEquals(Type.PREMIUM, getClientResponse.getClient().getClientType().getType());
        assertEquals(1000, getClientResponse.getClient().getClientType().getPremiumRating());
        assertEquals("Special offer you can not refuse.",
                getClientResponse.getClient().getClientType().getSpecialOffers());
    }
}
