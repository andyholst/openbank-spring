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

package se.jsquad.adapter;

import se.jsquad.api.client.AccountApi;
import se.jsquad.api.client.AccountTransactionApi;
import se.jsquad.api.client.ClientApi;
import se.jsquad.api.client.ClientTypeApi;
import se.jsquad.api.client.PersonApi;
import se.jsquad.api.client.TransactionTypeApi;
import se.jsquad.api.client.TypeApi;
import se.jsquad.entity.Account;
import se.jsquad.entity.AccountTransaction;
import se.jsquad.entity.Client;
import se.jsquad.entity.ForeignClient;
import se.jsquad.entity.Person;
import se.jsquad.entity.PremiumClient;
import se.jsquad.entity.RegularClient;
import se.jsquad.entity.TransactionType;

import javax.inject.Named;
import java.util.HashSet;
import java.util.Set;

@Named
public class ClientAdapterImpl implements ClientAdapter {
    @Override
    public ClientApi translateClientToClientApi(Client client) {
        ClientApi clientApi = new ClientApi();
        PersonApi personApi = new PersonApi();
        ClientTypeApi clientTypeApi = new ClientTypeApi();

        if (client.getClientType() instanceof RegularClient) {
            clientTypeApi.setRating(((RegularClient) client.getClientType()).getRating());
            clientTypeApi.setType(TypeApi.REGULAR);
        } else if (client.getClientType() instanceof PremiumClient) {
            clientTypeApi.setPremiumRating(((PremiumClient) client.getClientType()).getPremiumRating());
            clientTypeApi.setSpecialOffers(((PremiumClient) client.getClientType()).getSpecialOffers());
            clientTypeApi.setType(TypeApi.PREMIUM);
        } else {
            clientTypeApi.setCountry(((ForeignClient) client.getClientType()).getCountry());
            clientTypeApi.setType(TypeApi.FOREIGN);
        }

        clientApi.setClientType(clientTypeApi);

        personApi.setFirstName(client.getPerson().getFirstName());
        personApi.setLastName(client.getPerson().getLastName());
        personApi.setPersonIdentification(client.getPerson().getPersonIdentification());
        personApi.setMail(client.getPerson().getMail());

        Set<AccountApi> accountApiSet = new HashSet<>();

        for (Account account : client.getAccountSet()) {
            AccountApi accountApi = new AccountApi();

            accountApi.setBalance(account.getBalance());

            for (AccountTransaction accountTransaction : account.getAccountTransactionSet()) {
                AccountTransactionApi accountTransactionApi = new AccountTransactionApi();

                accountTransactionApi.setMessage(accountTransaction.getMessage());

                TransactionTypeApi transactionTypeApi = TransactionTypeApi.valueOf(accountTransaction
                        .getTransactionType().name());

                accountTransactionApi.setTransactionType(transactionTypeApi);

                accountApi.getAccountTransactionList().add(accountTransactionApi);
            }

            accountApiSet.add(accountApi);
        }


        clientApi.setPerson(personApi);
        clientApi.getAccountList().addAll(accountApiSet);

        return clientApi;
    }

    @Override
    public Client translateClientApiToClient(ClientApi clientApi) {
        Client client = new Client();
        client.setPerson(new Person());

        client.getPerson().setFirstName(clientApi.getPerson().getFirstName());
        client.getPerson().setLastName(clientApi.getPerson().getLastName());
        client.getPerson().setPersonIdentification(clientApi.getPerson().getPersonIdentification());
        client.getPerson().setMail(clientApi.getPerson().getMail());
        client.getPerson().setClient(client);

        if (TypeApi.REGULAR.equals(clientApi.getClientType().getType())) {
            client.setClientType(new RegularClient());
            ((RegularClient) client.getClientType()).setRating(clientApi.getClientType().getRating());
        } else if (TypeApi.PREMIUM.equals(clientApi.getClientType().getType())) {
            client.setClientType(new PremiumClient());
            ((PremiumClient) client.getClientType()).setPremiumRating(clientApi.getClientType().getPremiumRating());
            ((PremiumClient) client.getClientType()).setSpecialOffers(clientApi.getClientType().getSpecialOffers());
        } else {
            client.setClientType(new ForeignClient());
            ((ForeignClient) client.getClientType()).setCountry(clientApi.getClientType().getCountry());
        }

        client.getClientType().setClient(client);

        Set<Account> accountSet = new HashSet<>();

        if (clientApi.getAccountList() != null) {
            for (AccountApi accountApi : clientApi.getAccountList()) {
                Account account = new Account();

                account.setBalance(accountApi.getBalance());
                account.setAccountTransactionSet(new HashSet<>());

                if (accountApi.getAccountTransactionList() != null) {
                    for (AccountTransactionApi accountTransactionApi : accountApi.getAccountTransactionList()) {
                        AccountTransaction accountTransaction = new AccountTransaction();
                        accountTransaction.setAccount(account);
                        accountTransaction.setMessage(accountTransactionApi.getMessage());
                        accountTransaction.setTransactionType(TransactionType.valueOf(accountTransactionApi
                                .getTransactionType().value()));

                        account.getAccountTransactionSet().add(accountTransaction);
                    }
                }

                account.setClient(client);

                accountSet.add(account);
            }
        }

        client.setAccountSet(accountSet);

        return client;
    }
}
