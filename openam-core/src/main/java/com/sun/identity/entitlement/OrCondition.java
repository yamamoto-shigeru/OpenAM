/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009 Sun Microsystems Inc. All Rights Reserved
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * https://opensso.dev.java.net/public/CDDLv1.0.html or
 * opensso/legal/CDDLv1.0.txt
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at opensso/legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * $Id: OrCondition.java,v 1.2 2009/09/05 00:24:04 veiming Exp $
 *
 * Portions Copyrighted 2014-2015 ForgeRock AS.
 */
package com.sun.identity.entitlement;

import org.forgerock.openam.utils.CollectionUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.security.auth.Subject;

/**
 * {@link EntitlementCondition} wrapper on a set of
 * {@link EntitlementCondition}s to provide boolean OR logic.
 *
 * Membership of {@link OrCondition} is satisfied if the user is a member of any
 * of the wrapped {@link EntitlementCondition}.
 */
public class OrCondition extends LogicalCondition {

    /**
     * Constructor.
     */
    public OrCondition() {
        super();
    }

    /**
     * Constructor for providing {@link EntitlementCondition}s.
     *
     * @param eConditions Wrapped {@link EntitlementCondition}(s).
     */
    public OrCondition(Set<EntitlementCondition> eConditions) {
        super(eConditions);
    }

    /**
     * Evaluates this {@link ConditionDecision}'s {@link EntitlementCondition}s to determine the correct
     * decision to return - if any of the {@link EntitlementCondition}s are true, the returned decision is
     * satisfied and has no advices.
     *
     * @param realm Realm name.
     * @param subject EntitlementCondition under evaluation.
     * @param resourceName Resource name.
     * @param environment Environment parameters.
     * @return the {@link ConditionDecision} having performed the {@link EntitlementCondition}(s) evaluation.
     * @throws {@link EntitlementException} in case of any error.
     */
    public ConditionDecision evaluate(String realm, Subject subject, String resourceName,
                                      Map<String, Set<String>> environment) throws EntitlementException {

        final Set<EntitlementCondition> conditions = getEConditions();

        if (CollectionUtils.isEmpty(conditions)) {
            return ConditionDecision
                    .newSuccessBuilder()
                    .build();
        }

        Map<String, Set<String>> advice = new HashMap<>();
        Map<String, Set<String>> responseAttributes = new HashMap<>();
        long ttl = Long.MAX_VALUE;

        for (EntitlementCondition condition : conditions) {

            ConditionDecision decision = condition.evaluate(realm, subject, resourceName, environment);
            advice.putAll(decision.getAdvice());
            responseAttributes.putAll(decision.getResponseAttributes());

            if (decision.getTimeToLive() < ttl) {
                ttl = decision.getTimeToLive();
            }

            if (decision.isSatisfied()) {
                return ConditionDecision
                        .newSuccessBuilder()
                        .setResponseAttributes(responseAttributes)
                        .setTimeToLive(ttl)
                        .build();
            }
        }

        return ConditionDecision
                .newFailureBuilder()
                .setAdvice(advice)
                .setResponseAttributes(responseAttributes)
                .build();
    }
}
