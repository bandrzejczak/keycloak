/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.connections.jpa;

import javax.persistence.EntityManagerFactory;

import org.hibernate.SessionFactory;
import org.hibernate.jpa.internal.EntityManagerFactoryImpl;
import org.hibernate.stat.QueryStatistics;
import org.hibernate.stat.Statistics;
import org.jboss.logging.Logger;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.scheduled.ScheduledTask;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class HibernateStatsReporter implements ScheduledTask {

    private final EntityManagerFactory emf;
    private static final Logger logger = Logger.getLogger(HibernateStatsReporter.class);

    public HibernateStatsReporter(EntityManagerFactory emf) {
        this.emf = emf;
    }


    @Override
    public void run(KeycloakSession session) {
        SessionFactory sessionFactory = ((EntityManagerFactoryImpl) emf).getSessionFactory();
        Statistics stats = sessionFactory.getStatistics();

        logStats(stats);

        stats.clear(); // For now, clear stats after each iteration
    }


    protected void logStats(Statistics stats) {
        String lineSep = System.getProperty("line.separator");
        StringBuilder builder = new StringBuilder(lineSep).append(stats.toString()).append(lineSep);
        builder.append(lineSep).append("Queries statistics: ").append(lineSep).append(lineSep);

        for (String query : stats.getQueries()) {
            QueryStatistics queryStats = stats.getQueryStatistics(query);

            builder.append(query).append(lineSep)
                    .append("executionCount=" + queryStats.getExecutionCount()).append(lineSep)
                    .append("executionAvgTime=" + queryStats.getExecutionAvgTime()).append(" ms").append(lineSep)
                    .append(lineSep);

            builder.append(lineSep);
        }

        logger.infof(builder.toString());
    }

}