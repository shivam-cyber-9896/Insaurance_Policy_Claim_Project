package com.monocept.app.enums;

/**
 * Defines the specialization of an agent.
 * - HEALTH, MOTOR, LIFE, TRAVEL: Agent can only handle policies and claims of that specific product type.
 * - SUPER: Agent can handle all policy types and review all claims (no restriction).
 *
 * These values mirror ProductType intentionally so that specialization
 * can be directly compared against a policy's ProductType.
 */
public enum AgentSpecialization {
    HEALTH,
    MOTOR,
    LIFE,
    TRAVEL,
    SUPER   // Can access all policy types and claims
}
