package com.github.jonasmelchior.mymme.service;

import com.github.jonasmelchior.mymme.data.UeContext;
import com.github.jonasmelchior.mymme.repository.UeRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

/**
 * Exhaustive State Machine for ECM (EPS Connection Management).
 * Handles all state transitions defined in 3GPP TS 23.401.
 */
@ApplicationScoped
public class EcmStateMachine {

    private static final Logger LOG = Logger.getLogger(EcmStateMachine.class);

    @Inject
    UeRepository ueRepository;

    public void onEvent(UeContext context, EcmEvent event) {
        UeContext.EcmState currentState = context.getEcmState();
        UeContext.EcmState nextState = currentState;

        switch (currentState) {
            case ECM_IDLE:
                if (event == EcmEvent.S1_CONNECTION_ESTABLISHED) {
                    nextState = UeContext.EcmState.ECM_CONNECTED;
                }
                break;

            case ECM_CONNECTED:
                if (event == EcmEvent.S1_CONNECTION_RELEASED) {
                    nextState = UeContext.EcmState.ECM_IDLE;
                }
                break;
        }

        if (nextState != currentState) {
            LOG.infof("ECM Transition: IMSI=%s, %s -> %s", context.getImsi(), currentState, nextState);
            context.setEcmState(nextState);
            ueRepository.save(context);
        }
    }

    public enum EcmEvent {
        S1_CONNECTION_ESTABLISHED,
        S1_CONNECTION_RELEASED
    }
}
