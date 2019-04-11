package se.jsquad.repository;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import se.jsquad.entity.Client;
import se.jsquad.producer.OpenBankPersistenceUnitProducerAbstract;

import javax.persistence.TypedQuery;
import java.util.List;


@Repository("clientRepositoryImpl")
public class ClientRepositoryImpl extends OpenBankPersistenceUnitProducerAbstract implements ClientRepository {
    private Logger logger;

    @Autowired
    private ClientRepositoryImpl(@Qualifier("logger") Logger logger) {
        this.logger = logger;
        this.logger.log(Level.INFO, "ClientRepositoryImpl(logger: {})", logger);
    }

    @Override
    public Client getClientInformation(String personIdentification) {
        logger.log(Level.INFO, "getClientInformation(personIdentification: {})", "hidden");

        TypedQuery<Client> query = entityManager.createNamedQuery(Client.PERSON_IDENTIFICATION, Client.class);
        query.setParameter(Client.PERSON_IDENTIFICSTION_PARAM, personIdentification);

        List<Client> clientList = query.getResultList();

        if (clientList == null || clientList.isEmpty()) {
            return null;
        } else {
            return clientList.get(0);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void persistClient(Client client) {
        logger.log(Level.INFO, "persistClient(client: {})", client);

        entityManager.persist(client);
    }
}
